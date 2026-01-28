package io.elev8.core.discovery;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.Optional;

@Data
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class APIResourceList {

    private final String apiVersion;
    private final String kind;
    private final String groupVersion;

    @Singular("resource")
    private final List<APIResource> resources;

    public Optional<APIResource> findByKind(final String kind) {
        if (resources == null) {
            return Optional.empty();
        }
        return resources.stream()
                .filter(r -> r.getKind().equals(kind))
                .filter(r -> !r.isSubresource())
                .findFirst();
    }

    public Optional<APIResource> findByName(final String name) {
        if (resources == null) {
            return Optional.empty();
        }
        return resources.stream()
                .filter(r -> r.getName().equals(name))
                .findFirst();
    }

    public String getGroup() {
        if (groupVersion == null || !groupVersion.contains("/")) {
            return "";
        }
        final int slashIndex = groupVersion.indexOf('/');
        return groupVersion.substring(0, slashIndex);
    }

    public String getVersion() {
        if (groupVersion == null) {
            return null;
        }
        if (!groupVersion.contains("/")) {
            return groupVersion;
        }
        final int slashIndex = groupVersion.indexOf('/');
        return groupVersion.substring(slashIndex + 1);
    }
}
