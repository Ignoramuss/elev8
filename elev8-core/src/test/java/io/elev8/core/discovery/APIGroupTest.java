package io.elev8.core.discovery;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class APIGroupTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldBuildWithVersions() {
        final APIGroupVersion v1 = APIGroupVersion.builder()
                .groupVersion("apps/v1")
                .version("v1")
                .build();
        final APIGroupVersion v1beta1 = APIGroupVersion.builder()
                .groupVersion("apps/v1beta1")
                .version("v1beta1")
                .build();

        final APIGroup group = APIGroup.builder()
                .name("apps")
                .version(v1)
                .version(v1beta1)
                .preferredVersion(v1)
                .build();

        assertThat(group.getName()).isEqualTo("apps");
        assertThat(group.getVersions()).hasSize(2);
        assertThat(group.getPreferredVersion()).isEqualTo(v1);
    }

    @Test
    void shouldReturnPreferredVersionString() {
        final APIGroupVersion preferred = APIGroupVersion.builder()
                .groupVersion("apps/v1")
                .version("v1")
                .build();

        final APIGroup group = APIGroup.builder()
                .name("apps")
                .preferredVersion(preferred)
                .build();

        assertThat(group.getPreferredVersionString()).isEqualTo("v1");
    }

    @Test
    void shouldReturnFirstVersionWhenNoPreferred() {
        final APIGroupVersion v1 = APIGroupVersion.builder()
                .groupVersion("apps/v1")
                .version("v1")
                .build();

        final APIGroup group = APIGroup.builder()
                .name("apps")
                .version(v1)
                .build();

        assertThat(group.getPreferredVersionString()).isEqualTo("v1");
    }

    @Test
    void shouldReturnNullWhenNoVersions() {
        final APIGroup group = APIGroup.builder()
                .name("apps")
                .build();

        assertThat(group.getPreferredVersionString()).isNull();
    }

    @Test
    void shouldReturnPreferredGroupVersion() {
        final APIGroupVersion preferred = APIGroupVersion.builder()
                .groupVersion("stable.example.com/v1")
                .version("v1")
                .build();

        final APIGroup group = APIGroup.builder()
                .name("stable.example.com")
                .preferredVersion(preferred)
                .build();

        assertThat(group.getPreferredGroupVersion()).isEqualTo("stable.example.com/v1");
    }

    @Test
    void shouldSerializeToJson() throws Exception {
        final APIGroupVersion v1 = APIGroupVersion.builder()
                .groupVersion("apps/v1")
                .version("v1")
                .build();

        final APIGroup group = APIGroup.builder()
                .name("apps")
                .version(v1)
                .preferredVersion(v1)
                .build();

        final String json = objectMapper.writeValueAsString(group);

        assertThat(json).contains("\"name\":\"apps\"");
        assertThat(json).contains("\"versions\":");
    }

    @Test
    void shouldDeserializeFromJson() throws Exception {
        final String json = """
                {
                    "name": "apps",
                    "versions": [
                        {"groupVersion": "apps/v1", "version": "v1"}
                    ],
                    "preferredVersion": {"groupVersion": "apps/v1", "version": "v1"}
                }
                """;

        final APIGroup group = objectMapper.readValue(json, APIGroup.class);

        assertThat(group.getName()).isEqualTo("apps");
        assertThat(group.getVersions()).hasSize(1);
        assertThat(group.getPreferredVersion().getVersion()).isEqualTo("v1");
    }
}
