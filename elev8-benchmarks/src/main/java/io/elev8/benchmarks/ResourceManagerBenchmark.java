package io.elev8.benchmarks;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import io.elev8.core.list.ListOptions;
import io.elev8.core.selector.LabelSelectorQuery;
import io.elev8.resources.Metadata;
import io.elev8.resources.ResourceList;
import io.elev8.resources.pod.Container;
import io.elev8.resources.pod.Pod;
import io.elev8.resources.pod.PodSpec;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@BenchmarkMode({Mode.Throughput, Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
@State(Scope.Benchmark)
public class ResourceManagerBenchmark {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<ResourceList<Pod>> POD_LIST_TYPE = new TypeReference<>() {};

    @Getter private String podListJson;

    @Setup
    public void setup() throws JsonProcessingException {
        final List<Pod> pods = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            pods.add(Pod.builder()
                    .metadata(Metadata.builder()
                            .name("pod-" + i)
                            .namespace("default")
                            .labels(Map.of("app", "bench", "index", String.valueOf(i)))
                            .build())
                    .spec(PodSpec.builder()
                            .container(Container.builder()
                                    .name("main")
                                    .image("nginx:latest")
                                    .build())
                            .build())
                    .build());
        }
        final var resourceList = new ResourceList<Pod>();
        resourceList.setItems(pods);
        resourceList.setApiVersion("v1");
        resourceList.setKind("PodList");
        podListJson = OBJECT_MAPPER.writeValueAsString(resourceList);
    }

    @Benchmark
    public void buildListOptions(final Blackhole bh) {
        bh.consume(ListOptions.builder()
                .labelSelector("app=benchmark,env=prod")
                .fieldSelector("status.phase=Running")
                .limit(100)
                .build());
    }

    @Benchmark
    public void buildLabelSelectorQuery(final Blackhole bh) {
        bh.consume(LabelSelectorQuery.builder()
                .equals("app", "benchmark")
                .notEquals("env", "dev")
                .in("tier", "frontend", "backend")
                .exists("version")
                .build()
                .toQueryString());
    }

    @Benchmark
    public void deserializePodList(final Blackhole bh) throws JsonProcessingException {
        bh.consume(OBJECT_MAPPER.readValue(podListJson, POD_LIST_TYPE));
    }

    public static void main(final String[] args) throws RunnerException {
        final Options opt = new OptionsBuilder()
                .include(ResourceManagerBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
