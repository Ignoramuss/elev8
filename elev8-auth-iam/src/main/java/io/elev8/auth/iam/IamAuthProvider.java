package io.elev8.auth.iam;

import io.elev8.core.auth.AuthProvider;
import io.elev8.core.auth.AuthenticationException;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.signer.Aws4Signer;
import software.amazon.awssdk.auth.signer.params.Aws4PresignerParams;
import software.amazon.awssdk.http.SdkHttpFullRequest;
import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.regions.Region;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

/**
 * IAM-based authentication provider for EKS.
 * Generates authentication tokens using AWS STS GetCallerIdentity with Signature Version 4.
 */
@Slf4j
public final class IamAuthProvider implements AuthProvider {

    private static final Duration TOKEN_EXPIRATION = Duration.ofMinutes(14);
    private static final int DEFAULT_PRESIGNED_URL_EXPIRATION_SECONDS = 60;
    private static final String DEFAULT_STS_SERVICE_NAME = "sts";
    private static final String DEFAULT_TOKEN_PREFIX = "k8s-aws-v1.";

    private final String clusterName;
    private final Region region;
    private final AwsCredentialsProvider credentialsProvider;
    private final int presignedUrlExpirationSeconds;
    private final String stsServiceName;
    private final String tokenPrefix;
    private final Aws4Signer signer;

    private String cachedToken;
    private Instant tokenExpiration;

    private IamAuthProvider(final Builder builder) {
        this.clusterName = builder.clusterName;
        this.region = builder.region != null ? builder.region : Region.US_EAST_1;
        this.credentialsProvider = builder.credentialsProvider != null ?
                builder.credentialsProvider : DefaultCredentialsProvider.create();
        this.presignedUrlExpirationSeconds = builder.presignedUrlExpirationSeconds != null ?
                builder.presignedUrlExpirationSeconds : DEFAULT_PRESIGNED_URL_EXPIRATION_SECONDS;
        this.stsServiceName = builder.stsServiceName != null ?
                builder.stsServiceName : DEFAULT_STS_SERVICE_NAME;
        this.tokenPrefix = builder.tokenPrefix != null ?
                builder.tokenPrefix : DEFAULT_TOKEN_PREFIX;
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

            final AwsCredentials credentials = credentialsProvider.resolveCredentials();

            final String stsEndpoint = "https://sts." + region.id() + ".amazonaws.com";
            final SdkHttpFullRequest httpRequest = SdkHttpFullRequest.builder()
                    .uri(URI.create(stsEndpoint + "/?Action=GetCallerIdentity&Version=2011-06-15"))
                    .method(SdkHttpMethod.GET)
                    .putHeader("x-k8s-aws-id", clusterName)
                    .build();

            final Aws4PresignerParams presignerParams = Aws4PresignerParams.builder()
                    .awsCredentials(credentials)
                    .signingRegion(region)
                    .signingName(stsServiceName)
                    .expirationTime(Instant.now().plusSeconds(presignedUrlExpirationSeconds))
                    .build();

            final SdkHttpFullRequest presignedRequest = signer.presign(httpRequest, presignerParams);

            final String presignedUrl = presignedRequest.getUri().toString();

            final String encodedUrl = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(presignedUrl.getBytes(StandardCharsets.UTF_8));

            cachedToken = tokenPrefix + encodedUrl;
            tokenExpiration = Instant.now().plus(TOKEN_EXPIRATION);

            log.debug("Successfully generated IAM token, expires at: {}", tokenExpiration);

        } catch (Exception e) {
            throw new AuthenticationException("Failed to generate IAM authentication token", e);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String clusterName;
        private Region region;
        private AwsCredentialsProvider credentialsProvider;
        private Integer presignedUrlExpirationSeconds;
        private String stsServiceName;
        private String tokenPrefix;

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

        public Builder credentialsProvider(final AwsCredentialsProvider credentialsProvider) {
            this.credentialsProvider = credentialsProvider;
            return this;
        }

        public Builder presignedUrlExpirationSeconds(final int presignedUrlExpirationSeconds) {
            this.presignedUrlExpirationSeconds = presignedUrlExpirationSeconds;
            return this;
        }

        public Builder stsServiceName(final String stsServiceName) {
            this.stsServiceName = stsServiceName;
            return this;
        }

        public Builder tokenPrefix(final String tokenPrefix) {
            this.tokenPrefix = tokenPrefix;
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
