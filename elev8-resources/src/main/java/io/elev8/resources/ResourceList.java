package io.elev8.resources;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResourceList<T extends KubernetesResource> {

    private String apiVersion;
    private String kind;
    private ListMetadata metadata;
    private List<T> items;

    public ResourceList() {
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public ListMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(ListMetadata metadata) {
        this.metadata = metadata;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ListMetadata {
        private String resourceVersion;
        private String continueToken;

        public ListMetadata() {
        }

        public String getResourceVersion() {
            return resourceVersion;
        }

        public void setResourceVersion(String resourceVersion) {
            this.resourceVersion = resourceVersion;
        }

        public String getContinueToken() {
            return continueToken;
        }

        public void setContinueToken(String continueToken) {
            this.continueToken = continueToken;
        }
    }
}
