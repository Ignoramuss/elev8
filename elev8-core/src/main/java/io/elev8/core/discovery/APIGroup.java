package io.elev8.core.discovery;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Data
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class APIGroup {

    private final String name;

    @Singular("version")
    private final List<APIGroupVersion> versions;

    private final APIGroupVersion preferredVersion;

    public String getPreferredVersionString() {
        if (preferredVersion != null) {
            return preferredVersion.getVersion();
        }
        if (versions != null && !versions.isEmpty()) {
            return versions.get(0).getVersion();
        }
        return null;
    }

    public String getPreferredGroupVersion() {
        if (preferredVersion != null) {
            return preferredVersion.getGroupVersion();
        }
        if (versions != null && !versions.isEmpty()) {
            return versions.get(0).getGroupVersion();
        }
        return null;
    }
}
