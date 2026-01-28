package io.elev8.core.discovery;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class APIGroupVersion {

    private final String groupVersion;
    private final String version;

    public String getGroup() {
        if (groupVersion == null || !groupVersion.contains("/")) {
            return "";
        }
        final int slashIndex = groupVersion.indexOf('/');
        return groupVersion.substring(0, slashIndex);
    }
}
