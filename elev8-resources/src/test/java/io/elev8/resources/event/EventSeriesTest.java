package io.elev8.resources.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class EventSeriesTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Test
    void shouldBuildEventSeriesWithAllFields() {
        final Instant now = Instant.now();
        final EventSeries series = EventSeries.builder()
                .count(5)
                .lastObservedTime(now)
                .build();

        assertThat(series.getCount()).isEqualTo(5);
        assertThat(series.getLastObservedTime()).isEqualTo(now);
    }

    @Test
    void shouldBuildEventSeriesWithCountOnly() {
        final EventSeries series = EventSeries.builder()
                .count(3)
                .build();

        assertThat(series.getCount()).isEqualTo(3);
        assertThat(series.getLastObservedTime()).isNull();
    }

    @Test
    void shouldBuildEventSeriesWithLastObservedTimeOnly() {
        final Instant now = Instant.now();
        final EventSeries series = EventSeries.builder()
                .lastObservedTime(now)
                .build();

        assertThat(series.getCount()).isNull();
        assertThat(series.getLastObservedTime()).isEqualTo(now);
    }

    @Test
    void shouldBuildEmptyEventSeries() {
        final EventSeries series = EventSeries.builder().build();

        assertThat(series.getCount()).isNull();
        assertThat(series.getLastObservedTime()).isNull();
    }

    @Test
    void shouldSerializeToJson() throws JsonProcessingException {
        final Instant now = Instant.parse("2025-01-15T10:30:00Z");
        final EventSeries series = EventSeries.builder()
                .count(10)
                .lastObservedTime(now)
                .build();

        final String json = objectMapper.writeValueAsString(series);

        assertThat(json).contains("\"count\":10");
        assertThat(json).contains("\"lastObservedTime\"");
    }

    @Test
    void shouldNotSerializeNullFieldsToJson() throws JsonProcessingException {
        final EventSeries series = EventSeries.builder()
                .count(1)
                .build();

        final String json = objectMapper.writeValueAsString(series);

        assertThat(json).contains("\"count\":1");
        assertThat(json).doesNotContain("\"lastObservedTime\"");
    }

    @Test
    void shouldDeserializeFromJson() throws JsonProcessingException {
        final String json = "{\"count\":7,\"lastObservedTime\":\"2025-01-15T12:00:00Z\"}";

        final EventSeries series = objectMapper.readValue(json, EventSeries.class);

        assertThat(series.getCount()).isEqualTo(7);
        assertThat(series.getLastObservedTime()).isEqualTo(Instant.parse("2025-01-15T12:00:00Z"));
    }
}
