# Elev8 - EKS-Native Kubernetes Java Client

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/io.elev8/elev8-parent.svg)](https://search.maven.org/search?q=g:io.elev8)

A lightweight, EKS-optimized Kubernetes Java client that eliminates configuration complexity by providing native support for AWS IAM authentication, EKS Access Entries, and IRSA - without requiring kubectl or complex kubeconfig management.

## Why Elev8?

Existing Kubernetes Java clients (fabric8, official client) struggle with EKS authentication:

- **Complex IAM Authentication**: Difficult to configure AWS IAM roles and temporary credentials
- **Manual Token Management**: No automatic token refresh or credential management
- **Heavy Dependencies**: Large dependency trees causing version conflicts
- **Missing EKS Features**: No support for modern EKS features like Access Entries API

Elev8 solves these problems with:

✅ Zero kubeconfig configuration needed
✅ Native EKS Access Entries API support
✅ Automatic IAM token generation and refresh
✅ Minimal dependencies
✅ EKS-first design with expansion path
✅ Clear documentation for EKS scenarios

## Features

### Authentication Modes

- **IAM Roles**: Automatic STS token generation with AWS Signature V4
- **OIDC/IRSA**: Service account web identity token exchange for pods
- **EKS Access Entries**: Native integration with EKS Access Entries API (all 3 auth modes)
- **Service Account Tokens**: Traditional Kubernetes token-based auth

### Core Capabilities

- **Fluent API**: Clean, type-safe builder pattern for client configuration
- **Auto-Discovery**: Automatic cluster endpoint and CA certificate retrieval
- **Extensible**: Plugin architecture for custom resources and extensions
- **Multi-Cluster**: Simple configuration for multiple EKS clusters
- **Minimal Dependencies**: Only AWS SDK 2.x, Jackson, SLF4J, and OkHttp

## Prerequisites

### System Requirements

- **Operating System**: macOS, Linux, or Windows
- **Java**: 17 or later
- **Maven**: 3.8 or later
- **AWS Account**: With EKS cluster access
- **AWS Credentials**: Configured via AWS CLI or environment variables

### Installation

#### macOS (via Homebrew)

**Install Java 17:**
```bash
brew install openjdk@17

# Add to your ~/.zshrc or ~/.bash_profile
export JAVA_HOME="/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"

# Reload shell configuration
source ~/.zshrc
```

**Install Maven:**
```bash
brew install maven

# Verify installation
mvn -version
java -version
```

#### Linux (Ubuntu/Debian)

```bash
# Install Java 17
sudo apt update
sudo apt install openjdk-17-jdk

# Install Maven
sudo apt install maven

# Verify installation
mvn -version
java -version
```

#### Windows

1. Download and install [OpenJDK 17](https://adoptium.net/)
2. Download and install [Maven](https://maven.apache.org/download.cgi)
3. Add JAVA_HOME and Maven to your PATH environment variables

### AWS Setup

**Configure AWS Credentials:**
```bash
# Option 1: AWS CLI
aws configure

# Option 2: Environment Variables
export AWS_ACCESS_KEY_ID=your_access_key
export AWS_SECRET_ACCESS_KEY=your_secret_key
export AWS_REGION=us-east-1
```

**Verify EKS Access:**
```bash
aws eks list-clusters --region us-east-1
aws eks describe-cluster --name your-cluster --region us-east-1
```

## Quick Start

### Maven Dependency

```xml
<dependency>
    <groupId>io.elev8</groupId>
    <artifactId>elev8-eks</artifactId>
    <version>0.1.0</version>
</dependency>
```

### Basic Usage

```java
import io.elev8.eks.EksClient;
import io.elev8.resources.pod.Pod;

import java.util.List;

// Create client with automatic IAM authentication
final final EksClient client = EksClient.builder()
    .region("us-east-1")
    .cluster("my-cluster")
    .iamAuth()  // Auto-discovers credentials from environment
    .build();

// List pods in a namespace
final List<Pod> pods = client.pods().list("default");

// Get a specific pod
final final Pod pod = client.pods().get("default", "my-pod");

// Create a pod
final Pod newPod = Pod.builder()
    .name("nginx")
    .namespace("default")
    .spec(PodSpec.builder()
        .addContainer(Container.builder()
            .name("nginx")
            .image("nginx:latest")
            .build())
        .build())
    .build();

client.pods().create(newPod);
```

### IAM Authentication Examples

#### From EC2 Instance

```java
// Uses instance profile credentials automatically
final EksClient client = EksClient.builder()
    .region("us-east-1")
    .cluster("my-cluster")
    .iamAuth()
    .build();
```

#### From Lambda Function

```java
// Uses Lambda execution role automatically
final EksClient client = EksClient.builder()
    .region(System.getenv("AWS_REGION"))
    .cluster(System.getenv("EKS_CLUSTER_NAME"))
    .iamAuth()
    .build();
```

#### With Assume Role

```java
final EksClient client = EksClient.builder()
    .region("us-east-1")
    .cluster("my-cluster")
    .iamAuth(auth -> auth
        .assumeRole("arn:aws:iam::123456789012:role/EksAdmin")
        .sessionName("my-session"))
    .build();
```

#### With Specific Credentials

```java
final AwsCredentialsProvider credentialsProvider =
    StaticCredentialsProvider.create(
        AwsBasicCredentials.create(accessKey, secretKey));

final EksClient client = EksClient.builder()
    .region("us-east-1")
    .cluster("my-cluster")
    .iamAuth(auth -> auth
        .credentialsProvider(credentialsProvider))
    .build();
```

### OIDC/IRSA Authentication

For pods running in EKS with IAM Roles for Service Accounts:

```java
final EksClient client = EksClient.builder()
    .region("us-east-1")
    .cluster("my-cluster")
    .oidcAuth()  // Auto-discovers web identity token
    .build();
```

### EKS Access Entries

```java
import io.elev8.auth.accessentries.AccessEntry;
import io.elev8.auth.accessentries.AccessEntryManager;

// Create access entry manager
final AccessEntryManager manager = client.accessEntries();

// Create an access entry
final AccessEntry entry = AccessEntry.builder()
    .principalArn("arn:aws:iam::123456789012:role/Developer")
    .kubernetesGroups(List.of("developers"))
    .type("STANDARD")
    .build();

manager.create(entry);

// List access entries
final List<AccessEntry> entries = manager.list();

// Migrate from aws-auth ConfigMap
manager.migrateFromConfigMap();
```

## Authentication Modes Comparison

| Feature | IAM Auth | OIDC/IRSA | Access Entries | Token |
|---------|----------|-----------|----------------|-------|
| Zero Config | ✅ | ✅ | ⚠️ | ❌ |
| Auto Refresh | ✅ | ✅ | ✅ | ❌ |
| Cross-Account | ✅ | ✅ | ✅ | ❌ |
| No aws-auth CM | ❌ | ❌ | ✅ | ❌ |
| Future-Proof | ⚠️ | ⚠️ | ✅ | ❌ |

**Recommendation**: Use **Access Entries** for new clusters, **IAM Auth** for existing clusters.

## Project Structure

```
elev8/
├── elev8-core/              # Core abstractions and client framework
├── elev8-auth-iam/          # AWS IAM authentication
├── elev8-auth-oidc/         # OIDC/IRSA authentication
├── elev8-auth-accessentries/# EKS Access Entries API
├── elev8-auth-token/        # Service account tokens
├── elev8-resources/         # Kubernetes resource plugins
├── elev8-eks/               # EKS-specific features and client
└── examples/                # Usage examples
```

## Building from Source

```bash
# Clone the repository
git clone https://github.com/Ignoramuss/elev8.git
cd elev8

# Build all modules
mvn clean install

# Run tests
mvn test

# Build without tests
mvn clean install -DskipTests

# Build a specific module
cd elev8-core
mvn clean install
```

## Logging

Elev8 uses **SLF4J** as a logging facade, following best practices for open source Java libraries. This means you have complete control over the logging implementation.

### Adding a Logging Implementation

**Elev8 only depends on slf4j-api**. You must add your own SLF4J implementation:

**Option 1: Logback (Recommended)**
```xml
<dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-classic</artifactId>
    <version>1.5.17</version>
</dependency>
```

**Option 2: Log4j 2**
```xml
<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-slf4j2-impl</artifactId>
    <version>2.24.3</version>
</dependency>
```

**Option 3: Simple Logger (for testing)**
```xml
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-simple</artifactId>
    <version>2.0.16</version>
</dependency>
```

### Configuring Logback

Create `src/main/resources/logback.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <!-- Set Elev8 logging level -->
    <logger name="io.elev8" level="INFO"/>

    <!-- Set AWS SDK logging level (reduce noise) -->
    <logger name="software.amazon.awssdk" level="WARN"/>

    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
    </root>
</configuration>
```

### Log Levels

- **TRACE**: Very detailed debugging (token generation details)
- **DEBUG**: IAM authentication flow, HTTP requests
- **INFO**: Client initialization, resource operations
- **WARN**: Deprecated features, potential issues
- **ERROR**: Authentication failures, API errors

## Examples

See the [examples](examples/) directory for comprehensive examples:

- [IAM Authentication from EC2](examples/src/main/java/io/elev8/examples/IamAuthEc2Example.java)
- [IAM Authentication from Lambda](examples/src/main/java/io/elev8/examples/IamAuthLambdaExample.java)
- [OIDC/IRSA Authentication](examples/src/main/java/io/elev8/examples/OidcAuthExample.java)
- [EKS Access Entries Management](examples/src/main/java/io/elev8/examples/AccessEntriesExample.java)
- [Multi-Cluster Setup](examples/src/main/java/io/elev8/examples/MultiClusterExample.java)
- [Custom Resource Definitions](examples/src/main/java/io/elev8/examples/CustomResourceExample.java)

## Migration from fabric8

Elev8 provides a familiar API for fabric8 users:

```java
// fabric8
Config config = new ConfigBuilder()
    .withMasterUrl(url)
    .withOauthToken(token)
    .build();
KubernetesClient client = new KubernetesClientBuilder()
    .withConfig(config)
    .build();

// Elev8
final EksClient client = EksClient.builder()
    .region("us-east-1")
    .cluster("my-cluster")
    .iamAuth()  // No manual token management!
    .build();
```

## kubectl Command Equivalents

Elev8 provides type-safe Java alternatives to common kubectl commands:

### Pod Operations

| kubectl Command | Elev8 Equivalent |
|----------------|------------------|
| `kubectl get pods -n default` | `client.pods().list("default")` |
| `kubectl get pods --all-namespaces` | `client.pods().listAllNamespaces()` |
| `kubectl get pod my-pod -n default` | `client.pods().get("default", "my-pod")` |
| `kubectl get pod my-pod -n default -o json` | `final Pod pod = client.pods().get("default", "my-pod");<br>String json = pod.toJson();` |
| `kubectl create -f pod.yaml` | `Pod pod = Pod.builder()...build();<br>client.pods().create(pod);` |
| `kubectl delete pod my-pod -n default` | `client.pods().delete("default", "my-pod")` |
| `kubectl describe pod my-pod` | `final Pod pod = client.pods().get("default", "my-pod");<br>// Access pod.getStatus(), pod.getSpec()` |

### Service Operations

| kubectl Command | Elev8 Equivalent |
|----------------|------------------|
| `kubectl get services -n default` | `client.services().list("default")` |
| `kubectl get svc my-service -n default` | `client.services().get("default", "my-service")` |
| `kubectl create -f service.yaml` | `Service svc = Service.builder()...build();<br>client.services().create(svc);` |
| `kubectl delete service my-service` | `client.services().delete("default", "my-service")` |
| `kubectl expose deployment my-deploy --port=80` | `Service svc = Service.builder()<br>  .name("my-deploy")<br>  .spec(ServiceSpec.builder()<br>    .addSelector("app", "my-deploy")<br>    .addPort(80, 8080)<br>    .build())<br>  .build();<br>client.services().create(svc);` |

### Deployment Operations

| kubectl Command | Elev8 Equivalent |
|----------------|------------------|
| `kubectl get deployments -n default` | `client.deployments().list("default")` |
| `kubectl get deployment my-deploy -n default` | `client.deployments().get("default", "my-deploy")` |
| `kubectl create -f deployment.yaml` | `Deployment deploy = Deployment.builder()...build();<br>client.deployments().create(deploy);` |
| `kubectl apply -f deployment.yaml` | `Deployment deploy = ...; // modified deployment<br>client.deployments().update(deploy);` |
| `kubectl delete deployment my-deploy` | `client.deployments().delete("default", "my-deploy")` |
| `kubectl scale deployment my-deploy --replicas=5` | `final Deployment d = client.deployments().get("default", "my-deploy");<br>d.getSpec().setReplicas(5);<br>client.deployments().update(d);` |
| `kubectl rollout restart deployment/my-deploy` | `final Deployment d = client.deployments().get("default", "my-deploy");<br>// Add/update annotation to trigger restart<br>d.getMetadata().getAnnotations().put("kubectl.kubernetes.io/restartedAt", Instant.now().toString());<br>client.deployments().update(d);` |

### Complete Example: Creating a Deployment

**kubectl:**
```bash
kubectl create deployment nginx --image=nginx:1.21 --replicas=3 -n default
kubectl expose deployment nginx --port=80 --target-port=80
```

**Elev8:**
```java
// Create deployment
final Deployment deployment = Deployment.builder()
    .name("nginx")
    .namespace("default")
    .spec(DeploymentSpec.builder()
        .replicas(3)
        .addSelector("app", "nginx")
        .template(PodTemplateSpec.builder()
            .label("app", "nginx")
            .spec(PodSpec.builder()
                .addContainer(Container.builder()
                    .name("nginx")
                    .image("nginx:1.21")
                    .addPort(80)
                    .build())
                .build())
            .build())
        .build())
    .build();

client.deployments().create(deployment);

// Create service
final Service service = Service.builder()
    .name("nginx")
    .namespace("default")
    .spec(ServiceSpec.builder()
        .addSelector("app", "nginx")
        .addPort(80, 80)
        .type("ClusterIP")
        .build())
    .build();

client.services().create(service);
```

### Key Advantages Over kubectl

- **Type Safety**: Compile-time validation of all fields
- **IDE Support**: Auto-completion and inline documentation
- **Programmatic Control**: Full control flow and error handling
- **Integration**: Seamlessly integrates with Java applications
- **No External Dependencies**: No need to install kubectl
- **Consistent API**: Same pattern for all resource types

## Contributing

Contributions are welcome! Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details.

## License

Apache License 2.0 - see [LICENSE](LICENSE) for details.

## Support

- [GitHub Issues](https://github.com/yourusername/elev8/issues)
- [Documentation](https://github.com/yourusername/elev8/wiki)
- [Examples](examples/)

## Roadmap

### Completed

- [x] IAM authentication with automatic token refresh
- [x] Core Kubernetes client framework with HTTP abstraction
- [x] EKS cluster auto-discovery (endpoint and CA certificate)
- [x] Core resources with full CRUD (Pod, Service, Deployment)
- [x] Type-safe builder pattern for all resources
- [x] JSON serialization with Jackson
- [x] Resource manager architecture
- [x] Fluent API integration with EksClient

### In Progress

- [ ] OIDC/IRSA authentication support
- [ ] EKS Access Entries API integration
- [ ] Comprehensive unit test coverage

### Planned

- [ ] ConfigMap and Secret resources
- [ ] Additional resources (StatefulSet, DaemonSet, Job, CronJob)
- [ ] Watch/stream support for resource updates
- [ ] Namespace resource support
- [ ] Event streaming and logging
- [ ] CRD support with code generation
- [ ] GKE/AKS support for multi-cloud
- [ ] Reactive API support (Project Reactor)
- [ ] Maven Central publication
