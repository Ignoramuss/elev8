# Performance Benchmarks

Elev8 includes JMH (Java Microbenchmark Harness) benchmarks in the `elev8-benchmarks` module.

## Running Benchmarks

### Via Maven

```bash
cd elev8-benchmarks
mvn clean package -DskipTests
java -jar target/benchmarks.jar
```

### Run specific benchmarks

```bash
java -jar target/benchmarks.jar SerializationBenchmark
java -jar target/benchmarks.jar ResourceManagerBenchmark
java -jar target/benchmarks.jar ObjectConstructionBenchmark
```

### Quick smoke run

```bash
java -jar target/benchmarks.jar -wi 1 -i 1 -f 1
```

### List available benchmarks

```bash
java -jar target/benchmarks.jar -l
```

## Benchmark Suite

### SerializationBenchmark

Measures JSON serialization/deserialization throughput for Kubernetes resources:

- `serializePod` - Pod object to JSON string
- `deserializePod` - JSON string to Pod object
- `serializeDeployment` - Deployment object to JSON string
- `deserializeDeployment` - JSON string to Deployment object

### ResourceManagerBenchmark

Measures resource management overhead:

- `buildListOptions` - ListOptions builder construction with selectors
- `buildLabelSelectorQuery` - Type-safe label selector query building
- `deserializePodList` - Deserialize a list of 100 pods from JSON

### ObjectConstructionBenchmark

Measures builder pattern construction throughput:

- `buildMetadata` - Metadata object construction with labels and annotations
- `buildPod` - Full Pod object construction with spec
- `buildListOptions` - ListOptions construction

## Interpreting Results

JMH reports throughput (ops/time) and average time per operation. When comparing results:

- Run benchmarks on the same hardware with minimal background load
- Use the default fork/warmup/measurement settings for reproducible results
- Focus on relative changes between versions rather than absolute numbers
