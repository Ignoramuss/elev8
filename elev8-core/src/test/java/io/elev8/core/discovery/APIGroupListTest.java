package io.elev8.core.discovery;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class APIGroupListTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldBuildWithGroups() {
        final APIGroup apps = APIGroup.builder()
                .name("apps")
                .build();
        final APIGroup batch = APIGroup.builder()
                .name("batch")
                .build();

        final APIGroupList list = APIGroupList.builder()
                .apiVersion("v1")
                .kind("APIGroupList")
                .group(apps)
                .group(batch)
                .build();

        assertThat(list.getGroups()).hasSize(2);
    }

    @Test
    void shouldFindGroupByName() {
        final APIGroup apps = APIGroup.builder()
                .name("apps")
                .build();

        final APIGroupList list = APIGroupList.builder()
                .group(apps)
                .build();

        final Optional<APIGroup> found = list.findByName("apps");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("apps");
    }

    @Test
    void shouldReturnEmptyWhenGroupNotFound() {
        final APIGroupList list = APIGroupList.builder().build();

        final Optional<APIGroup> found = list.findByName("apps");

        assertThat(found).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenGroupsNull() {
        final APIGroupList list = APIGroupList.builder().build();

        assertThat(list.getGroups()).isEmpty();
    }

    @Test
    void shouldSerializeToJson() throws Exception {
        final APIGroup apps = APIGroup.builder()
                .name("apps")
                .build();

        final APIGroupList list = APIGroupList.builder()
                .apiVersion("v1")
                .kind("APIGroupList")
                .group(apps)
                .build();

        final String json = objectMapper.writeValueAsString(list);

        assertThat(json).contains("\"apiVersion\":\"v1\"");
        assertThat(json).contains("\"groups\":");
    }

    @Test
    void shouldDeserializeFromJson() throws Exception {
        final String json = """
                {
                    "apiVersion": "v1",
                    "kind": "APIGroupList",
                    "groups": [
                        {
                            "name": "apps",
                            "versions": [
                                {"groupVersion": "apps/v1", "version": "v1"}
                            ],
                            "preferredVersion": {"groupVersion": "apps/v1", "version": "v1"}
                        },
                        {
                            "name": "batch",
                            "versions": [
                                {"groupVersion": "batch/v1", "version": "v1"}
                            ]
                        }
                    ]
                }
                """;

        final APIGroupList list = objectMapper.readValue(json, APIGroupList.class);

        assertThat(list.getGroups()).hasSize(2);
        assertThat(list.findByName("apps")).isPresent();
        assertThat(list.findByName("batch")).isPresent();
    }

    @Test
    void shouldHandleNullGroupsInFindByName() {
        final APIGroupList list = APIGroupList.builder().build();

        assertThat(list.findByName("apps")).isEmpty();
    }
}
