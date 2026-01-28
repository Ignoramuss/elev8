package io.elev8.core.discovery;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class APIResourceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldBuildNamespacedResource() {
        final APIResource resource = APIResource.builder()
                .name("pods")
                .singularName("pod")
                .kind("Pod")
                .namespaced(true)
                .verb("create")
                .verb("get")
                .verb("list")
                .verb("update")
                .verb("patch")
                .verb("delete")
                .verb("watch")
                .group("")
                .version("v1")
                .build();

        assertThat(resource.getName()).isEqualTo("pods");
        assertThat(resource.getKind()).isEqualTo("Pod");
        assertThat(resource.isNamespaced()).isTrue();
        assertThat(resource.isClusterScoped()).isFalse();
    }

    @Test
    void shouldBuildClusterScopedResource() {
        final APIResource resource = APIResource.builder()
                .name("nodes")
                .kind("Node")
                .namespaced(false)
                .group("")
                .version("v1")
                .build();

        assertThat(resource.isNamespaced()).isFalse();
        assertThat(resource.isClusterScoped()).isTrue();
    }

    @Test
    void shouldCheckSupportedVerbs() {
        final APIResource resource = APIResource.builder()
                .name("pods")
                .kind("Pod")
                .verb("create")
                .verb("get")
                .verb("list")
                .verb("watch")
                .build();

        assertThat(resource.supportsCreate()).isTrue();
        assertThat(resource.supportsGet()).isTrue();
        assertThat(resource.supportsList()).isTrue();
        assertThat(resource.supportsWatch()).isTrue();
        assertThat(resource.supportsUpdate()).isFalse();
        assertThat(resource.supportsPatch()).isFalse();
        assertThat(resource.supportsDelete()).isFalse();
    }

    @Test
    void shouldReturnApiVersionForCoreApi() {
        final APIResource resource = APIResource.builder()
                .name("pods")
                .kind("Pod")
                .group("")
                .version("v1")
                .build();

        assertThat(resource.getApiVersion()).isEqualTo("v1");
    }

    @Test
    void shouldReturnApiVersionForGroupedApi() {
        final APIResource resource = APIResource.builder()
                .name("deployments")
                .kind("Deployment")
                .group("apps")
                .version("v1")
                .build();

        assertThat(resource.getApiVersion()).isEqualTo("apps/v1");
    }

    @Test
    void shouldReturnApiPathForCoreApi() {
        final APIResource resource = APIResource.builder()
                .name("pods")
                .kind("Pod")
                .group("")
                .version("v1")
                .build();

        assertThat(resource.getApiPath()).isEqualTo("/api/v1");
    }

    @Test
    void shouldReturnApiPathForGroupedApi() {
        final APIResource resource = APIResource.builder()
                .name("crontabs")
                .kind("CronTab")
                .group("stable.example.com")
                .version("v1")
                .build();

        assertThat(resource.getApiPath()).isEqualTo("/apis/stable.example.com/v1");
    }

    @Test
    void shouldIdentifySubresource() {
        final APIResource resource = APIResource.builder()
                .name("pods/log")
                .kind("PodLog")
                .build();

        assertThat(resource.isSubresource()).isTrue();
        assertThat(resource.getParentResource()).isEqualTo("pods");
        assertThat(resource.getSubresourceName()).isEqualTo("log");
    }

    @Test
    void shouldIdentifyNonSubresource() {
        final APIResource resource = APIResource.builder()
                .name("pods")
                .kind("Pod")
                .build();

        assertThat(resource.isSubresource()).isFalse();
        assertThat(resource.getParentResource()).isNull();
        assertThat(resource.getSubresourceName()).isNull();
    }

    @Test
    void shouldSerializeToJson() throws Exception {
        final APIResource resource = APIResource.builder()
                .name("pods")
                .kind("Pod")
                .namespaced(true)
                .verb("get")
                .verb("list")
                .build();

        final String json = objectMapper.writeValueAsString(resource);

        assertThat(json).contains("\"name\":\"pods\"");
        assertThat(json).contains("\"kind\":\"Pod\"");
        assertThat(json).contains("\"namespaced\":true");
    }

    @Test
    void shouldDeserializeFromJson() throws Exception {
        final String json = """
                {
                    "name": "deployments",
                    "singularName": "deployment",
                    "kind": "Deployment",
                    "namespaced": true,
                    "verbs": ["create", "get", "list", "update", "patch", "delete", "watch"],
                    "group": "apps",
                    "version": "v1"
                }
                """;

        final APIResource resource = objectMapper.readValue(json, APIResource.class);

        assertThat(resource.getName()).isEqualTo("deployments");
        assertThat(resource.getKind()).isEqualTo("Deployment");
        assertThat(resource.isNamespaced()).isTrue();
        assertThat(resource.supportsCreate()).isTrue();
        assertThat(resource.getGroup()).isEqualTo("apps");
        assertThat(resource.getVersion()).isEqualTo("v1");
    }

    @Test
    void shouldHandleShortNames() {
        final APIResource resource = APIResource.builder()
                .name("pods")
                .kind("Pod")
                .shortNames(List.of("po"))
                .build();

        assertThat(resource.getShortNames()).contains("po");
    }

    @Test
    void shouldHandleCategories() {
        final APIResource resource = APIResource.builder()
                .name("deployments")
                .kind("Deployment")
                .categories(List.of("all"))
                .build();

        assertThat(resource.getCategories()).contains("all");
    }

    @Test
    void shouldHandleNullVerbs() {
        final APIResource resource = APIResource.builder()
                .name("pods")
                .kind("Pod")
                .build();

        assertThat(resource.supportsVerb("get")).isFalse();
    }
}
