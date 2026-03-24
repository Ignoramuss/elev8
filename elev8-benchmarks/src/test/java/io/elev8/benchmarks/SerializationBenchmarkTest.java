package io.elev8.benchmarks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SerializationBenchmarkTest {

    private final SerializationBenchmark benchmark = new SerializationBenchmark();

    @BeforeEach
    void setUp() {
        benchmark.setup();
    }

    @Test
    void setupCreatesValidPod() {
        assertThat(benchmark.getPod()).isNotNull();
        assertThat(benchmark.getPod().getMetadata().getName()).isEqualTo("benchmark-pod");
    }

    @Test
    void setupCreatesValidDeployment() {
        assertThat(benchmark.getDeployment()).isNotNull();
        assertThat(benchmark.getDeployment().getMetadata().getName()).isEqualTo("benchmark-deploy");
    }

    @Test
    void podSerializesToJson() {
        final String json = benchmark.getPodJson();
        assertThat(json).contains("benchmark-pod");
        assertThat(json).contains("nginx");
    }

    @Test
    void deploymentSerializesToJson() {
        final String json = benchmark.getDeploymentJson();
        assertThat(json).contains("benchmark-deploy");
        assertThat(json).contains("nginx");
    }
}
