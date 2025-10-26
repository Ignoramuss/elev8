package io.elev8.auth.oidc;

import io.elev8.core.auth.AuthProvider;
import io.elev8.core.auth.AuthenticationException;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.signer.Aws4Signer;
import software.amazon.awssdk.auth.signer.params.Aws4SignerParams;
import software.amazon.awssdk.http.SdkHttpFullRequest;
import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.AssumeRoleWithWebIdentityRequest;
import software.amazon.awssdk.services.sts.model.AssumeRoleWithWebIdentityResponse;
import software.amazon.awssdk.services.sts.model.Credentials;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

/**
 * OIDC/IRSA-based authentication provider for EKS.
 * Uses web identity tokens to assume IAM roles and generate Kubernetes authentication tokens.
 */
@Slf4j
public final class OidcAuthProvider implements AuthProvider {

    private static final String TOKEN_PREFIX = "k8s-aws-v1.";
    private static final Duration TOKEN_EXPIRATION = Duration.ofMinutes(14);
    private static final String AWS_WEB_IDENTITY_TOKEN_FILE = "AWS_WEB_IDENTITY_TOKEN_FILE";
    private static final String AWS_ROLE_ARN = "AWS_ROLE_ARN";
    private static final String AWS_ROLE_SESSION_NAME = "AWS_ROLE_SESSION_NAME";
    private static final int ASSUMED_ROLE_DURATION_SECONDS = 3600;

    private final String clusterName;
    private final Region region;
    private final String roleArn;
    private final String webIdentityTokenFile;
    private final String roleSessionName;
    private final StsClient stsClient;
    private final Aws4Signer signer;

    private String cachedToken;
    private Instant tokenExpiration;
    private AwsSessionCredentials sessionCredentials;
    private Instant credentialsExpiration;

    private OidcAuthProvider(final Builder builder) {
        this.clusterName = builder.clusterName;
        this.region = builder.region != null ? builder.region : Region.US_EAST_1;
        this.roleArn = builder.roleArn != null ? builder.roleArn : System.getenv(AWS_ROLE_ARN);
        this.webIdentityTokenFile = builder.webIdentityTokenFile != null ?
                builder.webIdentityTokenFile : System.getenv(AWS_WEB_IDENTITY_TOKEN_FILE);
        this.roleSessionName = builder.roleSessionName != null ?
                builder.roleSessionName : getDefaultSessionName();
        this.stsClient = builder.stsClient != null ?
                builder.stsClient : StsClient.builder().region(region).build();
        this.signer = Aws4Signer.create();

        validateConfiguration();
    }

    private void validateConfiguration() {
        if (roleArn == null || roleArn.isEmpty()) {
            throw new IllegalArgumentException("Role ARN is required. Set via builder or " + AWS_ROLE_ARN + " environment variable");
        }
        if (webIdentityTokenFile == null || webIdentityTokenFile.isEmpty()) {
            throw new IllegalArgumentException("Web identity token file is required. Set via builder or " + AWS_WEB_IDENTITY_TOKEN_FILE + " environment variable");
        }
    }

    private String getDefaultSessionName() {
        final String envSessionName = System.getenv(AWS_ROLE_SESSION_NAME);
        return envSessionName != null ? envSessionName : "elev8-oidc-session";
    }

    @Override
    public String getToken() throws AuthenticationException {
        if (cachedToken == null || needsRefresh()) {
            refresh();
        }
        return cachedToken;
    }

    @Override
    public boolean needsRefresh() {
        if (tokenExpiration == null) {
            return true;
        }
        return Instant.now().plus(Duration.ofMinutes(1)).isAfter(tokenExpiration);
    }

    @Override
    public void refresh() throws AuthenticationException {
        try {
            log.debug("Refreshing OIDC authentication token for cluster: {}", clusterName);

            if (sessionCredentials == null || needsCredentialsRefresh()) {
                refreshSessionCredentials();
            }

            final String stsEndpoint = "https://sts." + region.id() + ".amazonaws.com";
            final SdkHttpFullRequest httpRequest = SdkHttpFullRequest.builder()
                    .uri(URI.create(stsEndpoint + "/?Action=GetCallerIdentity&Version=2011-06-15"))
                    .method(SdkHttpMethod.GET)
                    .putHeader("x-k8s-aws-id", clusterName)
                    .build();

            final Aws4SignerParams signerParams = Aws4SignerParams.builder()
                    .awsCredentials(sessionCredentials)
                    .signingRegion(region)
                    .signingName("sts")
                    .build();

            final SdkHttpFullRequest signedRequest = signer.sign(httpRequest, signerParams);
            final String presignedUrl = buildPresignedUrl(signedRequest);
            final String encodedUrl = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(presignedUrl.getBytes(StandardCharsets.UTF_8));

            cachedToken = TOKEN_PREFIX + encodedUrl;
            tokenExpiration = Instant.now().plus(TOKEN_EXPIRATION);

            log.debug("Successfully generated OIDC token, expires at: {}", tokenExpiration);

        } catch (Exception e) {
            throw new AuthenticationException("Failed to generate OIDC authentication token", e);
        }
    }

