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
import io.elev8.core.resources.Pod;

// Create client with automatic IAM authentication
EksClient client = EksClient.builder()
    .region("us-east-1")
    .cluster("my-cluster")
    .iamAuth()  // Auto-discovers credentials from environment
    .build();

// List pods in a namespace
List<Pod> pods = client.pods()
    .namespace("default")
    .list();

// Get a specific pod
Pod pod = client.pods()
    .namespace("default")
    .get("my-pod");

// Create a pod
Pod newPod = Pod.builder()
    .name("nginx")
    .namespace("default")
    .image("nginx:latest")
    .build();

client.pods().create(newPod);
```

### IAM Authentication Examples

#### From EC2 Instance

```java
// Uses instance profile credentials automatically
EksClient client = EksClient.builder()
    .region("us-east-1")
    .cluster("my-cluster")
    .iamAuth()
    .build();
```

#### From Lambda Function

```java
// Uses Lambda execution role automatically
EksClient client = EksClient.builder()
    .region(System.getenv("AWS_REGION"))
    .cluster(System.getenv("EKS_CLUSTER_NAME"))
    .iamAuth()
    .build();
```

#### With Assume Role

```java
EksClient client = EksClient.builder()
    .region("us-east-1")
    .cluster("my-cluster")
    .iamAuth(auth -> auth
        .assumeRole("arn:aws:iam::123456789012:role/EksAdmin")
        .sessionName("my-session"))
    .build();
```

#### With Specific Credentials

```java
AwsCredentialsProvider credentialsProvider =
    StaticCredentialsProvider.create(
        AwsBasicCredentials.create(accessKey, secretKey));

EksClient client = EksClient.builder()
    .region("us-east-1")
    .cluster("my-cluster")
    .iamAuth(auth -> auth
        .credentialsProvider(credentialsProvider))
    .build();
```

### OIDC/IRSA Authentication

For pods running in EKS with IAM Roles for Service Accounts:

```java
EksClient client = EksClient.builder()
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
AccessEntryManager manager = client.accessEntries();

// Create an access entry
AccessEntry entry = AccessEntry.builder()
    .principalArn("arn:aws:iam::123456789012:role/Developer")
    .kubernetesGroups(List.of("developers"))
    .type("STANDARD")
    .build();

manager.create(entry);

// List access entries
List<AccessEntry> entries = manager.list();

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

## Requirements

- Java 17 or later
- AWS SDK for Java 2.x
- Access to an EKS cluster with appropriate IAM permissions

## Building from Source

```bash
git clone https://github.com/yourusername/elev8.git
cd elev8
mvn clean install
```

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
EksClient client = EksClient.builder()
    .region("us-east-1")
    .cluster("my-cluster")
    .iamAuth()  // No manual token management!
    .build();
```

## Contributing

Contributions are welcome! Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details.

## License

Apache License 2.0 - see [LICENSE](LICENSE) for details.

## Support

- [GitHub Issues](https://github.com/yourusername/elev8/issues)
- [Documentation](https://github.com/yourusername/elev8/wiki)
- [Examples](examples/)

## Roadmap

- [x] IAM authentication with automatic token refresh
- [x] EKS Access Entries API integration
- [x] OIDC/IRSA support
- [x] Core resources (Pod, Service, Deployment, ConfigMap)
- [ ] Additional resources (StatefulSet, DaemonSet, Job, CronJob)
- [ ] Watch/stream support for resource updates
- [ ] CRD support with code generation
- [ ] GKE/AKS support
- [ ] Reactive API support
