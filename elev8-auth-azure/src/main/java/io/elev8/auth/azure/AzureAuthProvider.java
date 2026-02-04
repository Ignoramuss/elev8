package io.elev8.auth.azure;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import io.elev8.core.auth.AuthProvider;
import io.elev8.core.auth.AuthenticationException;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * Azure-based authentication provider for AKS.
 * Supports DefaultAzureCredential, explicit TokenCredential, client secret,
 * and managed identity authentication.
 */
@Slf4j
public final class AzureAuthProvider implements AuthProvider {

    private static final String AKS_SCOPE = "6dae42f8-4368-4678-94ff-3960e28e3630/.default";
    private static final Duration REFRESH_THRESHOLD = Duration.ofMinutes(1);

    private final TokenCredential credential;
    private final TokenRequestContext tokenRequestContext;
    private volatile AccessToken cachedToken;

    private AzureAuthProvider(final TokenCredential credential) {
        this.credential = credential;
        this.tokenRequestContext = new TokenRequestContext().addScopes(AKS_SCOPE);
    }

    @Override
    public String getToken() throws AuthenticationException {
        if (cachedToken == null || needsRefresh()) {
            refresh();
        }
        return cachedToken.getToken();
    }

    @Override
    public boolean needsRefresh() {
        if (cachedToken == null) {
            return true;
        }
        final OffsetDateTime expiresAt = cachedToken.getExpiresAt();
        if (expiresAt == null) {
            return true;
        }
        return OffsetDateTime.now().plus(REFRESH_THRESHOLD).isAfter(expiresAt);
    }

    @Override
    public void refresh() throws AuthenticationException {
        try {
            log.debug("Refreshing Azure authentication token");
            final AccessToken token = credential.getToken(tokenRequestContext).block();
            if (token == null) {
                throw new AuthenticationException("Azure credential returned no token");
            }
            cachedToken = token;
            log.debug("Successfully refreshed Azure token, expires at: {}", cachedToken.getExpiresAt());
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthenticationException("Failed to refresh Azure authentication token", e);
        }
    }

    @Override
    public void close() {
        // No resources to close
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private TokenCredential credential;
        private String tenantId;
        private String clientId;
        private String clientSecret;
        private String managedIdentityClientId;

        public Builder credential(final TokenCredential credential) {
            this.credential = credential;
            return this;
        }

        public Builder tenantId(final String tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public Builder clientId(final String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder clientSecret(final String clientSecret) {
            this.clientSecret = clientSecret;
            return this;
        }

        public Builder managedIdentityClientId(final String managedIdentityClientId) {
            this.managedIdentityClientId = managedIdentityClientId;
            return this;
        }

        public AzureAuthProvider build() {
            final TokenCredential resolvedCredential = resolveCredential();
            return new AzureAuthProvider(resolvedCredential);
        }

        private TokenCredential resolveCredential() {
            if (credential != null) {
                return credential;
            }
            if (tenantId != null && clientId != null && clientSecret != null) {
                log.debug("Building ClientSecretCredential for tenant: {}", tenantId);
                return new ClientSecretCredentialBuilder()
                        .tenantId(tenantId)
                        .clientId(clientId)
                        .clientSecret(clientSecret)
                        .build();
            }
            if (managedIdentityClientId != null) {
                log.debug("Building ManagedIdentityCredential for client ID: {}", managedIdentityClientId);
                return new ManagedIdentityCredentialBuilder()
                        .clientId(managedIdentityClientId)
                        .build();
            }
            log.debug("Using DefaultAzureCredential");
            return new DefaultAzureCredentialBuilder().build();
        }
    }
}
