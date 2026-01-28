package io.elev8.core.discovery;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class APIGroupVersionTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldBuildWithGroupVersion() {
        final APIGroupVersion gv = APIGroupVersion.builder()
                .groupVersion("apps/v1")
                .version("v1")
                .build();

        assertThat(gv.getGroupVersion()).isEqualTo("apps/v1");
        assertThat(gv.getVersion()).isEqualTo("v1");
    }

    @Test
    void shouldExtractGroupFromGroupVersion() {
        final APIGroupVersion gv = APIGroupVersion.builder()
                .groupVersion("stable.example.com/v1")
                .version("v1")
                .build();

        assertThat(gv.getGroup()).isEqualTo("stable.example.com");
    }

    @Test
    void shouldReturnEmptyGroupForCoreApi() {
        final APIGroupVersion gv = APIGroupVersion.builder()
                .groupVersion("v1")
                .version("v1")
                .build();

        assertThat(gv.getGroup()).isEmpty();
    }

    @Test
    void shouldReturnEmptyGroupWhenGroupVersionIsNull() {
        final APIGroupVersion gv = APIGroupVersion.builder()
                .version("v1")
                .build();

        assertThat(gv.getGroup()).isEmpty();
    }

    @Test
    void shouldSerializeToJson() throws Exception {
        final APIGroupVersion gv = APIGroupVersion.builder()
                .groupVersion("apps/v1")
                .version("v1")
                .build();

        final String json = objectMapper.writeValueAsString(gv);

        assertThat(json).contains("\"groupVersion\":\"apps/v1\"");
        assertThat(json).contains("\"version\":\"v1\"");
    }

    @Test
    void shouldDeserializeFromJson() throws Exception {
        final String json = "{\"groupVersion\":\"batch/v1\",\"version\":\"v1\"}";

        final APIGroupVersion gv = objectMapper.readValue(json, APIGroupVersion.class);

        assertThat(gv.getGroupVersion()).isEqualTo("batch/v1");
        assertThat(gv.getVersion()).isEqualTo("v1");
    }

    @Test
    void shouldSupportToBuilder() {
        final APIGroupVersion original = APIGroupVersion.builder()
                .groupVersion("apps/v1")
                .version("v1")
                .build();

        final APIGroupVersion modified = original.toBuilder()
                .version("v1beta1")
                .build();

        assertThat(modified.getVersion()).isEqualTo("v1beta1");
        assertThat(original.getVersion()).isEqualTo("v1");
    }
}
