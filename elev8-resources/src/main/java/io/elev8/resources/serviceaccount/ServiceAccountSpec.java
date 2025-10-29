package io.elev8.resources.serviceaccount;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.LocalObjectReference;
import io.elev8.resources.ObjectReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

/**
 * ServiceAccountSpec defines the desired state of a ServiceAccount.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceAccountSpec {
    /**
     * AutomountServiceAccountToken indicates whether pods running as this service account
     * should have an API token automatically mounted. Can be overridden at the pod level.
     */
    private Boolean automountServiceAccountToken;

    /**
     * Secrets is a list of secret references that can be used by pods running using
     * this ServiceAccount.
     */
    @Singular("secret")
    private List<ObjectReference> secrets;

    /**
     * ImagePullSecrets is a list of references to secrets in the same namespace to use
     * for pulling any images in pods that reference this ServiceAccount.
     */
    @Singular("imagePullSecret")
    private List<LocalObjectReference> imagePullSecrets;
}
