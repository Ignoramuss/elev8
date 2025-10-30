package io.elev8.resources.persistentvolume;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.jackson.Jacksonized;

/**
 * HostPathVolumeSource represents a host path mapped into a pod.
 * Host path volumes are useful for development and testing only.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HostPathVolumeSource {
    /**
     * Path of the directory on the host.
     */
    @NonNull
    private String path;

    /**
     * Type for HostPath Volume. Defaults to "".
     * Possible values: "", DirectoryOrCreate, Directory, FileOrCreate, File, Socket, CharDevice, BlockDevice.
     */
    private String type;
}
