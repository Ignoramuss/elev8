package io.elev8.core.discovery;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Data
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class APIResource {

    private final String name;
    private final String singularName;
    private final String kind;

    @JsonProperty("namespaced")
    private final boolean namespaced;

    @Singular("verb")
    private final List<String> verbs;

    private final String group;
    private final String version;
    private final List<String> shortNames;
    private final List<String> categories;
    private final String storageVersionHash;

    public boolean isNamespaced() {
        return namespaced;
    }

    public boolean isClusterScoped() {
        return !namespaced;
    }

    public boolean supportsVerb(final String verb) {
        return verbs != null && verbs.contains(verb);
    }

    public boolean supportsCreate() {
        return supportsVerb("create");
    }

    public boolean supportsGet() {
        return supportsVerb("get");
    }

    public boolean supportsList() {
        return supportsVerb("list");
    }

    public boolean supportsUpdate() {
        return supportsVerb("update");
    }

    public boolean supportsPatch() {
        return supportsVerb("patch");
    }

    public boolean supportsDelete() {
        return supportsVerb("delete");
    }

    public boolean supportsWatch() {
        return supportsVerb("watch");
    }

    public String getApiVersion() {
        if (group == null || group.isEmpty()) {
            return version;
        }
        return group + "/" + version;
    }

    public String getApiPath() {
        if (group == null || group.isEmpty()) {
            return "/api/" + version;
        }
        return "/apis/" + group + "/" + version;
    }

    public boolean isSubresource() {
        return name != null && name.contains("/");
    }

    public String getParentResource() {
        if (!isSubresource()) {
            return null;
        }
        return name.substring(0, name.indexOf('/'));
    }

    public String getSubresourceName() {
        if (!isSubresource()) {
            return null;
        }
        return name.substring(name.indexOf('/') + 1);
    }
}
