package io.elev8.resources.ingress;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

/**
 * HTTPIngressRuleValue is a list of http selectors pointing to backends.
 * In the example: http://&lt;host&gt;/&lt;path&gt;?&lt;searchpart&gt; -&gt; backend where
 * parts of the url correspond to RFC 3986, this resource will be used to match
 * against everything after the last '/' and before the first '?' or '#'.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HTTPIngressRuleValue {
    /**
     * A collection of paths that map requests to backends.
     */
    @Singular("path")
    private List<HTTPIngressPath> paths;
}
