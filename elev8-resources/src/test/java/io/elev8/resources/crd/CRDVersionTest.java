package io.elev8.resources.crd;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.elev8.resources.crd.schema.CustomResourceColumnDefinition;
import io.elev8.resources.crd.schema.CustomResourceSubresourceScale;
import io.elev8.resources.crd.schema.CustomResourceSubresources;
import io.elev8.resources.crd.schema.CustomResourceValidation;
import io.elev8.resources.crd.schema.JSONSchemaProps;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CRDVersionTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldBuildCRDVersionWithRequiredFields() {
        final CRDVersion version = CRDVersion.builder()
                .name("v1")
                .served(true)
                .storage(true)
                .build();

        assertThat(version.getName()).isEqualTo("v1");
        assertThat(version.getServed()).isTrue();
        assertThat(version.getStorage()).isTrue();
    }

    @Test
    void shouldBuildCRDVersionWithSchema() {
        final CRDVersion version = CRDVersion.builder()
                .name("v1")
                .served(true)
                .storage(true)
                .schema(CustomResourceValidation.builder()
                        .openAPIV3Schema(JSONSchemaProps.builder()
                                .type("object")
                                .property("spec", JSONSchemaProps.builder()
                                        .type("object")
                                        .property("replicas", JSONSchemaProps.builder()
                                                .type("integer")
                                                .minimum(1)
                                                .maximum(100)
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build();

        assertThat(version.getSchema()).isNotNull();
        assertThat(version.getSchema().getOpenAPIV3Schema().getType()).isEqualTo("object");
        assertThat(version.getSchema().getOpenAPIV3Schema().getProperties()).containsKey("spec");
    }

    @Test
    void shouldBuildCRDVersionWithSubresources() {
        final CRDVersion version = CRDVersion.builder()
                .name("v1")
                .served(true)
                .storage(true)
                .subresources(CustomResourceSubresources.builder()
                        .status(Map.of())
                        .scale(CustomResourceSubresourceScale.builder()
                                .specReplicasPath(".spec.replicas")
                                .statusReplicasPath(".status.replicas")
                                .labelSelectorPath(".status.selector")
                                .build())
                        .build())
                .build();

        assertThat(version.getSubresources()).isNotNull();
        assertThat(version.getSubresources().getStatus()).isNotNull();
        assertThat(version.getSubresources().getScale().getSpecReplicasPath()).isEqualTo(".spec.replicas");
    }

    @Test
    void shouldBuildCRDVersionWithAdditionalPrinterColumns() {
        final CRDVersion version = CRDVersion.builder()
                .name("v1")
                .served(true)
                .storage(true)
                .additionalPrinterColumn(CustomResourceColumnDefinition.builder()
                        .name("Replicas")
                        .type("integer")
                        .description("Number of replicas")
                        .jsonPath(".spec.replicas")
                        .priority(0)
                        .build())
                .additionalPrinterColumn(CustomResourceColumnDefinition.builder()
                        .name("Age")
                        .type("date")
                        .jsonPath(".metadata.creationTimestamp")
                        .build())
                .build();

        assertThat(version.getAdditionalPrinterColumns()).hasSize(2);
        assertThat(version.getAdditionalPrinterColumns().get(0).getName()).isEqualTo("Replicas");
        assertThat(version.getAdditionalPrinterColumns().get(0).getType()).isEqualTo("integer");
        assertThat(version.getAdditionalPrinterColumns().get(1).getName()).isEqualTo("Age");
    }

    @Test
    void shouldBuildDeprecatedCRDVersion() {
        final CRDVersion version = CRDVersion.builder()
                .name("v1beta1")
                .served(true)
                .storage(false)
                .deprecated(true)
                .deprecationWarning("v1beta1 is deprecated, migrate to v1")
                .build();

        assertThat(version.getDeprecated()).isTrue();
        assertThat(version.getDeprecationWarning()).isEqualTo("v1beta1 is deprecated, migrate to v1");
        assertThat(version.getStorage()).isFalse();
    }

    @Test
    void shouldSerializeToJson() throws Exception {
        final CRDVersion version = CRDVersion.builder()
                .name("v1")
                .served(true)
                .storage(true)
                .build();

        final String json = objectMapper.writeValueAsString(version);

        assertThat(json).contains("\"name\":\"v1\"");
        assertThat(json).contains("\"served\":true");
        assertThat(json).contains("\"storage\":true");
    }

    @Test
    void shouldDeserializeFromJson() throws Exception {
        final String json = """
                {
                  "name": "v1",
                  "served": true,
                  "storage": true,
                  "deprecated": false,
                  "additionalPrinterColumns": [
                    {
                      "name": "Replicas",
                      "type": "integer",
                      "jsonPath": ".spec.replicas"
                    }
                  ]
                }
                """;

        final CRDVersion version = objectMapper.readValue(json, CRDVersion.class);

        assertThat(version.getName()).isEqualTo("v1");
        assertThat(version.getServed()).isTrue();
        assertThat(version.getStorage()).isTrue();
        assertThat(version.getDeprecated()).isFalse();
        assertThat(version.getAdditionalPrinterColumns()).hasSize(1);
        assertThat(version.getAdditionalPrinterColumns().get(0).getName()).isEqualTo("Replicas");
    }
}
