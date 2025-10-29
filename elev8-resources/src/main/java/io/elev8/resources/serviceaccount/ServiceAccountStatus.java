package io.elev8.resources.serviceaccount;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.ObjectReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

/**
 * ServiceAccountStatus represents the current status of a ServiceAccount.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceAccountStatus {
    /**
     * Secrets is the list of secrets currently allowed for use by pods running
     * as this ServiceAccount.
     */
    @Singular("secret")
    private List<ObjectReference> secrets;
}
