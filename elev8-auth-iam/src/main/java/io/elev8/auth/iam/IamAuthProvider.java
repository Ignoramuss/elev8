package io.elev8.auth.iam;

import io.elev8.core.auth.AuthProvider;
import io.elev8.core.auth.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.signer.AwsSignerExecutionAttribute;
import software.amazon.awssdk.http.SdkHttpFullRequest;
import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sts.auth.StsPresigner;
import software.amazon.awssdk.services.sts.model.GetCallerIdentityRequest;
import software.amazon.awssdk.services.sts.presigner.model.GetCallerIdentityPresignRequest;

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
    private static final Duration PRE_SIGNATURE_EXPIRATION = Duration.ofMinutes(15);

    private final String clusterName;
    private final Region region;
    private final AwsCredentialsProvider credentialsProvider;
    private final StsPresigner stsPresigner;

    private String cachedToken;
    private Instant tokenExpiration;

    private IamAuthProvider(Builder builder) {
        this.clusterName = builder.clusterName;
        this.region = builder.region != null ? builder.region : Region.US_EAST_1;
        this.credentialsProvider = builder.credentialsProvider != null ?
                builder.credentialsProvider : DefaultCredentialsProvider.create();

        this.stsPresigner = StsPresigner.builder()
                .region(this.region)
                .credentialsProvider(this.credentialsProvider)
                .build();
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

            // Create GetCallerIdentity request
            GetCallerIdentityRequest callerIdentityRequest = GetCallerIdentityRequest.builder().build();

            // Create presign request with custom headers
            GetCallerIdentityPresignRequest presignRequest = GetCallerIdentityPresignRequest.builder()
                    .getCallerIdentityRequest(callerIdentityRequest)
                    .signatureDuration(PRE_SIGNATURE_EXPIRATION)
                    .build();

            // Presign the request
            SdkHttpFullRequest presignedRequest = stsPresigner.presignGetCallerIdentity(presignRequest)
                    .httpRequest();

            // Add cluster name header to the URL
            String url = buildPresignedUrl(presignedRequest);

            // Encode the URL in base64
            String encodedUrl = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(url.getBytes(StandardCharsets.UTF_8));

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

        if (request.port() > 0) {
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

        // Add cluster name as header
        url.append("&x-k8s-aws-id=").append(encodeURIComponent(clusterName));

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
        if (stsPresigner != null) {
            stsPresigner.close();
        }
    }
}
