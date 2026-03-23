package io.elev8.resources.admissionregistration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RuleWithOperationsTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldBuildWithAllFields() {
        final RuleWithOperations rule = RuleWithOperations.builder()
                .operation("CREATE")
                .operation("UPDATE")
                .apiGroup("")
                .apiVersion("v1")
                .resource("pods")
                .scope("Namespaced")
                .build();

        assertThat(rule.getOperations()).containsExactly("CREATE", "UPDATE");
        assertThat(rule.getApiGroups()).containsExactly("");
        assertThat(rule.getApiVersions()).containsExactly("v1");
        assertThat(rule.getResources()).containsExactly("pods");
        assertThat(rule.getScope()).isEqualTo("Namespaced");
    }

    @Test
    void shouldBuildWithWildcardOperations() {
        final RuleWithOperations rule = RuleWithOperations.builder()
                .operation("*")
                .apiGroup("*")
                .apiVersion("*")
                .resource("*")
                .scope("*")
                .build();

        assertThat(rule.getOperations()).containsExactly("*");
        assertThat(rule.getApiGroups()).containsExactly("*");
        assertThat(rule.getResources()).containsExactly("*");
    }

    @Test
    void shouldSerializeToJson() throws Exception {
        final RuleWithOperations rule = RuleWithOperations.builder()
                .operation("CREATE")
                .apiGroup("apps")
                .apiVersion("v1")
                .resource("deployments")
                .scope("Namespaced")
                .build();

        final String json = objectMapper.writeValueAsString(rule);

        assertThat(json).contains("\"operations\":[\"CREATE\"]");
        assertThat(json).contains("\"apiGroups\":[\"apps\"]");
        assertThat(json).contains("\"resources\":[\"deployments\"]");
        assertThat(json).contains("\"scope\":\"Namespaced\"");
    }

    @Test
    void shouldDeserializeFromJson() throws Exception {
        final String json = """
                {
                  "operations": ["CREATE", "UPDATE"],
                  "apiGroups": [""],
                  "apiVersions": ["v1"],
                  "resources": ["pods"],
                  "scope": "Namespaced"
                }
                """;

        final RuleWithOperations rule = objectMapper.readValue(json, RuleWithOperations.class);

        assertThat(rule.getOperations()).containsExactly("CREATE", "UPDATE");
        assertThat(rule.getApiGroups()).containsExactly("");
        assertThat(rule.getApiVersions()).containsExactly("v1");
        assertThat(rule.getResources()).containsExactly("pods");
        assertThat(rule.getScope()).isEqualTo("Namespaced");
    }

    @Test
    void shouldIncludeEmptyCollectionsWhenNoneAdded() throws Exception {
        final RuleWithOperations rule = RuleWithOperations.builder()
                .operation("DELETE")
                .build();

        final String json = objectMapper.writeValueAsString(rule);

        assertThat(json).contains("\"operations\":[\"DELETE\"]");
        assertThat(json).doesNotContain("scope");
    }
}
