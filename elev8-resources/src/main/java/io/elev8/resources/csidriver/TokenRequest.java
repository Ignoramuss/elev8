package io.elev8.resources.csidriver;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * TokenRequest contains information about a service account token request.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokenRequest {
    /**
     * Audience is the intended audience of the token.
     * A recipient of a token must identify itself with an identifier in the audience.
     * Required field.
     *
     * Audiences should be distinct to avoid validation failures.
     * Empty string means the token audience is the APIAudiences of kube-apiserver.
     */
    private String audience;

    /**
     * ExpirationSeconds is the duration of validity of the token in seconds.
     * Optional field with constraints:
     * - Minimum: 600 seconds (10 minutes)
     * - Maximum: 2^32 seconds
     *
     * If not specified, uses the default ExpirationSeconds from TokenRequestSpec.
     */
    private Long expirationSeconds;
}
