package io.elev8.resources.admissionregistration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MatchConditionTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldBuildWithAllFields() {
        final MatchCondition condition = MatchCondition.builder()
                .name("exclude-leases")
                .expression("!(request.resource.group == 'coordination.k8s.io' && request.resource.resource == 'leases')")
                .build();

        assertThat(condition.getName()).isEqualTo("exclude-leases");
        assertThat(condition.getExpression()).contains("coordination.k8s.io");
    }

    @Test
    void shouldSerializeToJson() throws Exception {
        final MatchCondition condition = MatchCondition.builder()
                .name("only-create")
                .expression("request.operation == 'CREATE'")
                .build();

        final String json = objectMapper.writeValueAsString(condition);

        assertThat(json).contains("\"name\":\"only-create\"");
        assertThat(json).contains("\"expression\":\"request.operation == 'CREATE'\"");
    }

    @Test
    void shouldDeserializeFromJson() throws Exception {
        final String json = """
                {
                  "name": "environment-check",
                  "expression": "object.metadata.labels['env'] == 'prod'"
                }
                """;

        final MatchCondition condition = objectMapper.readValue(json, MatchCondition.class);

        assertThat(condition.getName()).isEqualTo("environment-check");
        assertThat(condition.getExpression()).isEqualTo("object.metadata.labels['env'] == 'prod'");
    }

    @Test
    void shouldOmitNullFieldsInJson() throws Exception {
        final MatchCondition condition = MatchCondition.builder()
                .name("test")
                .build();

        final String json = objectMapper.writeValueAsString(condition);

        assertThat(json).contains("\"name\":\"test\"");
        assertThat(json).doesNotContain("expression");
    }
}