    private boolean needsCredentialsRefresh() {
        if (credentialsExpiration == null) {
            return true;
        }
        return Instant.now().plus(Duration.ofMinutes(5)).isAfter(credentialsExpiration);
    }

    private void refreshSessionCredentials() throws AuthenticationException {
        try {
            log.debug("Refreshing session credentials using web identity token");

            final String webIdentityToken = readWebIdentityToken();

            final AssumeRoleWithWebIdentityRequest request = AssumeRoleWithWebIdentityRequest.builder()
                    .roleArn(roleArn)
                    .roleSessionName(roleSessionName)
                    .webIdentityToken(webIdentityToken)
                    .durationSeconds(ASSUMED_ROLE_DURATION_SECONDS)
                    .build();

            final AssumeRoleWithWebIdentityResponse response = stsClient.assumeRoleWithWebIdentity(request);
            final Credentials credentials = response.credentials();

            sessionCredentials = AwsSessionCredentials.create(
                    credentials.accessKeyId(),
                    credentials.secretAccessKey(),
                    credentials.sessionToken()
            );

            credentialsExpiration = credentials.expiration();

            log.debug("Successfully assumed role: {}, credentials expire at: {}", roleArn, credentialsExpiration);

        } catch (Exception e) {
            throw new AuthenticationException("Failed to assume role with web identity", e);
        }
    }

    private String readWebIdentityToken() throws IOException {
        final Path tokenPath = Paths.get(webIdentityTokenFile);
        if (!Files.exists(tokenPath)) {
            throw new IOException("Web identity token file not found: " + webIdentityTokenFile);
        }
        return Files.readString(tokenPath, StandardCharsets.UTF_8).trim();
    }

    private String buildPresignedUrl(final SdkHttpFullRequest request) {
        final StringBuilder url = new StringBuilder();
        url.append(request.protocol()).append("://");
        url.append(request.host());

        if (request.port() > 0 && request.port() != 443) {
            url.append(":").append(request.port());
        }

        url.append(request.encodedPath());

        if (!request.rawQueryParameters().isEmpty()) {
            url.append("?");
            boolean first = true;
            for (var entry : request.rawQueryParameters().entrySet()) {
                for (String value : entry.getValue()) {
                    if (!first) {
                        url.append("&");
                    }
                    url.append(entry.getKey()).append("=").append(value);
                    first = false;
                }
            }
        }

        for (var entry : request.headers().entrySet()) {
            if (entry.getKey().equalsIgnoreCase("x-k8s-aws-id")) {
                for (String value : entry.getValue()) {
                    url.append(url.indexOf("?") > 0 ? "&" : "?");
                    url.append(entry.getKey()).append("=").append(encodeURIComponent(value));
                }
            }
        }

        return url.toString();
    }

    private String encodeURIComponent(final String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8)
                .replace("+", "%20")
                .replace("%21", "!")
                .replace("%27", "'")
                .replace("%28", "(")
                .replace("%29", ")")
                .replace("%7E", "~");
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String clusterName;
        private Region region;
        private String roleArn;
        private String webIdentityTokenFile;
        private String roleSessionName;
        private StsClient stsClient;

        public Builder clusterName(final String clusterName) {
            this.clusterName = clusterName;
            return this;
        }

        public Builder region(final Region region) {
            this.region = region;
            return this;
        }

        public Builder region(final String region) {
            this.region = Region.of(region);
            return this;
        }

        public Builder roleArn(final String roleArn) {
            this.roleArn = roleArn;
            return this;
        }

        public Builder webIdentityTokenFile(final String webIdentityTokenFile) {
            this.webIdentityTokenFile = webIdentityTokenFile;
            return this;
        }

        public Builder roleSessionName(final String roleSessionName) {
            this.roleSessionName = roleSessionName;
            return this;
        }

        public Builder stsClient(final StsClient stsClient) {
            this.stsClient = stsClient;
            return this;
        }

        public OidcAuthProvider build() {
            if (clusterName == null || clusterName.isEmpty()) {
                throw new IllegalArgumentException("Cluster name is required");
            }
            return new OidcAuthProvider(this);
        }
    }

    @Override
    public void close() {
        if (stsClient != null) {
            stsClient.close();
        }
    }
}
