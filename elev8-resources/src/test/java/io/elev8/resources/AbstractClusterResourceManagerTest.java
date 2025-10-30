package io.elev8.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.core.client.KubernetesClient;
import io.elev8.core.client.KubernetesClientException;
import io.elev8.core.http.HttpResponse;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AbstractClusterResourceManagerTest {

    private KubernetesClient mockClient;
    private TestClusterResourceManager manager;

    @BeforeEach
    void setUp() {
        mockClient = mock(KubernetesClient.class);
        manager = new TestClusterResourceManager(mockClient);
    }

    @Test
    void shouldBuildCorrectClusterPath() {
        final String path = manager.buildClusterPath();
        assertThat(path).isEqualTo("/api/v1/testresources");
    }

    @Test
    void shouldBuildCorrectResourcePath() {
        final String path = manager.buildResourcePath("my-resource");
        assertThat(path).isEqualTo("/api/v1/testresources/my-resource");
    }

    @Test
    void shouldCallCorrectPathForList() throws Exception {
        final HttpResponse mockResponse = mock(HttpResponse.class);
        when(mockResponse.isSuccessful()).thenReturn(false);
        when(mockResponse.getStatusCode()).thenReturn(500);
        when(mockResponse.getBody()).thenReturn("Error");
        when(mockClient.get("/api/v1/testresources")).thenReturn(mockResponse);

        assertThatThrownBy(() -> manager.list())
                .isInstanceOf(ResourceException.class);

        verify(mockClient).get("/api/v1/testresources");
    }

    @Test
    void shouldGetClusterResource() throws Exception {
        final String resourceJson = """
                {
                  "apiVersion": "v1",
                  "kind": "TestResource",
                  "metadata": {
                    "name": "my-resource"
                  }
                }
                """;

        final HttpResponse mockResponse = mock(HttpResponse.class);
        when(mockResponse.isSuccessful()).thenReturn(true);
        when(mockResponse.getBody()).thenReturn(resourceJson);
        when(mockClient.get("/api/v1/testresources/my-resource")).thenReturn(mockResponse);

        final TestResource resource = manager.get("my-resource");

        assertThat(resource).isNotNull();
        assertThat(resource.getName()).isEqualTo("my-resource");
        verify(mockClient).get("/api/v1/testresources/my-resource");
    }

    @Test
    void shouldThrowExceptionWhenResourceNotFound() throws Exception {
        final HttpResponse mockResponse = mock(HttpResponse.class);
        when(mockResponse.isNotFound()).thenReturn(true);
        when(mockClient.get(anyString())).thenReturn(mockResponse);

        assertThatThrownBy(() -> manager.get("nonexistent"))
                .isInstanceOf(ResourceException.class)
                .hasMessageContaining("Resource not found");
    }

    @Test
    void shouldCreateClusterResource() throws Exception {
        final TestResource resource = TestResource.builder()
                .name("new-resource")
                .build();

        final String createdJson = """
                {
                  "apiVersion": "v1",
                  "kind": "TestResource",
                  "metadata": {
                    "name": "new-resource"
                  }
                }
                """;

        final HttpResponse mockResponse = mock(HttpResponse.class);
        when(mockResponse.isSuccessful()).thenReturn(true);
        when(mockResponse.getBody()).thenReturn(createdJson);
        when(mockClient.post(eq("/api/v1/testresources"), anyString())).thenReturn(mockResponse);

        final TestResource created = manager.create(resource);

        assertThat(created).isNotNull();
        assertThat(created.getName()).isEqualTo("new-resource");

        final ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockClient).post(pathCaptor.capture(), bodyCaptor.capture());

        assertThat(pathCaptor.getValue()).isEqualTo("/api/v1/testresources");
    }

    @Test
    void shouldUpdateClusterResource() throws Exception {
        final TestResource resource = TestResource.builder()
                .name("existing-resource")
                .build();

        final String updatedJson = """
                {
                  "apiVersion": "v1",
                  "kind": "TestResource",
                  "metadata": {
                    "name": "existing-resource"
                  }
                }
                """;

        final HttpResponse mockResponse = mock(HttpResponse.class);
        when(mockResponse.isSuccessful()).thenReturn(true);
        when(mockResponse.getBody()).thenReturn(updatedJson);
        when(mockClient.put(eq("/api/v1/testresources/existing-resource"), anyString()))
                .thenReturn(mockResponse);

        final TestResource updated = manager.update(resource);

        assertThat(updated).isNotNull();
        assertThat(updated.getName()).isEqualTo("existing-resource");
        verify(mockClient).put(eq("/api/v1/testresources/existing-resource"), anyString());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingResourceWithoutName() {
        final TestResource resource = TestResource.builder().build();

        assertThatThrownBy(() -> manager.update(resource))
                .isInstanceOf(ResourceException.class)
                .hasMessageContaining("Resource name is required for update");
    }

    @Test
    void shouldDeleteClusterResource() throws Exception {
        final HttpResponse mockResponse = mock(HttpResponse.class);
        when(mockResponse.isSuccessful()).thenReturn(true);
        when(mockClient.delete("/api/v1/testresources/my-resource")).thenReturn(mockResponse);

        manager.delete("my-resource");

        verify(mockClient).delete("/api/v1/testresources/my-resource");
    }

    @Test
    void shouldHandleDeleteOfNonexistentResource() throws Exception {
        final HttpResponse mockResponse = mock(HttpResponse.class);
        when(mockResponse.isSuccessful()).thenReturn(false);
        when(mockResponse.isNotFound()).thenReturn(true);
        when(mockClient.delete(anyString())).thenReturn(mockResponse);

        // Should not throw exception for 404
        manager.delete("nonexistent");

        verify(mockClient).delete("/api/v1/testresources/nonexistent");
    }

    @Test
    void shouldThrowExceptionWhenListFails() throws Exception {
        when(mockClient.get(anyString())).thenThrow(new KubernetesClientException("Connection error"));

        assertThatThrownBy(() -> manager.list())
                .isInstanceOf(ResourceException.class)
                .hasMessageContaining("Failed to list resources");
    }

    @Test
    void shouldThrowExceptionWhenGetFails() throws Exception {
        when(mockClient.get(anyString())).thenThrow(new KubernetesClientException("Connection error"));

        assertThatThrownBy(() -> manager.get("resource"))
                .isInstanceOf(ResourceException.class)
                .hasMessageContaining("Failed to get resource");
    }

    @Test
    void shouldThrowExceptionWhenCreateFails() throws Exception {
        final TestResource resource = TestResource.builder().name("test").build();
        when(mockClient.post(anyString(), anyString()))
                .thenThrow(new KubernetesClientException("Connection error"));

        assertThatThrownBy(() -> manager.create(resource))
                .isInstanceOf(ResourceException.class)
                .hasMessageContaining("Failed to create resource");
    }

    @Test
    void shouldThrowExceptionWhenUpdateFails() throws Exception {
        final TestResource resource = TestResource.builder().name("test").build();
        when(mockClient.put(anyString(), anyString()))
                .thenThrow(new KubernetesClientException("Connection error"));

        assertThatThrownBy(() -> manager.update(resource))
                .isInstanceOf(ResourceException.class)
                .hasMessageContaining("Failed to update resource");
    }

    @Test
    void shouldThrowExceptionWhenDeleteFails() throws Exception {
        when(mockClient.delete(anyString()))
                .thenThrow(new KubernetesClientException("Connection error"));

        assertThatThrownBy(() -> manager.delete("resource"))
                .isInstanceOf(ResourceException.class)
                .hasMessageContaining("Failed to delete resource");
    }

    @Test
    void shouldReturnCorrectApiPath() {
        assertThat(manager.getApiPath()).isEqualTo("/api/v1");
    }

    // Test implementation of AbstractClusterResourceManager for testing purposes
    static class TestClusterResourceManager extends AbstractClusterResourceManager<TestResource> {
        TestClusterResourceManager(final KubernetesClient client) {
            super(client, TestResource.class, "/api/v1");
        }

        @Override
        protected String getResourceTypePlural() {
            return "testresources";
        }

        @Override
        public String buildClusterPath() {
            return super.buildClusterPath();
        }

        @Override
        public String buildResourcePath(final String name) {
            return super.buildResourcePath(name);
        }
    }

    // Test resource class for testing purposes
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

            Builder name(final String name) {
                this.name = name;
                return this;
            }

            TestResource build() {
                final TestResource resource = new TestResource();
                if (name != null) {
                    final Metadata metadata = Metadata.builder()
                            .name(name)
                            .build();
                    resource.setMetadata(metadata);
                }
                return resource;
            }
        }
    }
}
