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
public class APIGroupList {

    private final String apiVersion;
    private final String kind;

    @Singular("group")
    private final List<APIGroup> groups;

    public Optional<APIGroup> findByName(final String name) {
        if (groups == null) {
            return Optional.empty();
        }
        return groups.stream()
                .filter(g -> g.getName().equals(name))
                .findFirst();
    }

    public List<APIGroup> getGroups() {
        return groups != null ? groups : List.of();
    }
}
