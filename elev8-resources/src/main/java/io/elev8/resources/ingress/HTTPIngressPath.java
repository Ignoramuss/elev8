package io.elev8.resources.ingress;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.jackson.Jacksonized;

/**
 * HTTPIngressPath associates a path with a backend. Incoming urls matching the
 * path are forwarded to the backend.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HTTPIngressPath {
    /**
     * Path is matched against the path of an incoming request. Currently it can
     * contain characters disallowed from the conventional "path" part of a URL
     * as defined by RFC 3986. Paths must begin with a '/' and must be present
     * when using PathType with value "Exact" or "Prefix".
     */
    @NonNull
    private String path;

    /**
     * PathType determines the interpretation of the Path matching. PathType can
     * be one of the following values:
     * * Exact: Matches the URL path exactly.
     * * Prefix: Matches based on a URL path prefix split by '/'.
     * * ImplementationSpecific: Interpretation is up to the IngressClass.
     */
    @NonNull
    private String pathType;

    /**
     * Backend defines the referenced service endpoint to which the traffic
     * will be forwarded to.
     */
    @NonNull
    private IngressBackend backend;
}
