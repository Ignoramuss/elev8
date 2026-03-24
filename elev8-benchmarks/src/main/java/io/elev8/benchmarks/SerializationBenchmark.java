package io.elev8.benchmarks;

import lombok.Getter;
import io.elev8.resources.Metadata;
import io.elev8.resources.deployment.Deployment;
import io.elev8.resources.deployment.DeploymentSpec;
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
import org.openjdk.jmh.annotations.Setup;
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
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
@State(Scope.Benchmark)
public class SerializationBenchmark {

    @Getter private Pod pod;
    @Getter private Deployment deployment;
    @Getter private String podJson;
    @Getter private String deploymentJson;

    @Setup
    public void setup() {
        pod = Pod.builder()
                .metadata(Metadata.builder()
                        .name("benchmark-pod")
                        .namespace("default")
                        .labels(Map.of("app", "benchmark", "env", "test"))
                        .annotations(Map.of("description", "benchmark test pod"))
                        .build())
                .spec(PodSpec.builder()
                        .containers(List.of(
                                Container.builder()
                                        .name("main")
                                        .image("nginx:1.25")
                                        .build()))
                        .build())
                .build();

        deployment = Deployment.builder()
                .metadata(Metadata.builder()
                        .name("benchmark-deploy")
                        .namespace("default")
                        .labels(Map.of("app", "benchmark", "version", "v1"))
                        .build())
                .spec(DeploymentSpec.builder()
                        .replicas(3)
                        .selector("app", "benchmark")
                        .template(DeploymentSpec.PodTemplateSpec.builder()
                                .spec(PodSpec.builder()
                                        .container(Container.builder()
                                                .name("main")
                                                .image("nginx:1.25")
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build();

        podJson = pod.toJson();
        deploymentJson = deployment.toJson();
    }

    @Benchmark
    public void serializePod(final Blackhole bh) {
        bh.consume(pod.toJson());
    }

    @Benchmark
    public void deserializePod(final Blackhole bh) {
        bh.consume(Pod.fromJson(podJson, Pod.class));
    }

    @Benchmark
    public void serializeDeployment(final Blackhole bh) {
        bh.consume(deployment.toJson());
    }

    @Benchmark
    public void deserializeDeployment(final Blackhole bh) {
        bh.consume(Deployment.fromJson(deploymentJson, Deployment.class));
    }

    public static void main(final String[] args) throws RunnerException {
        final Options opt = new OptionsBuilder()
                .include(SerializationBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
