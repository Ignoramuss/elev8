package io.elev8.resources.persistentvolume;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.jackson.Jacksonized;

/**
 * NFSVolumeSource represents an NFS mount that lasts the lifetime of a pod.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NFSVolumeSource {
    /**
     * Server is the hostname or IP address of the NFS server.
     */
    @NonNull
    private String server;

    /**
     * Path that is exported by the NFS server.
     */
    @NonNull
    private String path;

    /**
     * ReadOnly forces the NFS export to be mounted with read-only permissions.
     * Defaults to false.
     */
    private Boolean readOnly;
}
