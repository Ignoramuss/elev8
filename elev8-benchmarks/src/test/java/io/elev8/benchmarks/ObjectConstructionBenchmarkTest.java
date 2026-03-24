package io.elev8.benchmarks;

import io.elev8.core.list.ListOptions;
import io.elev8.resources.Metadata;
import io.elev8.resources.pod.Container;
import io.elev8.resources.pod.Pod;
import io.elev8.resources.pod.PodSpec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ObjectConstructionBenchmarkTest {

    @Test
    void buildMetadataProducesValidObject() {
        final var metadata = Metadata.builder()
                .name("test-pod")
                .namespace("default")
                .build();
        assertThat(metadata.getName()).isEqualTo("test-pod");
    }

    @Test
    void buildPodProducesValidObject() {
        final var pod = Pod.builder()
                .metadata(Metadata.builder()
                        .name("test-pod")
                        .namespace("default")
                        .build())
                .spec(PodSpec.builder()
                        .container(Container.builder()
                                .name("main")
                                .image("nginx:latest")
                                .build())
                        .build())
                .build();
        assertThat(pod.getKind()).isEqualTo("Pod");
    }

    @Test
    void buildListOptionsProducesValidObject() {
        final var options = ListOptions.builder()
                .labelSelector("app=test")
                .limit(50)
                .build();
        assertThat(options.getLimit()).isEqualTo(50);
    }
}
