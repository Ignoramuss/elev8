package io.elev8.auth.iam;

import io.elev8.core.auth.AuthProvider;
import io.elev8.core.auth.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.signer.Aws4Signer;
import software.amazon.awssdk.auth.signer.params.Aws4SignerParams;
import software.amazon.awssdk.http.SdkHttpFullRequest;
import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.regions.Region;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

/**
 * IAM-based authentication provider for EKS.
 * Generates authentication tokens using AWS STS GetCallerIdentity with Signature Version 4.
 */
public class IamAuthProvider implements AuthProvider {

    private static final Logger log = LoggerFactory.getLogger(IamAuthProvider.class);
    private static final String TOKEN_PREFIX = "k8s-aws-v1.";
    private static final Duration TOKEN_EXPIRATION = Duration.ofMinutes(14);
    private static final int PRESIGNED_URL_EXPIRATION_SECONDS = 60;

    private final String clusterName;
    private final Region region;
    private final AwsCredentialsProvider credentialsProvider;
    private final Aws4Signer signer;

    private String cachedToken;
    private Instant tokenExpiration;

    private IamAuthProvider(Builder builder) {
        this.clusterName = builder.clusterName;
        this.region = builder.region != null ? builder.region : Region.US_EAST_1;
        this.credentialsProvider = builder.credentialsProvider != null ?
                builder.credentialsProvider : DefaultCredentialsProvider.create();
        this.signer = Aws4Signer.create();
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
        // Refresh 1 minute before expiration
        return Instant.now().plus(Duration.ofMinutes(1)).isAfter(tokenExpiration);
    }

    @Override
    public void refresh() throws AuthenticationException {
        try {
            log.debug("Generating new IAM authentication token for cluster: {}", clusterName);

            // Get credentials
            AwsCredentials credentials = credentialsProvider.resolveCredentials();

            // Build STS GetCallerIdentity request
            String stsEndpoint = "https://sts." + region.id() + ".amazonaws.com";
            SdkHttpFullRequest httpRequest = SdkHttpFullRequest.builder()
                    .uri(URI.create(stsEndpoint + "/?Action=GetCallerIdentity&Version=2011-06-15"))
                    .method(SdkHttpMethod.GET)
                    .putHeader("x-k8s-aws-id", clusterName)
                    .build();

            // Sign the request
            Aws4SignerParams signerParams = Aws4SignerParams.builder()
                    .awsCredentials(credentials)
                    .signingRegion(region)
                    .signingName("sts")
                    .build();

            SdkHttpFullRequest signedRequest = signer.sign(httpRequest, signerParams);

            // Build the presigned URL
            String presignedUrl = buildPresignedUrl(signedRequest);

            // Encode the URL in base64
            String encodedUrl = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(presignedUrl.getBytes(StandardCharsets.UTF_8));

            // Create token with prefix
            cachedToken = TOKEN_PREFIX + encodedUrl;
            tokenExpiration = Instant.now().plus(TOKEN_EXPIRATION);

            log.debug("Successfully generated IAM token, expires at: {}", tokenExpiration);

        } catch (Exception e) {
            throw new AuthenticationException("Failed to generate IAM authentication token", e);
        }
    }

    private String buildPresignedUrl(SdkHttpFullRequest request) {
        StringBuilder url = new StringBuilder();
        url.append(request.protocol()).append("://");
        url.append(request.host());

        if (request.port() > 0 && request.port() != 443) {
            url.append(":").append(request.port());
        }

        url.append(request.encodedPath());

        // Add query parameters
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

        // Add headers as query parameters (for x-k8s-aws-id)
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

    private String encodeURIComponent(String value) {
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

    public static class Builder {
        private String clusterName;
        private Region region;
        private AwsCredentialsProvider credentialsProvider;

        public Builder clusterName(String clusterName) {
            this.clusterName = clusterName;
            return this;
        }

        public Builder region(Region region) {
            this.region = region;
            return this;
        }

        public Builder region(String region) {
            this.region = Region.of(region);
            return this;
        }

        public Builder credentialsProvider(AwsCredentialsProvider credentialsProvider) {
            this.credentialsProvider = credentialsProvider;
            return this;
        }

        public IamAuthProvider build() {
            if (clusterName == null || clusterName.isEmpty()) {
                throw new IllegalArgumentException("Cluster name is required");
            }
            return new IamAuthProvider(this);
        }
    }

    @Override
    public void close() {
        // No resources to close
    }
}
