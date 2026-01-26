package io.elev8.resources.crd;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.elev8.resources.crd.schema.CustomResourceColumnDefinition;
import io.elev8.resources.crd.schema.CustomResourceSubresources;
import io.elev8.resources.crd.schema.CustomResourceValidation;
import io.elev8.resources.crd.schema.JSONSchemaProps;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CustomResourceDefinitionTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldBuildCRDWithRequiredFields() {
        final CustomResourceDefinition crd = CustomResourceDefinition.builder()
                .name("crontabs.stable.example.com")
                .spec(CustomResourceDefinitionSpec.builder()
                        .group("stable.example.com")
                        .scope("Namespaced")
                        .names(CRDNames.builder()
                                .kind("CronTab")
                                .plural("crontabs")
                                .build())
                        .version(CRDVersion.builder()
                                .name("v1")
                                .served(true)
                                .storage(true)
                                .build())
                        .build())
                .build();

        assertThat(crd.getApiVersion()).isEqualTo("apiextensions.k8s.io/v1");
        assertThat(crd.getKind()).isEqualTo("CustomResourceDefinition");
        assertThat(crd.getName()).isEqualTo("crontabs.stable.example.com");
        assertThat(crd.getSpec().getGroup()).isEqualTo("stable.example.com");
        assertThat(crd.getSpec().getScope()).isEqualTo("Namespaced");
        assertThat(crd.getSpec().getNames().getKind()).isEqualTo("CronTab");
        assertThat(crd.getSpec().getNames().getPlural()).isEqualTo("crontabs");
    }

    @Test
    void shouldBuildCRDWithAllFields() {
        final CustomResourceDefinition crd = CustomResourceDefinition.builder()
                .name("crontabs.stable.example.com")
                .label("app", "crontab-controller")
                .annotation("api.example.com/managed", "true")
                .spec(CustomResourceDefinitionSpec.builder()
                        .group("stable.example.com")
                        .scope("Namespaced")
                        .names(CRDNames.builder()
                                .kind("CronTab")
                                .plural("crontabs")
                                .singular("crontab")
                                .shortName("ct")
                                .category("all")
                                .listKind("CronTabList")
                                .build())
                        .version(CRDVersion.builder()
                                .name("v1")
                                .served(true)
                                .storage(true)
                                .schema(CustomResourceValidation.builder()
                                        .openAPIV3Schema(JSONSchemaProps.builder()
                                                .type("object")
                                                .property("spec", JSONSchemaProps.builder()
                                                        .type("object")
                                                        .property("cronSpec", JSONSchemaProps.builder()
                                                                .type("string")
                                                                .build())
                                                        .property("image", JSONSchemaProps.builder()
                                                                .type("string")
                                                                .build())
                                                        .property("replicas", JSONSchemaProps.builder()
                                                                .type("integer")
                                                                .build())
                                                        .build())
                                                .build())
                                        .build())
                                .subresources(CustomResourceSubresources.builder()
                                        .status(Map.of())
                                        .build())
                                .additionalPrinterColumn(CustomResourceColumnDefinition.builder()
                                        .name("Spec")
                                        .type("string")
                                        .description("The cron spec defining the interval")
                                        .jsonPath(".spec.cronSpec")
                                        .build())
                                .additionalPrinterColumn(CustomResourceColumnDefinition.builder()
                                        .name("Replicas")
                                        .type("integer")
                                        .description("The number of replicas")
                                        .jsonPath(".spec.replicas")
                                        .build())
                                .build())
                        .build())
                .build();

        assertThat(crd.getMetadata().getLabels()).containsEntry("app", "crontab-controller");
        assertThat(crd.getMetadata().getAnnotations()).containsEntry("api.example.com/managed", "true");
        assertThat(crd.getSpec().getNames().getSingular()).isEqualTo("crontab");
        assertThat(crd.getSpec().getNames().getShortNames()).containsExactly("ct");
        assertThat(crd.getSpec().getNames().getCategories()).containsExactly("all");
        assertThat(crd.getSpec().getVersions().get(0).getAdditionalPrinterColumns()).hasSize(2);
    }

    @Test
    void shouldBuildClusterScopedCRD() {
        final CustomResourceDefinition crd = CustomResourceDefinition.builder()
                .name("myresources.example.com")
                .spec(CustomResourceDefinitionSpec.builder()
                        .group("example.com")
                        .scope("Cluster")
                        .names(CRDNames.builder()
                                .kind("MyResource")
                                .plural("myresources")
                                .build())
                        .version(CRDVersion.builder()
                                .name("v1")
                                .served(true)
                                .storage(true)
                                .build())
                        .build())
                .build();

        assertThat(crd.getSpec().getScope()).isEqualTo("Cluster");
    }

    @Test
    void shouldBuildCRDWithMultipleVersions() {
        final CustomResourceDefinition crd = CustomResourceDefinition.builder()
                .name("crontabs.stable.example.com")
                .spec(CustomResourceDefinitionSpec.builder()
                        .group("stable.example.com")
                        .scope("Namespaced")
                        .names(CRDNames.builder()
                                .kind("CronTab")
                                .plural("crontabs")
                                .build())
                        .version(CRDVersion.builder()
                                .name("v1")
                                .served(true)
                                .storage(true)
                                .build())
                        .version(CRDVersion.builder()
                                .name("v1beta1")
                                .served(true)
                                .storage(false)
                                .deprecated(true)
                                .deprecationWarning("v1beta1 is deprecated, use v1")
                                .build())
                        .build())
                .build();

        assertThat(crd.getSpec().getVersions()).hasSize(2);
        assertThat(crd.getSpec().getVersions().get(1).getDeprecated()).isTrue();
        assertThat(crd.getSpec().getVersions().get(1).getDeprecationWarning())
                .isEqualTo("v1beta1 is deprecated, use v1");
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        assertThatThrownBy(() -> CustomResourceDefinition.builder()
                .spec(CustomResourceDefinitionSpec.builder()
                        .group("stable.example.com")
                        .names(CRDNames.builder()
                                .kind("CronTab")
                                .plural("crontabs")
                                .build())
                        .version(CRDVersion.builder()
                                .name("v1")
                                .served(true)
                                .storage(true)
                                .build())
                        .build())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CustomResourceDefinition name is required");
    }

    @Test
    void shouldThrowExceptionWhenSpecIsNull() {
        assertThatThrownBy(() -> CustomResourceDefinition.builder()
                .name("test.example.com")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CustomResourceDefinition spec is required");
    }

    @Test
    void shouldThrowExceptionWhenGroupIsNull() {
        assertThatThrownBy(() -> CustomResourceDefinition.builder()
                .name("test.example.com")
                .spec(CustomResourceDefinitionSpec.builder()
                        .names(CRDNames.builder()
                                .kind("Test")
                                .plural("tests")
                                .build())
                        .version(CRDVersion.builder()
                                .name("v1")
                                .served(true)
                                .storage(true)
                                .build())
                        .build())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CustomResourceDefinition spec.group is required");
    }

    @Test
    void shouldThrowExceptionWhenNamesIsNull() {
        assertThatThrownBy(() -> CustomResourceDefinition.builder()
                .name("test.example.com")
                .spec(CustomResourceDefinitionSpec.builder()
                        .group("example.com")
                        .version(CRDVersion.builder()
                                .name("v1")
                                .served(true)
                                .storage(true)
                                .build())
                        .build())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CustomResourceDefinition spec.names is required");
    }

    @Test
    void shouldThrowExceptionWhenKindIsNull() {
        assertThatThrownBy(() -> CustomResourceDefinition.builder()
                .name("test.example.com")
                .spec(CustomResourceDefinitionSpec.builder()
                        .group("example.com")
                        .names(CRDNames.builder()
                                .plural("tests")
                                .build())
                        .version(CRDVersion.builder()
                                .name("v1")
                                .served(true)
                                .storage(true)
                                .build())
                        .build())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CustomResourceDefinition spec.names.kind is required");
    }

    @Test
    void shouldThrowExceptionWhenPluralIsNull() {
        assertThatThrownBy(() -> CustomResourceDefinition.builder()
                .name("test.example.com")
                .spec(CustomResourceDefinitionSpec.builder()
                        .group("example.com")
                        .names(CRDNames.builder()
                                .kind("Test")
                                .build())
                        .version(CRDVersion.builder()
                                .name("v1")
                                .served(true)
                                .storage(true)
                                .build())
                        .build())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CustomResourceDefinition spec.names.plural is required");
    }

    @Test
    void shouldThrowExceptionWhenVersionsIsEmpty() {
        assertThatThrownBy(() -> CustomResourceDefinition.builder()
                .name("test.example.com")
                .spec(CustomResourceDefinitionSpec.builder()
                        .group("example.com")
                        .names(CRDNames.builder()
                                .kind("Test")
                                .plural("tests")
                                .build())
                        .build())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CustomResourceDefinition spec.versions is required");
    }

    @Test
    void shouldSerializeToJson() throws Exception {
        final CustomResourceDefinition crd = CustomResourceDefinition.builder()
                .name("crontabs.stable.example.com")
                .spec(CustomResourceDefinitionSpec.builder()
                        .group("stable.example.com")
                        .scope("Namespaced")
                        .names(CRDNames.builder()
                                .kind("CronTab")
                                .plural("crontabs")
                                .singular("crontab")
                                .build())
                        .version(CRDVersion.builder()
                                .name("v1")
                                .served(true)
                                .storage(true)
                                .build())
                        .build())
                .build();

        final String json = objectMapper.writeValueAsString(crd);

        assertThat(json).contains("\"apiVersion\":\"apiextensions.k8s.io/v1\"");
        assertThat(json).contains("\"kind\":\"CustomResourceDefinition\"");
        assertThat(json).contains("\"name\":\"crontabs.stable.example.com\"");
        assertThat(json).contains("\"group\":\"stable.example.com\"");
        assertThat(json).contains("\"scope\":\"Namespaced\"");
    }

    @Test
    void shouldDeserializeFromJson() throws Exception {
        final String json = """
                {
                  "apiVersion": "apiextensions.k8s.io/v1",
                  "kind": "CustomResourceDefinition",
                  "metadata": {
                    "name": "crontabs.stable.example.com"
                  },
                  "spec": {
                    "group": "stable.example.com",
                    "scope": "Namespaced",
                    "names": {
                      "kind": "CronTab",
                      "plural": "crontabs",
                      "singular": "crontab",
                      "shortNames": ["ct"]
                    },
                    "versions": [
                      {
                        "name": "v1",
                        "served": true,
                        "storage": true
                      }
                    ]
                  }
                }
                """;

        final CustomResourceDefinition crd = objectMapper.readValue(json, CustomResourceDefinition.class);

        assertThat(crd.getApiVersion()).isEqualTo("apiextensions.k8s.io/v1");
        assertThat(crd.getKind()).isEqualTo("CustomResourceDefinition");
        assertThat(crd.getName()).isEqualTo("crontabs.stable.example.com");
        assertThat(crd.getSpec().getGroup()).isEqualTo("stable.example.com");
        assertThat(crd.getSpec().getScope()).isEqualTo("Namespaced");
        assertThat(crd.getSpec().getNames().getKind()).isEqualTo("CronTab");
        assertThat(crd.getSpec().getNames().getPlural()).isEqualTo("crontabs");
        assertThat(crd.getSpec().getNames().getSingular()).isEqualTo("crontab");
        assertThat(crd.getSpec().getNames().getShortNames()).containsExactly("ct");
        assertThat(crd.getSpec().getVersions()).hasSize(1);
        assertThat(crd.getSpec().getVersions().get(0).getName()).isEqualTo("v1");
    }
}
