package io.elev8.resources.persistentvolume;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * PersistentVolumeStatus represents the current status/state of a persistent volume.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PersistentVolumeStatus {
    /**
     * Phase indicates if a volume is available, bound to a claim, or released by a claim.
     * Possible values: Pending, Available, Bound, Released, Failed.
     */
    private String phase;

    /**
     * Message is a human-readable message indicating details about why the volume is in this state.
     */
    private String message;

    /**
     * Reason is a brief CamelCase string that describes any failure and is meant for machine parsing
     * and tidy display in the CLI.
     */
    private String reason;
}
