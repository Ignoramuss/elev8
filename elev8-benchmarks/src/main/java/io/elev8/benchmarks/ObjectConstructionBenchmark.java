package io.elev8.benchmarks;

import io.elev8.core.list.ListOptions;
import io.elev8.resources.Metadata;
import io.elev8.resources.pod.Pod;
import io.elev8.resources.pod.Container;
import io.elev8.resources.pod.PodSpec;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@BenchmarkMode({Mode.Throughput, Mode.AverageTime})
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
@State(Scope.Benchmark)
public class ObjectConstructionBenchmark {

    @Benchmark
    public void buildMetadata(final Blackhole bh) {
        bh.consume(Metadata.builder()
                .name("test-pod")
                .namespace("default")
                .labels(Map.of("app", "test", "env", "prod"))
                .annotations(Map.of("note", "benchmark"))
                .build());
    }

    @Benchmark
    public void buildPod(final Blackhole bh) {
        bh.consume(Pod.builder()
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
                .build());
    }

    @Benchmark
    public void buildListOptions(final Blackhole bh) {
        bh.consume(ListOptions.builder()
                .labelSelector("app=test")
                .fieldSelector("metadata.name=pod-1")
                .limit(50)
                .build());
    }

    public static void main(final String[] args) throws RunnerException {
        final Options opt = new OptionsBuilder()
                .include(ObjectConstructionBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
