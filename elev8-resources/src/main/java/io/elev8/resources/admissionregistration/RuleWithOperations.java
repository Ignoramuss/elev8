package io.elev8.resources.admissionregistration;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RuleWithOperations {
    @Singular("operation")
    private List<String> operations;

    @Singular("apiGroup")
    private List<String> apiGroups;

    @Singular("apiVersion")
    private List<String> apiVersions;

    @Singular("resource")
    private List<String> resources;

    private String scope;
}
