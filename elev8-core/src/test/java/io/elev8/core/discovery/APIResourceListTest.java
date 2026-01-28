package io.elev8.core.discovery;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class APIResourceListTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldBuildWithResources() {
        final APIResource pods = APIResource.builder()
                .name("pods")
                .kind("Pod")
                .namespaced(true)
                .build();
        final APIResource services = APIResource.builder()
                .name("services")
                .kind("Service")
                .namespaced(true)
                .build();

        final APIResourceList list = APIResourceList.builder()
                .apiVersion("v1")
                .kind("APIResourceList")
                .groupVersion("v1")
                .resource(pods)
                .resource(services)
                .build();

        assertThat(list.getGroupVersion()).isEqualTo("v1");
        assertThat(list.getResources()).hasSize(2);
    }

    @Test
    void shouldFindResourceByKind() {
        final APIResource pods = APIResource.builder()
                .name("pods")
                .kind("Pod")
                .build();

        final APIResourceList list = APIResourceList.builder()
                .groupVersion("v1")
                .resource(pods)
                .build();

        final Optional<APIResource> found = list.findByKind("Pod");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("pods");
    }

    @Test
    void shouldNotFindSubresourceByKind() {
        final APIResource pods = APIResource.builder()
                .name("pods")
                .kind("Pod")
                .build();
        final APIResource podLog = APIResource.builder()
                .name("pods/log")
                .kind("Pod")
                .build();

        final APIResourceList list = APIResourceList.builder()
                .groupVersion("v1")
                .resource(pods)
                .resource(podLog)
                .build();

        final Optional<APIResource> found = list.findByKind("Pod");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("pods");
    }

    @Test
    void shouldReturnEmptyWhenKindNotFound() {
        final APIResourceList list = APIResourceList.builder()
                .groupVersion("v1")
                .build();

        final Optional<APIResource> found = list.findByKind("Pod");

        assertThat(found).isEmpty();
    }

    @Test
    void shouldFindResourceByName() {
        final APIResource pods = APIResource.builder()
                .name("pods")
                .kind("Pod")
                .build();

        final APIResourceList list = APIResourceList.builder()
                .groupVersion("v1")
                .resource(pods)
                .build();

        final Optional<APIResource> found = list.findByName("pods");

        assertThat(found).isPresent();
        assertThat(found.get().getKind()).isEqualTo("Pod");
    }

    @Test
    void shouldExtractGroupFromGroupVersion() {
        final APIResourceList list = APIResourceList.builder()
                .groupVersion("apps/v1")
                .build();

        assertThat(list.getGroup()).isEqualTo("apps");
        assertThat(list.getVersion()).isEqualTo("v1");
    }

    @Test
    void shouldReturnEmptyGroupForCoreApi() {
        final APIResourceList list = APIResourceList.builder()
                .groupVersion("v1")
                .build();

        assertThat(list.getGroup()).isEmpty();
        assertThat(list.getVersion()).isEqualTo("v1");
    }

    @Test
    void shouldHandleNullGroupVersion() {
        final APIResourceList list = APIResourceList.builder().build();

        assertThat(list.getGroup()).isEmpty();
        assertThat(list.getVersion()).isNull();
    }

    @Test
    void shouldSerializeToJson() throws Exception {
        final APIResource pods = APIResource.builder()
                .name("pods")
                .kind("Pod")
                .namespaced(true)
                .build();

        final APIResourceList list = APIResourceList.builder()
                .apiVersion("v1")
                .kind("APIResourceList")
                .groupVersion("v1")
                .resource(pods)
                .build();

        final String json = objectMapper.writeValueAsString(list);

        assertThat(json).contains("\"groupVersion\":\"v1\"");
        assertThat(json).contains("\"resources\":");
    }

    @Test
    void shouldDeserializeFromJson() throws Exception {
        final String json = """
                {
                    "apiVersion": "v1",
                    "kind": "APIResourceList",
                    "groupVersion": "apps/v1",
                    "resources": [
                        {
                            "name": "deployments",
                            "kind": "Deployment",
                            "namespaced": true
                        }
                    ]
                }
                """;

        final APIResourceList list = objectMapper.readValue(json, APIResourceList.class);

        assertThat(list.getGroupVersion()).isEqualTo("apps/v1");
        assertThat(list.getResources()).hasSize(1);
        assertThat(list.getResources().get(0).getKind()).isEqualTo("Deployment");
    }

    @Test
    void shouldHandleNullResources() {
        final APIResourceList list = APIResourceList.builder()
                .groupVersion("v1")
                .build();

        assertThat(list.findByKind("Pod")).isEmpty();
        assertThat(list.findByName("pods")).isEmpty();
    }
}
