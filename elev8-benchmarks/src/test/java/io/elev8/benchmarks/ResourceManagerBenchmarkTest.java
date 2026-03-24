package io.elev8.benchmarks;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.elev8.core.list.ListOptions;
import io.elev8.core.selector.LabelSelectorQuery;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class ResourceManagerBenchmarkTest {

    private final ResourceManagerBenchmark benchmark = new ResourceManagerBenchmark();

    @Test
    void setupCreatesValidTestData() {
        assertThatCode(benchmark::setup).doesNotThrowAnyException();
    }

    @Test
    void buildListOptionsProducesValidObject() {
        final var options = ListOptions.builder()
                .labelSelector("app=benchmark,env=prod")
                .fieldSelector("status.phase=Running")
                .limit(100)
                .build();
        assertThat(options.getLabelSelector()).isEqualTo("app=benchmark,env=prod");
        assertThat(options.getLimit()).isEqualTo(100);
    }

    @Test
    void buildLabelSelectorQueryProducesValidString() {
        final String selector = LabelSelectorQuery.builder()
                .equals("app", "benchmark")
                .notEquals("env", "dev")
                .in("tier", "frontend", "backend")
                .exists("version")
                .build()
                .toQueryString();
        assertThat(selector).contains("app=benchmark");
    }

    @Test
    void deserializePodListDoesNotThrow() throws JsonProcessingException {
        benchmark.setup();
        assertThat(benchmark.getPodListJson()).contains("pod-0");
    }
}
