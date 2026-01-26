package io.elev8.resources.crd;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CRDNamesTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldBuildCRDNamesWithRequiredFields() {
        final CRDNames names = CRDNames.builder()
                .kind("CronTab")
                .plural("crontabs")
                .build();

        assertThat(names.getKind()).isEqualTo("CronTab");
        assertThat(names.getPlural()).isEqualTo("crontabs");
    }

    @Test
    void shouldBuildCRDNamesWithAllFields() {
        final CRDNames names = CRDNames.builder()
                .kind("CronTab")
                .plural("crontabs")
                .singular("crontab")
                .listKind("CronTabList")
                .shortName("ct")
                .shortName("cron")
                .category("all")
                .category("cron-resources")
                .build();

        assertThat(names.getKind()).isEqualTo("CronTab");
        assertThat(names.getPlural()).isEqualTo("crontabs");
        assertThat(names.getSingular()).isEqualTo("crontab");
        assertThat(names.getListKind()).isEqualTo("CronTabList");
        assertThat(names.getShortNames()).containsExactly("ct", "cron");
        assertThat(names.getCategories()).containsExactly("all", "cron-resources");
    }

    @Test
    void shouldSerializeToJson() throws Exception {
        final CRDNames names = CRDNames.builder()
                .kind("CronTab")
                .plural("crontabs")
                .singular("crontab")
                .shortName("ct")
                .build();

        final String json = objectMapper.writeValueAsString(names);

        assertThat(json).contains("\"kind\":\"CronTab\"");
        assertThat(json).contains("\"plural\":\"crontabs\"");
        assertThat(json).contains("\"singular\":\"crontab\"");
        assertThat(json).contains("\"shortNames\":[\"ct\"]");
    }

    @Test
    void shouldDeserializeFromJson() throws Exception {
        final String json = """
                {
                  "kind": "CronTab",
                  "plural": "crontabs",
                  "singular": "crontab",
                  "shortNames": ["ct", "cron"],
                  "listKind": "CronTabList",
                  "categories": ["all"]
                }
                """;

        final CRDNames names = objectMapper.readValue(json, CRDNames.class);

        assertThat(names.getKind()).isEqualTo("CronTab");
        assertThat(names.getPlural()).isEqualTo("crontabs");
        assertThat(names.getSingular()).isEqualTo("crontab");
        assertThat(names.getShortNames()).containsExactly("ct", "cron");
        assertThat(names.getListKind()).isEqualTo("CronTabList");
        assertThat(names.getCategories()).containsExactly("all");
    }
}
