package io.elev8.resources.volumesnapshot;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * VolumeSnapshotError describes an error encountered during snapshot creation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VolumeSnapshotError {
    /**
     * Message is a string detailing the encountered error during snapshot creation.
     * NOTE: Message may be logged, and it should not contain sensitive information.
     */
    private String message;

    /**
     * Time is the timestamp when the error was encountered.
     * Format: RFC3339 date-time
     */
    private String time;
}
