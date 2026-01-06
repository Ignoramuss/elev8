package io.elev8.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.core.client.KubernetesClient;
import io.elev8.core.http.HttpResponse;
import io.elev8.core.patch.ApplyOptions;
import io.elev8.core.patch.PatchOptions;
import io.elev8.core.patch.PatchType;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AbstractReadOnlyResourceManagerTest {

    private static final int METHOD_NOT_ALLOWED = 405;

    private KubernetesClient mockClient;
    private TestReadOnlyResourceManager manager;

    @BeforeEach
    void setUp() {
        mockClient = mock(KubernetesClient.class);
        manager = new TestReadOnlyResourceManager(mockClient);
    }

    @Test
    void shouldAllowListOperation() throws Exception {
        final String listJson = """
                {
                  "apiVersion": "v1",
                  "kind": "TestResourceList",
                  "items": []
                }
                """;

        final HttpResponse mockResponse = mock(HttpResponse.class);
        when(mockResponse.isSuccessful()).thenReturn(true);
        when(mockResponse.getBody()).thenReturn(listJson);
        when(mockClient.get("/api/v1/namespaces/default/testresources")).thenReturn(mockResponse);

        final var resources = manager.list("default");

        assertThat(resources).isEmpty();
    }

    @Test
    void shouldAllowGetOperation() throws Exception {
        final String resourceJson = """
                {
                  "apiVersion": "v1",
                  "kind": "TestResource",
                  "metadata": {
                    "name": "test-resource",
                    "namespace": "default"
                  }
                }
                """;

        final HttpResponse mockResponse = mock(HttpResponse.class);
        when(mockResponse.isSuccessful()).thenReturn(true);
        when(mockResponse.getBody()).thenReturn(resourceJson);
        when(mockClient.get("/api/v1/namespaces/default/testresources/test-resource")).thenReturn(mockResponse);

        final TestResource resource = manager.get("default", "test-resource");

        assertThat(resource).isNotNull();
        assertThat(resource.getName()).isEqualTo("test-resource");
    }

    @Test
    void shouldRejectCreateOperation() {
        final TestResource resource = TestResource.builder()
                .name("test")
                .namespace("default")
                .build();

        assertThatThrownBy(() -> manager.create(resource))
                .isInstanceOf(ResourceException.class)
                .hasMessageContaining("read-only")
                .hasMessageContaining("create")
                .satisfies(e -> {
                    final ResourceException re = (ResourceException) e;
                    assertThat(re.getStatusCode()).isEqualTo(METHOD_NOT_ALLOWED);
                });
    }

    @Test
    void shouldRejectUpdateOperation() {
        final TestResource resource = TestResource.builder()
                .name("test")
                .namespace("default")
                .build();

        assertThatThrownBy(() -> manager.update(resource))
                .isInstanceOf(ResourceException.class)
                .hasMessageContaining("read-only")
                .hasMessageContaining("update")
                .satisfies(e -> {
                    final ResourceException re = (ResourceException) e;
                    assertThat(re.getStatusCode()).isEqualTo(METHOD_NOT_ALLOWED);
                });
    }

    @Test
    void shouldRejectDeleteOperation() {
        assertThatThrownBy(() -> manager.delete("default", "test"))
                .isInstanceOf(ResourceException.class)
                .hasMessageContaining("read-only")
                .hasMessageContaining("delete")
                .satisfies(e -> {
                    final ResourceException re = (ResourceException) e;
                    assertThat(re.getStatusCode()).isEqualTo(METHOD_NOT_ALLOWED);
                });
    }

    @Test
    void shouldRejectPatchOperation() {
        final PatchOptions options = PatchOptions.builder()
                .patchType(PatchType.JSON_PATCH)
                .build();

        assertThatThrownBy(() -> manager.patch("default", "test", options, "[]"))
                .isInstanceOf(ResourceException.class)
                .hasMessageContaining("read-only")
                .hasMessageContaining("patch")
                .satisfies(e -> {
                    final ResourceException re = (ResourceException) e;
                    assertThat(re.getStatusCode()).isEqualTo(METHOD_NOT_ALLOWED);
                });
    }

    @Test
    void shouldRejectApplyOperation() {
        final ApplyOptions options = ApplyOptions.builder()
                .fieldManager("test-manager")
                .build();

        assertThatThrownBy(() -> manager.apply("default", "test", options, "{}"))
                .isInstanceOf(ResourceException.class)
                .hasMessageContaining("read-only")
                .hasMessageContaining("apply")
                .satisfies(e -> {
                    final ResourceException re = (ResourceException) e;
                    assertThat(re.getStatusCode()).isEqualTo(METHOD_NOT_ALLOWED);
                });
    }

    @Test
    void shouldReturnCorrectApiPath() {
        assertThat(manager.getApiPath()).isEqualTo("/api/v1");
    }

    static class TestReadOnlyResourceManager extends AbstractReadOnlyResourceManager<TestResource> {
        TestReadOnlyResourceManager(final KubernetesClient client) {
            super(client, TestResource.class, "/api/v1");
        }

        @Override
        protected String getResourceTypePlural() {
            return "testresources";
        }
    }

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TestResource extends AbstractResource {
        public TestResource() {
            super("v1", "TestResource", null);
        }

        static Builder builder() {
            return new Builder();
        }

        static class Builder {
            private String name;
            private String namespace;

            Builder name(final String name) {
                this.name = name;
                return this;
            }

            Builder namespace(final String namespace) {
                this.namespace = namespace;
                return this;
            }

            TestResource build() {
                final TestResource resource = new TestResource();
                if (name != null || namespace != null) {
                    final Metadata metadata = Metadata.builder()
                            .name(name)
                            .namespace(namespace)
                            .build();
                    resource.setMetadata(metadata);
                }
                return resource;
            }
        }
    }
}
