# Elev8 ☁️ - Cloud-Native Kubernetes Java Client

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/io.elev8/elev8-parent.svg)](https://search.maven.org/search?q=g:io.elev8)

A lightweight, cloud-native Kubernetes Java client that eliminates configuration complexity by providing native support for AWS IAM authentication (EKS), GCP authentication (GKE), and Azure authentication (AKS) - without requiring kubectl or complex kubeconfig management.

## Table of Contents

- [Why Elev8?](#why-elev8)
- [Features](#features)
  - [Authentication Modes](#authentication-modes)
  - [Core Capabilities](#core-capabilities)
- [Prerequisites](#prerequisites)
  - [System Requirements](#system-requirements)
  - [Installation](#installation)
  - [AWS Setup](#aws-setup)
- [Quick Start](#quick-start)
  - [Maven Dependency](#maven-dependency)
  - [Basic Usage](#basic-usage)
  - [IAM Authentication Examples](#iam-authentication-examples)
  - [OIDC/IRSA Authentication](#oidcirsa-authentication)
  - [ConfigMaps](#configmaps)
  - [Secrets](#secrets)
  - [DaemonSets](#daemonsets)
  - [Jobs](#jobs)
  - [StatefulSets](#statefulsets)
  - [ReplicaSets](#replicasets)
  - [Ingress](#ingress)
  - [CronJobs](#cronjobs)
  - [Namespaces](#namespaces)
  - [ServiceAccounts](#serviceaccounts)
  - [Roles](#roles)
  - [RoleBindings](#rolebindings)
  - [ClusterRoles](#clusterroles)
  - [ClusterRoleBindings](#clusterrolebindings)
  - [NetworkPolicies](#networkpolicies)
  - [HorizontalPodAutoscalers](#horizontalpodautoscalers)
  - [VerticalPodAutoscalers](#verticalpodautoscalers)
  - [ResourceQuotas](#resourcequotas)
  - [PersistentVolumes](#persistentvolumes)
  - [PersistentVolumeClaims](#persistentvolumeclaims)
  - [EKS Access Entries](#eks-access-entries)
  - [Watch API](#watch-api)
- [Authentication Modes Comparison](#authentication-modes-comparison)
- [Project Structure](#project-structure)
- [Building from Source](#building-from-source)
- [Logging](#logging)
  - [Adding a Logging Implementation](#adding-a-logging-implementation)
  - [Configuring Logback](#configuring-logback)
  - [Log Levels](#log-levels)
- [Examples](#examples)
- [Migration from fabric8](#migration-from-fabric8)
- [kubectl Command Equivalents](#kubectl-command-equivalents)
  - [Pod Operations](#pod-operations)
  - [Service Operations](#service-operations)
  - [Deployment Operations](#deployment-operations)
  - [ConfigMap Operations](#configmap-operations)
  - [Secret Operations](#secret-operations)
  - [DaemonSet Operations](#daemonset-operations)
  - [Job Operations](#job-operations)
  - [StatefulSet Operations](#statefulset-operations)
  - [ReplicaSet Operations](#replicaset-operations)
  - [Ingress Operations](#ingress-operations)
  - [CronJob Operations](#cronjob-operations)
  - [Namespace Operations](#namespace-operations)
  - [ServiceAccount Operations](#serviceaccount-operations)
  - [Role Operations](#role-operations)
  - [RoleBinding Operations](#rolebinding-operations)
  - [ClusterRole Operations](#clusterrole-operations)
  - [ClusterRoleBinding Operations](#clusterrolebinding-operations)
  - [NetworkPolicy Operations](#networkpolicy-operations)
  - [HorizontalPodAutoscaler Operations](#horizontalpodautoscaler-operations)
  - [VerticalPodAutoscaler Operations](#verticalpodautoscaler-operations)
  - [ResourceQuota Operations](#resourcequota-operations)
  - [PersistentVolume Operations](#persistentvolume-operations)
  - [PersistentVolumeClaim Operations](#persistentvolumeclaim-operations)
- [Contributing](#contributing)
- [License](#license)
- [Support](#support)
- [Roadmap](#roadmap)
  - [Completed](#completed)
  - [In Progress](#in-progress)

## Why Elev8?

Existing Kubernetes Java clients (fabric8, official client) struggle with cloud provider authentication:

- **Complex IAM Authentication**: Difficult to configure cloud IAM roles and temporary credentials
- **Manual Token Management**: No automatic token refresh or credential management
- **Heavy Dependencies**: Large dependency trees causing version conflicts
- **Missing Cloud Features**: No support for modern cloud features like EKS Access Entries API

Elev8 solves these problems with:

✅ Zero kubeconfig configuration needed
✅ Native cloud provider API support (EKS Access Entries, GKE, AKS)
✅ Automatic IAM token generation and refresh
✅ Minimal dependencies
✅ Cloud-first design with multi-cloud support (EKS/GKE/AKS)
✅ Clear documentation for cloud provider scenarios

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
final EksClient client = EksClient.builder()
    .clusterName("my-cluster")
    .region("us-east-1")
    .build();

// List pods in a namespace
final List<Pod> pods = client.pods().list("default");

// Get a specific pod
final Pod pod = client.pods().get("default", "my-pod");

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
    .clusterName("my-cluster")
    .region("us-east-1")
    .build();
```

#### From Lambda Function

```java
// Uses Lambda execution role automatically
final EksClient client = EksClient.builder()
    .clusterName(System.getenv("EKS_CLUSTER_NAME"))
    .region(System.getenv("AWS_REGION"))
    .build();
```

#### With Assume Role

```java
final EksClient client = EksClient.builder()
    .clusterName("my-cluster")
    .region("us-east-1")
    .roleArn("arn:aws:iam::123456789012:role/EksAdmin")
    .sessionName("my-session")
    .build();
```

#### With Specific Credentials

```java
final AwsCredentialsProvider credentialsProvider =
    StaticCredentialsProvider.create(
        AwsBasicCredentials.create(accessKey, secretKey));

final EksClient client = EksClient.builder()
    .clusterName("my-cluster")
    .region("us-east-1")
    .baseCredentialsProvider(credentialsProvider)
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

### ConfigMaps

```java
import io.elev8.resources.configmap.ConfigMap;

// Create a ConfigMap with data
final ConfigMap configMap = ConfigMap.builder()
    .name("app-config")
    .namespace("default")
    .addData("database.url", "jdbc:postgresql://localhost:5432/mydb")
    .addData("database.user", "admin")
    .addData("app.environment", "production")
    .build();

client.configMaps().create(configMap);

// Get a ConfigMap
final ConfigMap retrieved = client.configMaps().get("default", "app-config");

// List ConfigMaps in namespace
final List<ConfigMap> configMaps = client.configMaps().list("default");

// Create immutable ConfigMap
final ConfigMap immutableConfig = ConfigMap.builder()
    .name("constants")
    .namespace("default")
    .addData("version", "1.0.0")
    .immutable(true)
    .build();
```

### Secrets

```java
import io.elev8.resources.secret.Secret;

// Create a basic authentication secret
final Secret basicAuth = Secret.builder()
    .name("database-credentials")
    .namespace("default")
    .basicAuth("admin", "password123")
    .build();

client.secrets().create(basicAuth);

// Create a TLS secret
final Secret tlsSecret = Secret.builder()
    .name("tls-cert")
    .namespace("default")
    .tls("base64-encoded-cert", "base64-encoded-key")
    .build();

// Create an opaque secret with custom data
final Secret apiSecret = Secret.builder()
    .name("api-keys")
    .namespace("default")
    .addStringData("api-key", "my-secret-key")
    .addStringData("api-secret", "my-secret-value")
    .build();

// Create Docker registry secret
final Secret dockerSecret = Secret.builder()
    .name("docker-registry")
    .namespace("default")
    .dockerConfigJson("base64-encoded-docker-config")
    .build();

// Get a Secret
final Secret retrieved = client.secrets().get("default", "api-keys");

// List Secrets in namespace
final List<Secret> secrets = client.secrets().list("default");
```

### DaemonSets

```java
import io.elev8.resources.daemonset.DaemonSet;
import io.elev8.resources.daemonset.DaemonSetSpec;

// Create a DaemonSet for log collection on all nodes
final DaemonSet logCollector = DaemonSet.builder()
    .name("fluentd-elasticsearch")
    .namespace("kube-system")
    .spec(DaemonSetSpec.builder()
        .selector("app", "fluentd")
        .template(DaemonSetPodTemplateSpec.builder()
            .label("app", "fluentd")
            .spec(PodSpec.builder()
                .container(Container.builder()
                    .name("fluentd")
                    .image("fluent/fluentd-kubernetes-daemonset:v1.14-debian-elasticsearch7")
                    .build())
                .build())
            .build())
        .build())
    .build();

client.daemonSets().create(logCollector);

// Create a DaemonSet with custom update strategy
final DaemonSet nodeExporter = DaemonSet.builder()
    .name("node-exporter")
    .namespace("monitoring")
    .spec(DaemonSetSpec.builder()
        .selector("app", "node-exporter")
        .updateStrategy("OnDelete")
        .minReadySeconds(30)
        .template(DaemonSetPodTemplateSpec.builder()
            .label("app", "node-exporter")
            .spec(PodSpec.builder()
                .container(Container.builder()
                    .name("node-exporter")
                    .image("prom/node-exporter:latest")
                    .build())
                .build())
            .build())
        .build())
    .build();

client.daemonSets().create(nodeExporter);

// Get a DaemonSet
final DaemonSet retrieved = client.daemonSets().get("kube-system", "fluentd-elasticsearch");

// List DaemonSets in namespace
final List<DaemonSet> daemonSets = client.daemonSets().list("kube-system");

// Delete a DaemonSet
client.daemonSets().delete("kube-system", "fluentd-elasticsearch");
```

### Jobs

```java
import io.elev8.resources.job.Job;
import io.elev8.resources.job.JobSpec;

// Create a simple batch Job
final Job batchJob = Job.builder()
    .name("pi-calculation")
    .namespace("default")
    .spec(JobSpec.builder()
        .completions(1)
        .template(JobPodTemplateSpec.builder()
            .spec(PodSpec.builder()
                .container(Container.builder()
                    .name("pi")
                    .image("perl:5.34")
                    .command(List.of("perl", "-Mbignum=bpi", "-wle", "print bpi(2000)"))
                    .build())
                .restartPolicy("Never")
                .build())
            .build())
        .build())
    .build();

client.jobs().create(batchJob);

// Create a parallel processing Job
final Job parallelJob = Job.builder()
    .name("data-processor")
    .namespace("default")
    .spec(JobSpec.builder()
        .completions(10)
        .parallelism(3)
        .backoffLimit(4)
        .template(JobPodTemplateSpec.builder()
            .label("app", "processor")
            .spec(PodSpec.builder()
                .container(Container.builder()
                    .name("processor")
                    .image("data-processor:latest")
                    .build())
                .restartPolicy("OnFailure")
                .build())
            .build())
        .build())
    .build();

client.jobs().create(parallelJob);

// Create a Job with timeout and TTL
final Job timedJob = Job.builder()
    .name("cleanup-job")
    .namespace("default")
    .spec(JobSpec.builder()
        .activeDeadlineSeconds(300L)
        .ttlSecondsAfterFinished(100)
        .template(JobPodTemplateSpec.builder()
            .spec(PodSpec.builder()
                .container(Container.builder()
                    .name("cleanup")
                    .image("cleanup:latest")
                    .build())
                .restartPolicy("Never")
                .build())
            .build())
        .build())
    .build();

client.jobs().create(timedJob);

// Get a Job
final Job retrieved = client.jobs().get("default", "pi-calculation");

// List Jobs in namespace
final List<Job> jobs = client.jobs().list("default");

// Delete a Job
client.jobs().delete("default", "pi-calculation");
```

### StatefulSets

```java
import io.elev8.resources.statefulset.StatefulSet;
import io.elev8.resources.statefulset.StatefulSetSpec;

// Create a simple StatefulSet for a web application
final StatefulSet webApp = StatefulSet.builder()
    .name("web")
    .namespace("default")
    .spec(StatefulSetSpec.builder()
        .serviceName("nginx")
        .replicas(3)
        .selector("app", "nginx")
        .template(StatefulSetPodTemplateSpec.builder()
            .label("app", "nginx")
            .spec(PodSpec.builder()
                .container(Container.builder()
                    .name("nginx")
                    .image("nginx:1.21")
                    .addPort(80)
                    .build())
                .build())
            .build())
        .build())
    .build();

client.statefulSets().create(webApp);

// Create a StatefulSet with custom update strategy
final StatefulSet database = StatefulSet.builder()
    .name("postgres")
    .namespace("default")
    .spec(StatefulSetSpec.builder()
        .serviceName("postgres")
        .replicas(3)
        .selector("app", "postgres")
        .updateStrategy("OnDelete")
        .podManagementPolicy("Parallel")
        .revisionHistoryLimit(5)
        .minReadySeconds(10)
        .template(StatefulSetPodTemplateSpec.builder()
            .label("app", "postgres")
            .spec(PodSpec.builder()
                .container(Container.builder()
                    .name("postgres")
                    .image("postgres:14")
                    .build())
                .build())
            .build())
        .build())
    .build();

client.statefulSets().create(database);

// Get a StatefulSet
final StatefulSet retrieved = client.statefulSets().get("default", "web");

// List StatefulSets in namespace
final List<StatefulSet> statefulSets = client.statefulSets().list("default");

// Delete a StatefulSet
client.statefulSets().delete("default", "web");
```

### ReplicaSets

```java
import io.elev8.resources.replicaset.ReplicaSet;
import io.elev8.resources.replicaset.ReplicaSetSpec;
import io.elev8.resources.replicaset.ReplicaSetPodTemplateSpec;

// Create a simple ReplicaSet
final ReplicaSet replicaSet = ReplicaSet.builder()
    .name("nginx-replicaset")
    .namespace("default")
    .spec(ReplicaSetSpec.builder()
        .replicas(3)
        .selector("app", "nginx")
        .template(ReplicaSetPodTemplateSpec.builder()
            .label("app", "nginx")
            .spec(PodSpec.builder()
                .container(Container.builder()
                    .name("nginx")
                    .image("nginx:1.21")
                    .addPort(80)
                    .build())
                .build())
            .build())
        .build())
    .build();

client.replicaSets().create(replicaSet);

// Create a ReplicaSet with minReadySeconds
final ReplicaSet frontendReplicaSet = ReplicaSet.builder()
    .name("frontend")
    .namespace("default")
    .label("tier", "frontend")
    .spec(ReplicaSetSpec.builder()
        .replicas(5)
        .selector("app", "frontend")
        .selector("tier", "frontend")
        .minReadySeconds(30)
        .template(ReplicaSetPodTemplateSpec.builder()
            .label("app", "frontend")
            .label("tier", "frontend")
            .spec(PodSpec.builder()
                .container(Container.builder()
                    .name("frontend")
                    .image("frontend:v1")
                    .build())
                .build())
            .build())
        .build())
    .build();

client.replicaSets().create(frontendReplicaSet);

// Get a ReplicaSet
final ReplicaSet retrieved = client.replicaSets().get("default", "nginx-replicaset");

// List ReplicaSets in namespace
final List<ReplicaSet> replicaSets = client.replicaSets().list("default");

// Delete a ReplicaSet
client.replicaSets().delete("default", "nginx-replicaset");
```

### Ingress

```java
import io.elev8.resources.ingress.Ingress;
import io.elev8.resources.ingress.IngressSpec;
import io.elev8.resources.ingress.IngressRule;
import io.elev8.resources.ingress.HTTPIngressRuleValue;
import io.elev8.resources.ingress.HTTPIngressPath;
import io.elev8.resources.ingress.IngressBackend;
import io.elev8.resources.ingress.IngressServiceBackend;
import io.elev8.resources.ingress.ServiceBackendPort;
import io.elev8.resources.ingress.IngressTLS;

// Create a simple HTTP Ingress
final Ingress simpleIngress = Ingress.builder()
    .name("example-ingress")
    .namespace("default")
    .spec(IngressSpec.builder()
        .ingressClassName("nginx")
        .rule(IngressRule.builder()
            .host("example.com")
            .http(HTTPIngressRuleValue.builder()
                .path(HTTPIngressPath.builder()
                    .path("/")
                    .pathType("Prefix")
                    .backend(IngressBackend.builder()
                        .service(IngressServiceBackend.builder()
                            .name("web-service")
                            .port(ServiceBackendPort.builder()
                                .number(80)
                                .build())
                            .build())
                        .build())
                    .build())
                .build())
            .build())
        .build())
    .build();

client.ingresses().create(simpleIngress);

// Create an Ingress with multiple paths and TLS
final Ingress tlsIngress = Ingress.builder()
    .name("app-ingress")
    .namespace("default")
    .label("app", "web")
    .spec(IngressSpec.builder()
        .ingressClassName("nginx")
        .tl(IngressTLS.builder()
            .host("app.example.com")
            .secretName("app-tls-secret")
            .build())
        .rule(IngressRule.builder()
            .host("app.example.com")
            .http(HTTPIngressRuleValue.builder()
                .path(HTTPIngressPath.builder()
                    .path("/api")
                    .pathType("Prefix")
                    .backend(IngressBackend.builder()
                        .service(IngressServiceBackend.builder()
                            .name("api-service")
                            .port(ServiceBackendPort.builder()
                                .number(8080)
                                .build())
                            .build())
                        .build())
                    .build())
                .path(HTTPIngressPath.builder()
                    .path("/web")
                    .pathType("Prefix")
                    .backend(IngressBackend.builder()
                        .service(IngressServiceBackend.builder()
                            .name("web-service")
                            .port(ServiceBackendPort.builder()
                                .number(80)
                                .build())
                            .build())
                        .build())
                    .build())
                .build())
            .build())
        .build())
    .build();

client.ingresses().create(tlsIngress);

// Get an Ingress
final Ingress retrieved = client.ingresses().get("default", "example-ingress");

// Check load balancer status
if (retrieved.getStatus() != null && retrieved.getStatus().getLoadBalancer() != null) {
    retrieved.getStatus().getLoadBalancer().getIngress().forEach(lb -> {
        System.out.println("Load Balancer: " +
            (lb.getHostname() != null ? lb.getHostname() : lb.getIp()));
    });
}

// List Ingresses in namespace
final List<Ingress> ingresses = client.ingresses().list("default");

// Delete an Ingress
client.ingresses().delete("default", "example-ingress");
```

**kubectl equivalents:**
```bash
# Create Ingress
kubectl apply -f ingress.yaml

# Get Ingress
kubectl get ingress example-ingress

# Describe Ingress (shows load balancer status)
kubectl describe ingress example-ingress

# Delete Ingress
kubectl delete ingress example-ingress
```

### CronJobs

```java
import io.elev8.resources.cronjob.CronJob;
import io.elev8.resources.cronjob.CronJobSpec;
import io.elev8.resources.cronjob.CronJobJobTemplateSpec;

// Create a simple CronJob that runs every 5 minutes
final CronJob helloCron = CronJob.builder()
    .name("hello")
    .namespace("default")
    .spec(CronJobSpec.builder()
        .schedule("*/5 * * * *")
        .jobTemplate(CronJobJobTemplateSpec.builder()
            .spec(JobSpec.builder()
                .template(JobPodTemplateSpec.builder()
                    .spec(PodSpec.builder()
                        .container(Container.builder()
                            .name("hello")
                            .image("busybox:latest")
                            .command(List.of("/bin/sh", "-c", "date; echo Hello from Kubernetes"))
                            .build())
                        .restartPolicy("OnFailure")
                        .build())
                    .build())
                .build())
            .build())
        .build())
    .build();

client.cronJobs().create(helloCron);

// Create a CronJob with custom settings
final CronJob backupJob = CronJob.builder()
    .name("database-backup")
    .namespace("default")
    .spec(CronJobSpec.builder()
        .schedule("0 2 * * *")
        .concurrencyPolicy("Forbid")
        .successfulJobsHistoryLimit(5)
        .failedJobsHistoryLimit(3)
        .startingDeadlineSeconds(300L)
        .suspend(false)
        .jobTemplate(CronJobJobTemplateSpec.builder()
            .label("app", "backup")
            .spec(JobSpec.builder()
                .template(JobPodTemplateSpec.builder()
                    .spec(PodSpec.builder()
                        .container(Container.builder()
                            .name("backup")
                            .image("backup-tool:latest")
                            .build())
                        .restartPolicy("Never")
                        .build())
                    .build())
                .build())
            .build())
        .build())
    .build();

client.cronJobs().create(backupJob);

// Get a CronJob
final CronJob retrieved = client.cronJobs().get("default", "hello");

// List CronJobs in namespace
final List<CronJob> cronJobs = client.cronJobs().list("default");

// Delete a CronJob
client.cronJobs().delete("default", "hello");
```

### Namespaces

```java
import io.elev8.resources.namespace.Namespace;
import io.elev8.resources.namespace.NamespaceSpec;

// Create a simple Namespace
final Namespace namespace = Namespace.builder()
    .name("production")
    .build();

client.namespaces().create(namespace);

// Create a Namespace with labels
final Namespace devNamespace = Namespace.builder()
    .name("development")
    .label("env", "dev")
    .label("team", "backend")
    .build();

client.namespaces().create(devNamespace);

// Create a Namespace with finalizers
final Namespace protectedNamespace = Namespace.builder()
    .name("protected")
    .label("environment", "production")
    .addFinalizer("kubernetes")
    .addFinalizer("example.com/custom-finalizer")
    .build();

client.namespaces().create(protectedNamespace);

// List all Namespaces
final List<Namespace> namespaces = client.namespaces().listAllNamespaces();

// Get a specific Namespace
final Namespace retrieved = client.namespaces().get("production");

// Delete a Namespace
client.namespaces().delete("development");
```

### ServiceAccounts

```java
import io.elev8.resources.serviceaccount.ServiceAccount;
import io.elev8.resources.serviceaccount.ServiceAccountSpec;
import io.elev8.resources.LocalObjectReference;

// Create a simple ServiceAccount
final ServiceAccount serviceAccount = ServiceAccount.builder()
    .name("my-service-account")
    .namespace("default")
    .build();

client.serviceAccounts().create(serviceAccount);

// Create a ServiceAccount with automount disabled
final ServiceAccount noAutomount = ServiceAccount.builder()
    .name("no-automount-sa")
    .namespace("default")
    .spec(ServiceAccountSpec.builder()
        .automountServiceAccountToken(false)
        .build())
    .build();

client.serviceAccounts().create(noAutomount);

// Create a ServiceAccount with image pull secrets
final ServiceAccount withImagePullSecrets = ServiceAccount.builder()
    .name("docker-sa")
    .namespace("default")
    .label("app", "backend")
    .spec(ServiceAccountSpec.builder()
        .automountServiceAccountToken(true)
        .imagePullSecret(LocalObjectReference.builder()
            .name("docker-registry-secret")
            .build())
        .imagePullSecret(LocalObjectReference.builder()
            .name("ghcr-secret")
            .build())
        .build())
    .build();

client.serviceAccounts().create(withImagePullSecrets);

// Get a ServiceAccount
final ServiceAccount retrieved = client.serviceAccounts().get("default", "my-service-account");

// List ServiceAccounts in a namespace
final List<ServiceAccount> serviceAccounts = client.serviceAccounts().list("default");

// Delete a ServiceAccount
client.serviceAccounts().delete("default", "my-service-account");
```

**kubectl equivalents:**
```bash
# Create ServiceAccount
kubectl apply -f serviceaccount.yaml

# Get ServiceAccount
kubectl get serviceaccount my-service-account -n default

# Describe ServiceAccount (shows secrets and tokens)
kubectl describe serviceaccount my-service-account -n default

# Delete ServiceAccount
kubectl delete serviceaccount my-service-account -n default
```

### Roles

```java
import io.elev8.resources.role.Role;
import io.elev8.resources.role.RoleSpec;
import io.elev8.resources.role.PolicyRule;

// Create a Role for reading pods
final Role podReader = Role.builder()
    .name("pod-reader")
    .namespace("default")
    .spec(RoleSpec.builder()
        .rule(PolicyRule.builder()
            .apiGroup("")
            .resource("pods")
            .verb("get")
            .verb("list")
            .verb("watch")
            .build())
        .build())
    .build();

client.roles().create(podReader);

// Create a Role with multiple resource permissions
final Role developerRole = Role.builder()
    .name("developer")
    .namespace("default")
    .label("team", "backend")
    .spec(RoleSpec.builder()
        .rule(PolicyRule.builder()
            .apiGroup("")
            .resource("pods")
            .resource("services")
            .verb("get")
            .verb("list")
            .verb("watch")
            .build())
        .rule(PolicyRule.builder()
            .apiGroup("apps")
            .resource("deployments")
            .verb("get")
            .verb("list")
            .verb("watch")
            .build())
        .build())
    .build();

client.roles().create(developerRole);

// Create a Role with specific resource names
final Role secretReader = Role.builder()
    .name("secret-reader")
    .namespace("default")
    .spec(RoleSpec.builder()
        .rule(PolicyRule.builder()
            .apiGroup("")
            .resource("secrets")
            .verb("get")
            .resourceName("app-config")
            .resourceName("database-credentials")
            .build())
        .build())
    .build();

client.roles().create(secretReader);

// Create an admin Role with wildcard permissions
final Role namespaceAdmin = Role.builder()
    .name("namespace-admin")
    .namespace("production")
    .spec(RoleSpec.builder()
        .rule(PolicyRule.builder()
            .apiGroup("*")
            .resource("*")
            .verb("*")
            .build())
        .build())
    .build();

client.roles().create(namespaceAdmin);

// Get a Role
final Role retrieved = client.roles().get("default", "pod-reader");

// List Roles in a namespace
final List<Role> roles = client.roles().list("default");

// Delete a Role
client.roles().delete("default", "pod-reader");
```

**kubectl equivalents:**
```bash
# Create Role
kubectl apply -f role.yaml

# Get Role
kubectl get role pod-reader -n default

# Describe Role (shows policy rules)
kubectl describe role pod-reader -n default

# Delete Role
kubectl delete role pod-reader -n default
```

### RoleBindings

```java
import io.elev8.resources.rolebinding.RoleBinding;
import io.elev8.resources.rolebinding.RoleBindingSpec;
import io.elev8.resources.rolebinding.Subject;
import io.elev8.resources.rolebinding.RoleRef;

// Bind a Role to a ServiceAccount
final RoleBinding podReaderBinding = RoleBinding.builder()
    .name("read-pods")
    .namespace("default")
    .spec(RoleBindingSpec.builder()
        .subject(Subject.builder()
            .kind("ServiceAccount")
            .name("my-service-account")
            .namespace("default")
            .build())
        .roleRef(RoleRef.builder()
            .apiGroup("rbac.authorization.k8s.io")
            .kind("Role")
            .name("pod-reader")
            .build())
        .build())
    .build();

client.roleBindings().create(podReaderBinding);

// Bind a Role to a User
final RoleBinding userBinding = RoleBinding.builder()
    .name("developer-binding")
    .namespace("default")
    .spec(RoleBindingSpec.builder()
        .subject(Subject.builder()
            .kind("User")
            .name("jane@example.com")
            .apiGroup("rbac.authorization.k8s.io")
            .build())
        .roleRef(RoleRef.builder()
            .apiGroup("rbac.authorization.k8s.io")
            .kind("Role")
            .name("developer")
            .build())
        .build())
    .build();

client.roleBindings().create(userBinding);

// Bind a Role to a Group
final RoleBinding groupBinding = RoleBinding.builder()
    .name("team-binding")
    .namespace("production")
    .spec(RoleBindingSpec.builder()
        .subject(Subject.builder()
            .kind("Group")
            .name("developers")
            .apiGroup("rbac.authorization.k8s.io")
            .build())
        .roleRef(RoleRef.builder()
            .apiGroup("rbac.authorization.k8s.io")
            .kind("Role")
            .name("namespace-admin")
            .build())
        .build())
    .build();

client.roleBindings().create(groupBinding);

// Bind a ClusterRole to subjects in a namespace (grants namespace-scoped permissions)
final RoleBinding viewBinding = RoleBinding.builder()
    .name("view-binding")
    .namespace("default")
    .spec(RoleBindingSpec.builder()
        .subject(Subject.builder()
            .kind("ServiceAccount")
            .name("viewer-sa")
            .namespace("default")
            .build())
        .roleRef(RoleRef.builder()
            .apiGroup("rbac.authorization.k8s.io")
            .kind("ClusterRole")
            .name("view")
            .build())
        .build())
    .build();

client.roleBindings().create(viewBinding);

// Bind to multiple subjects
final RoleBinding multiSubjectBinding = RoleBinding.builder()
    .name("multi-reader")
    .namespace("default")
    .spec(RoleBindingSpec.builder()
        .subject(Subject.builder()
            .kind("User")
            .name("alice@example.com")
            .apiGroup("rbac.authorization.k8s.io")
            .build())
        .subject(Subject.builder()
            .kind("User")
            .name("bob@example.com")
            .apiGroup("rbac.authorization.k8s.io")
            .build())
        .subject(Subject.builder()
            .kind("ServiceAccount")
            .name("app-sa")
            .namespace("default")
            .build())
        .roleRef(RoleRef.builder()
            .apiGroup("rbac.authorization.k8s.io")
            .kind("Role")
            .name("pod-reader")
            .build())
        .build())
    .build();

client.roleBindings().create(multiSubjectBinding);

// Get a RoleBinding
final RoleBinding retrieved = client.roleBindings().get("default", "read-pods");

// List RoleBindings in a namespace
final List<RoleBinding> roleBindings = client.roleBindings().list("default");

// Delete a RoleBinding
client.roleBindings().delete("default", "read-pods");
```

**kubectl equivalents:**
```bash
# Create RoleBinding
kubectl apply -f rolebinding.yaml

# Get RoleBinding
kubectl get rolebinding read-pods -n default

# Describe RoleBinding (shows subjects and role reference)
kubectl describe rolebinding read-pods -n default

# Delete RoleBinding
kubectl delete rolebinding read-pods -n default

# Create RoleBinding using kubectl create
kubectl create rolebinding read-pods \
  --role=pod-reader \
  --serviceaccount=default:my-service-account \
  -n default
```

### ClusterRoles

```java
import io.elev8.resources.clusterrole.ClusterRole;
import io.elev8.resources.role.RoleSpec;
import io.elev8.resources.role.PolicyRule;

// Create a ClusterRole for reading pods cluster-wide
final ClusterRole podReader = ClusterRole.builder()
    .name("pod-reader")
    .spec(RoleSpec.builder()
        .rule(PolicyRule.builder()
            .apiGroup("")
            .resource("pods")
            .verb("get")
            .verb("list")
            .verb("watch")
            .build())
        .build())
    .build();

client.clusterRoles().create(podReader);

// Create a ClusterRole with multiple resource permissions
final ClusterRole adminRole = ClusterRole.builder()
    .name("resource-admin")
    .label("type", "admin")
    .spec(RoleSpec.builder()
        .rule(PolicyRule.builder()
            .apiGroup("")
            .resource("pods")
            .resource("services")
            .resource("configmaps")
            .verb("get")
            .verb("list")
            .verb("watch")
            .verb("create")
            .verb("update")
            .verb("delete")
            .build())
        .rule(PolicyRule.builder()
            .apiGroup("apps")
            .resource("deployments")
            .resource("statefulsets")
            .verb("*")
            .build())
        .build())
    .build();

client.clusterRoles().create(adminRole);

// Create a ClusterRole with specific resource names
final ClusterRole secretReader = ClusterRole.builder()
    .name("specific-secret-reader")
    .spec(RoleSpec.builder()
        .rule(PolicyRule.builder()
            .apiGroup("")
            .resource("secrets")
            .verb("get")
            .resourceName("important-secret")
            .resourceName("database-credentials")
            .build())
        .build())
    .build();

client.clusterRoles().create(secretReader);

// Create a cluster-admin ClusterRole with wildcard permissions
final ClusterRole clusterAdmin = ClusterRole.builder()
    .name("cluster-admin")
    .spec(RoleSpec.builder()
        .rule(PolicyRule.builder()
            .apiGroup("*")
            .resource("*")
            .verb("*")
            .build())
        .build())
    .build();

client.clusterRoles().create(clusterAdmin);

// Get a ClusterRole (no namespace needed - cluster-scoped)
final ClusterRole retrieved = client.clusterRoles().get("pod-reader");

// List all ClusterRoles
final List<ClusterRole> clusterRoles = client.clusterRoles().list();

// Delete a ClusterRole
client.clusterRoles().delete("pod-reader");
```

**kubectl equivalents:**
```bash
# Create ClusterRole
kubectl apply -f clusterrole.yaml

# Get ClusterRole (no namespace needed)
kubectl get clusterrole pod-reader

# Describe ClusterRole (shows policy rules)
kubectl describe clusterrole pod-reader

# Delete ClusterRole
kubectl delete clusterrole pod-reader

# Create ClusterRole using kubectl create
kubectl create clusterrole pod-reader \
  --verb=get,list,watch \
  --resource=pods
```

### ClusterRoleBindings

```java
import io.elev8.resources.clusterrolebinding.ClusterRoleBinding;
import io.elev8.resources.clusterrolebinding.ClusterRoleBindingSpec;
import io.elev8.resources.rolebinding.Subject;
import io.elev8.resources.rolebinding.RoleRef;

// Bind a ClusterRole to a user cluster-wide
final ClusterRoleBinding adminBinding = ClusterRoleBinding.builder()
    .name("cluster-admin-binding")
    .spec(ClusterRoleBindingSpec.builder()
        .subject(Subject.builder()
            .kind("User")
            .name("admin@example.com")
            .apiGroup("rbac.authorization.k8s.io")
            .build())
        .roleRef(RoleRef.builder()
            .apiGroup("rbac.authorization.k8s.io")
            .kind("ClusterRole")
            .name("cluster-admin")
            .build())
        .build())
    .build();

client.clusterRoleBindings().create(adminBinding);

// Bind a ClusterRole to a ServiceAccount cluster-wide
final ClusterRoleBinding saBinding = ClusterRoleBinding.builder()
    .name("system-sa-binding")
    .spec(ClusterRoleBindingSpec.builder()
        .subject(Subject.builder()
            .kind("ServiceAccount")
            .name("system-service-account")
            .namespace("kube-system")
            .build())
        .roleRef(RoleRef.builder()
            .apiGroup("rbac.authorization.k8s.io")
            .kind("ClusterRole")
            .name("cluster-admin")
            .build())
        .build())
    .build();

client.clusterRoleBindings().create(saBinding);

// Bind a ClusterRole to a Group
final ClusterRoleBinding groupBinding = ClusterRoleBinding.builder()
    .name("developers-view")
    .label("team", "developers")
    .spec(ClusterRoleBindingSpec.builder()
        .subject(Subject.builder()
            .kind("Group")
            .name("developers")
            .apiGroup("rbac.authorization.k8s.io")
            .build())
        .roleRef(RoleRef.builder()
            .apiGroup("rbac.authorization.k8s.io")
            .kind("ClusterRole")
            .name("view")
            .build())
        .build())
    .build();

client.clusterRoleBindings().create(groupBinding);

// Bind to multiple subjects cluster-wide
final ClusterRoleBinding multiBinding = ClusterRoleBinding.builder()
    .name("multi-admin-binding")
    .spec(ClusterRoleBindingSpec.builder()
        .subject(Subject.builder()
            .kind("User")
            .name("alice@example.com")
            .apiGroup("rbac.authorization.k8s.io")
            .build())
        .subject(Subject.builder()
            .kind("User")
            .name("bob@example.com")
            .apiGroup("rbac.authorization.k8s.io")
            .build())
        .subject(Subject.builder()
            .kind("Group")
            .name("system:masters")
            .apiGroup("rbac.authorization.k8s.io")
            .build())
        .roleRef(RoleRef.builder()
            .apiGroup("rbac.authorization.k8s.io")
            .kind("ClusterRole")
            .name("cluster-admin")
            .build())
        .build())
    .build();

client.clusterRoleBindings().create(multiBinding);

// Get a ClusterRoleBinding (no namespace needed - cluster-scoped)
final ClusterRoleBinding retrieved = client.clusterRoleBindings().get("cluster-admin-binding");

// List all ClusterRoleBindings
final List<ClusterRoleBinding> bindings = client.clusterRoleBindings().list();

// Delete a ClusterRoleBinding
client.clusterRoleBindings().delete("cluster-admin-binding");
```

**kubectl equivalents:**
```bash
# Create ClusterRoleBinding
kubectl apply -f clusterrolebinding.yaml

# Get ClusterRoleBinding (no namespace needed)
kubectl get clusterrolebinding cluster-admin-binding

# Describe ClusterRoleBinding (shows subjects and role reference)
kubectl describe clusterrolebinding cluster-admin-binding

# Delete ClusterRoleBinding
kubectl delete clusterrolebinding cluster-admin-binding

# Create ClusterRoleBinding using kubectl create
kubectl create clusterrolebinding cluster-admin-binding \
  --clusterrole=cluster-admin \
  --user=admin@example.com

# Create ClusterRoleBinding for a ServiceAccount
kubectl create clusterrolebinding system-sa-binding \
  --clusterrole=cluster-admin \
  --serviceaccount=kube-system:system-service-account
```

### NetworkPolicies

```java
import io.elev8.resources.networkpolicy.NetworkPolicy;
import io.elev8.resources.networkpolicy.NetworkPolicySpec;
import io.elev8.resources.networkpolicy.NetworkPolicyIngressRule;
import io.elev8.resources.networkpolicy.NetworkPolicyEgressRule;
import io.elev8.resources.networkpolicy.NetworkPolicyPeer;
import io.elev8.resources.networkpolicy.NetworkPolicyPort;
import io.elev8.resources.networkpolicy.IPBlock;
import io.elev8.resources.LabelSelector;

// Create a deny-all ingress policy (baseline security)
final NetworkPolicy denyAll = NetworkPolicy.builder()
    .name("deny-all-ingress")
    .namespace("default")
    .spec(NetworkPolicySpec.builder()
        .podSelector(LabelSelector.builder().build())  // Empty selector = all pods
        .policyType("Ingress")
        .build())
    .build();

client.networkPolicies().create(denyAll);

// Create a NetworkPolicy allowing database access from specific pods
final NetworkPolicy allowDbAccess = NetworkPolicy.builder()
    .name("allow-db-access")
    .namespace("default")
    .spec(NetworkPolicySpec.builder()
        .podSelector(LabelSelector.builder()
            .matchLabel("role", "db")
            .build())
        .policyType("Ingress")
        .ingressRule(NetworkPolicyIngressRule.builder()
            .from(NetworkPolicyPeer.builder()
                .podSelector(LabelSelector.builder()
                    .matchLabel("role", "frontend")
                    .build())
                .build())
            .port(NetworkPolicyPort.builder()
                .protocol("TCP")
                .port(5432)
                .build())
            .build())
        .build())
    .build();

client.networkPolicies().create(allowDbAccess);

// Create a NetworkPolicy with namespace selector
final NetworkPolicy allowFromProd = NetworkPolicy.builder()
    .name("allow-from-production")
    .namespace("default")
    .spec(NetworkPolicySpec.builder()
        .podSelector(LabelSelector.builder()
            .matchLabel("app", "backend")
            .build())
        .policyType("Ingress")
        .ingressRule(NetworkPolicyIngressRule.builder()
            .from(NetworkPolicyPeer.builder()
                .namespaceSelector(LabelSelector.builder()
                    .matchLabel("environment", "production")
                    .build())
                .build())
            .build())
        .build())
    .build();

client.networkPolicies().create(allowFromProd);

// Create a NetworkPolicy with IP block (allow specific CIDR)
final NetworkPolicy allowExternalCidr = NetworkPolicy.builder()
    .name("allow-external")
    .namespace("default")
    .spec(NetworkPolicySpec.builder()
        .podSelector(LabelSelector.builder()
            .matchLabel("role", "api")
            .build())
        .policyType("Ingress")
        .ingressRule(NetworkPolicyIngressRule.builder()
            .from(NetworkPolicyPeer.builder()
                .ipBlock(IPBlock.builder()
                    .cidr("172.17.0.0/16")
                    .except("172.17.1.0/24")
                    .build())
                .build())
            .port(NetworkPolicyPort.builder()
                .protocol("TCP")
                .port(443)
                .build())
            .build())
        .build())
    .build();

client.networkPolicies().create(allowExternalCidr);

// Create a NetworkPolicy with egress rules (restrict outbound traffic)
final NetworkPolicy restrictEgress = NetworkPolicy.builder()
    .name("restrict-egress")
    .namespace("default")
    .spec(NetworkPolicySpec.builder()
        .podSelector(LabelSelector.builder()
            .matchLabel("app", "web")
            .build())
        .policyType("Egress")
        .egressRule(NetworkPolicyEgressRule.builder()
            .to(NetworkPolicyPeer.builder()
                .podSelector(LabelSelector.builder()
                    .matchLabel("role", "db")
                    .build())
                .build())
            .port(NetworkPolicyPort.builder()
                .protocol("TCP")
                .port(5432)
                .build())
            .build())
        .egressRule(NetworkPolicyEgressRule.builder()
            .to(NetworkPolicyPeer.builder()
                .ipBlock(IPBlock.builder()
                    .cidr("0.0.0.0/0")
                    .except("169.254.169.254/32")  // Block EC2 metadata endpoint
                    .build())
                .build())
            .port(NetworkPolicyPort.builder()
                .protocol("TCP")
                .port(443)
                .build())
            .build())
        .build())
    .build();

client.networkPolicies().create(restrictEgress);

// Create a NetworkPolicy with both ingress and egress
final NetworkPolicy fullPolicy = NetworkPolicy.builder()
    .name("full-network-policy")
    .namespace("default")
    .label("tier", "security")
    .spec(NetworkPolicySpec.builder()
        .podSelector(LabelSelector.builder()
            .matchLabel("app", "payment")
            .build())
        .policyType("Ingress")
        .policyType("Egress")
        .ingressRule(NetworkPolicyIngressRule.builder()
            .from(NetworkPolicyPeer.builder()
                .podSelector(LabelSelector.builder()
                    .matchLabel("app", "frontend")
                    .build())
                .build())
            .port(NetworkPolicyPort.builder()
                .protocol("TCP")
                .port(8080)
                .build())
            .build())
        .egressRule(NetworkPolicyEgressRule.builder()
            .to(NetworkPolicyPeer.builder()
                .podSelector(LabelSelector.builder()
                    .matchLabel("app", "database")
                    .build())
                .build())
            .port(NetworkPolicyPort.builder()
                .protocol("TCP")
                .port(5432)
                .build())
            .build())
        .build())
    .build();

client.networkPolicies().create(fullPolicy);

// Get a NetworkPolicy
final NetworkPolicy retrieved = client.networkPolicies().get("default", "allow-db-access");

// List NetworkPolicies in a namespace
final List<NetworkPolicy> policies = client.networkPolicies().list("default");

// Delete a NetworkPolicy
client.networkPolicies().delete("default", "deny-all-ingress");
```

**kubectl equivalents:**
```bash
# Create NetworkPolicy
kubectl apply -f networkpolicy.yaml

# Get NetworkPolicy
kubectl get networkpolicy allow-db-access -n default

# Describe NetworkPolicy (shows ingress/egress rules)
kubectl describe networkpolicy allow-db-access -n default

# Delete NetworkPolicy
kubectl delete networkpolicy allow-db-access -n default

# List NetworkPolicies
kubectl get networkpolicies -n default
```

### HorizontalPodAutoscalers

```java
import io.elev8.resources.horizontalpodautoscaler.*;

// Simple CPU-based autoscaling
final HorizontalPodAutoscaler cpuHpa = HorizontalPodAutoscaler.builder()
    .name("php-apache-hpa")
    .namespace("default")
    .spec(HorizontalPodAutoscalerSpec.builder()
        .scaleTargetRef(CrossVersionObjectReference.builder()
            .apiVersion("apps/v1")
            .kind("Deployment")
            .name("php-apache")
            .build())
        .minReplicas(1)
        .maxReplicas(10)
        .metric(MetricSpec.builder()
            .type("Resource")
            .resource(ResourceMetricSource.builder()
                .name("cpu")
                .target(MetricTarget.builder()
                    .type("Utilization")
                    .averageUtilization(50)
                    .build())
                .build())
            .build())
        .build())
    .build();

client.horizontalPodAutoscalers().create(cpuHpa);

// Multi-metric HPA (CPU + Memory)
final HorizontalPodAutoscaler multiMetricHpa = HorizontalPodAutoscaler.builder()
    .name("multi-metric-hpa")
    .namespace("default")
    .spec(HorizontalPodAutoscalerSpec.builder()
        .scaleTargetRef(CrossVersionObjectReference.builder()
            .apiVersion("apps/v1")
            .kind("Deployment")
            .name("my-app")
            .build())
        .minReplicas(2)
        .maxReplicas(20)
        .metric(MetricSpec.builder()
            .type("Resource")
            .resource(ResourceMetricSource.builder()
                .name("cpu")
                .target(MetricTarget.builder()
                    .type("Utilization")
                    .averageUtilization(70)
                    .build())
                .build())
            .build())
        .metric(MetricSpec.builder()
            .type("Resource")
            .resource(ResourceMetricSource.builder()
                .name("memory")
                .target(MetricTarget.builder()
                    .type("Utilization")
                    .averageUtilization(80)
                    .build())
                .build())
            .build())
        .build())
    .build();

client.horizontalPodAutoscalers().create(multiMetricHpa);

// Get and check status
final HorizontalPodAutoscaler hpa = client.horizontalPodAutoscalers()
    .get("default", "php-apache-hpa");
System.out.println("Current: " + hpa.getStatus().getCurrentReplicas());
System.out.println("Desired: " + hpa.getStatus().getDesiredReplicas());

// List HPAs
final List<HorizontalPodAutoscaler> hpas =
    client.horizontalPodAutoscalers().list("default");

// Delete HPA
client.horizontalPodAutoscalers().delete("default", "php-apache-hpa");
```

**kubectl equivalents:**
```bash
# Create HPA
kubectl apply -f hpa.yaml

# Create HPA for deployment (shorthand)
kubectl autoscale deployment php-apache --cpu-percent=50 --min=1 --max=10

# Get HPA
kubectl get hpa php-apache-hpa -n default

# Describe HPA (shows current/desired replicas, metrics)
kubectl describe hpa php-apache-hpa -n default

# Delete HPA
kubectl delete hpa php-apache-hpa -n default
```

### VerticalPodAutoscalers

**NOTE**: VPA is a Custom Resource Definition (CRD) and must be installed in your cluster before use.
See [Kubernetes VPA Installation](https://github.com/kubernetes/autoscaler/tree/master/vertical-pod-autoscaler#installation).

```java
import io.elev8.resources.verticalpodautoscaler.*;
import io.elev8.resources.horizontalpodautoscaler.CrossVersionObjectReference;

// Create VPA with recommendation-only mode (safe for production)
final VerticalPodAutoscaler vpaOff = VerticalPodAutoscaler.builder()
    .name("my-app-vpa")
    .namespace("default")
    .spec(VerticalPodAutoscalerSpec.builder()
        .targetRef(CrossVersionObjectReference.builder()
            .apiVersion("apps/v1")
            .kind("Deployment")
            .name("my-app")
            .build())
        .updatePolicy(VPAUpdatePolicy.builder()
            .updateMode("Off")  // Only provide recommendations, don't update pods
            .build())
        .resourcePolicy(VPAResourcePolicy.builder()
            .containerPolicy(VPAContainerResourcePolicy.builder()
                .containerName("*")  // Apply to all containers
                .minAllowed(Map.of("cpu", "100m", "memory", "128Mi"))
                .maxAllowed(Map.of("cpu", "2", "memory", "2Gi"))
                .build())
            .build())
        .build())
    .build();

client.verticalPodAutoscalers().create(vpaOff);

// Create VPA with auto-update mode (updates pods automatically)
final VerticalPodAutoscaler vpaAuto = VerticalPodAutoscaler.builder()
    .name("auto-vpa")
    .namespace("default")
    .spec(VerticalPodAutoscalerSpec.builder()
        .targetRef(CrossVersionObjectReference.builder()
            .apiVersion("apps/v1")
            .kind("Deployment")
            .name("web-app")
            .build())
        .updatePolicy(VPAUpdatePolicy.builder()
            .updateMode("Auto")  // Automatically update pods (causes restarts)
            .minReplicas(2)  // Ensure at least 2 replicas during updates
            .build())
        .build())
    .build();

client.verticalPodAutoscalers().create(vpaAuto);

// Get VPA and check recommendations
final VerticalPodAutoscaler vpa = client.verticalPodAutoscalers()
    .get("default", "my-app-vpa");

if (vpa.getStatus() != null && vpa.getStatus().getRecommendation() != null) {
    vpa.getStatus().getRecommendation().getContainerRecommendations()
        .forEach(rec -> {
            System.out.println("Container: " + rec.getContainerName());
            System.out.println("Target CPU: " + rec.getTarget().get("cpu"));
            System.out.println("Target Memory: " + rec.getTarget().get("memory"));
            System.out.println("Lower Bound CPU: " + rec.getLowerBound().get("cpu"));
            System.out.println("Upper Bound CPU: " + rec.getUpperBound().get("cpu"));
        });
}

// List VPAs
final List<VerticalPodAutoscaler> vpas = client.verticalPodAutoscalers().list("default");

// Delete VPA
client.verticalPodAutoscalers().delete("default", "my-app-vpa");
```

**kubectl equivalents:**
```bash
# Install VPA CRD (required first-time setup)
kubectl apply -f https://github.com/kubernetes/autoscaler/releases/latest/download/vertical-pod-autoscaler.yaml

# Create VPA
kubectl apply -f vpa.yaml

# Get VPA
kubectl get vpa my-app-vpa -n default

# Describe VPA (shows recommendations)
kubectl describe vpa my-app-vpa -n default

# Delete VPA
kubectl delete vpa my-app-vpa -n default
```

### ResourceQuotas

```java
import io.elev8.resources.resourcequota.*;

// Create ResourceQuota with compute limits
final ResourceQuota computeQuota = ResourceQuota.builder()
    .name("compute-quota")
    .namespace("my-namespace")
    .spec(ResourceQuotaSpec.builder()
        .hardLimit("requests.cpu", "10")
        .hardLimit("requests.memory", "20Gi")
        .hardLimit("limits.cpu", "20")
        .hardLimit("limits.memory", "40Gi")
        .hardLimit("pods", "50")
        .build())
    .build();

client.resourceQuotas().create(computeQuota);

// Create ResourceQuota with object count limits
final ResourceQuota objectQuota = ResourceQuota.builder()
    .name("object-quota")
    .namespace("my-namespace")
    .spec(ResourceQuotaSpec.builder()
        .hardLimit("pods", "100")
        .hardLimit("services", "20")
        .hardLimit("secrets", "30")
        .hardLimit("configmaps", "25")
        .hardLimit("persistentvolumeclaims", "15")
        .build())
    .build();

client.resourceQuotas().create(objectQuota);

// Create ResourceQuota with scopes
final ResourceQuota scopedQuota = ResourceQuota.builder()
    .name("terminating-quota")
    .namespace("my-namespace")
    .spec(ResourceQuotaSpec.builder()
        .hardLimit("pods", "10")
        .scope("Terminating")  // Only count terminating pods
        .build())
    .build();

client.resourceQuotas().create(scopedQuota);

// Get quota and check usage
final ResourceQuota quota = client.resourceQuotas()
    .get("my-namespace", "compute-quota");
System.out.println("CPU Used: " + quota.getStatus().getUsed().get("requests.cpu"));
System.out.println("CPU Limit: " + quota.getStatus().getHard().get("requests.cpu"));

// List quotas
final List<ResourceQuota> quotas = client.resourceQuotas().list("my-namespace");

// Delete quota
client.resourceQuotas().delete("my-namespace", "compute-quota");
```

**kubectl equivalents:**
```bash
# Create ResourceQuota
kubectl apply -f resourcequota.yaml

# Get ResourceQuota
kubectl get resourcequota compute-quota -n my-namespace

# Describe ResourceQuota (shows used vs hard limits)
kubectl describe resourcequota compute-quota -n my-namespace

# Delete ResourceQuota
kubectl delete resourcequota compute-quota -n my-namespace
```

### PersistentVolumes

```java
import io.elev8.resources.persistentvolume.PersistentVolume;
import io.elev8.resources.persistentvolume.PersistentVolumeSpec;
import io.elev8.resources.persistentvolume.HostPathVolumeSource;
import io.elev8.resources.persistentvolume.NFSVolumeSource;
import io.elev8.resources.persistentvolume.AWSElasticBlockStoreVolumeSource;

// Create a HostPath PersistentVolume
final PersistentVolume hostPathPV = PersistentVolume.builder()
    .name("local-pv")
    .label("type", "local")
    .spec(PersistentVolumeSpec.builder()
        .storageClassName("manual")
        .capacity("10Gi")
        .accessMode("ReadWriteOnce")
        .hostPath(HostPathVolumeSource.builder()
            .path("/mnt/data")
            .type("DirectoryOrCreate")
            .build())
        .build())
    .build();

client.persistentVolumes().create(hostPathPV);

// Create an NFS PersistentVolume
final PersistentVolume nfsPV = PersistentVolume.builder()
    .name("nfs-pv")
    .label("type", "nfs")
    .spec(PersistentVolumeSpec.builder()
        .storageClassName("nfs")
        .capacity("50Gi")
        .accessMode("ReadWriteMany")
        .persistentVolumeReclaimPolicy("Retain")
        .nfs(NFSVolumeSource.builder()
            .server("nfs-server.example.com")
            .path("/exports/data")
            .readOnly(false)
            .build())
        .build())
    .build();

client.persistentVolumes().create(nfsPV);

// Create an AWS EBS PersistentVolume
final PersistentVolume ebsPV = PersistentVolume.builder()
    .name("ebs-pv")
    .label("type", "ebs")
    .spec(PersistentVolumeSpec.builder()
        .storageClassName("gp2")
        .capacity("100Gi")
        .accessMode("ReadWriteOnce")
        .awsElasticBlockStore(AWSElasticBlockStoreVolumeSource.builder()
            .volumeID("vol-0123456789abcdef0")
            .fsType("ext4")
            .build())
        .build())
    .build();

client.persistentVolumes().create(ebsPV);

// Get a PersistentVolume (note: no namespace needed, cluster-scoped)
final PersistentVolume retrieved = client.persistentVolumes().get("local-pv");

// List all PersistentVolumes
final List<PersistentVolume> pvs = client.persistentVolumes().list();

// Delete a PersistentVolume
client.persistentVolumes().delete("local-pv");
```

**kubectl equivalents:**
```bash
# Create PersistentVolume
kubectl apply -f persistentvolume.yaml

# Get PersistentVolume (no namespace needed)
kubectl get pv local-pv

# List all PersistentVolumes
kubectl get pv

# Describe PersistentVolume (shows capacity, access modes, status)
kubectl describe pv local-pv

# Delete PersistentVolume
kubectl delete pv local-pv
```

### PersistentVolumeClaims

```java
import io.elev8.resources.persistentvolumeclaim.PersistentVolumeClaim;
import io.elev8.resources.persistentvolumeclaim.PersistentVolumeClaimSpec;
import io.elev8.resources.ResourceRequirements;

// Create a simple PersistentVolumeClaim
final PersistentVolumeClaim pvc = PersistentVolumeClaim.builder()
    .name("my-pvc")
    .namespace("default")
    .spec(PersistentVolumeClaimSpec.builder()
        .accessMode("ReadWriteOnce")
        .resources(ResourceRequirements.builder()
            .request("storage", "10Gi")
            .build())
        .build())
    .build();

client.persistentVolumeClaims().create(pvc);

// Create a PVC with a specific StorageClass
final PersistentVolumeClaim ebsPVC = PersistentVolumeClaim.builder()
    .name("ebs-claim")
    .namespace("default")
    .label("app", "database")
    .spec(PersistentVolumeClaimSpec.builder()
        .accessMode("ReadWriteOnce")
        .storageClassName("gp2")
        .resources(ResourceRequirements.builder()
            .request("storage", "100Gi")
            .build())
        .build())
    .build();

client.persistentVolumeClaims().create(ebsPVC);

// Create a shared PVC with multiple access modes
final PersistentVolumeClaim sharedPVC = PersistentVolumeClaim.builder()
    .name("shared-data")
    .namespace("default")
    .spec(PersistentVolumeClaimSpec.builder()
        .accessMode("ReadWriteMany")
        .storageClassName("nfs")
        .resources(ResourceRequirements.builder()
            .request("storage", "50Gi")
            .build())
        .build())
    .build();

client.persistentVolumeClaims().create(sharedPVC);

// Create a block mode PVC
final PersistentVolumeClaim blockPVC = PersistentVolumeClaim.builder()
    .name("block-storage")
    .namespace("default")
    .spec(PersistentVolumeClaimSpec.builder()
        .accessMode("ReadWriteOnce")
        .volumeMode("Block")
        .storageClassName("fast-ssd")
        .resources(ResourceRequirements.builder()
            .request("storage", "20Gi")
            .build())
        .build())
    .build();

client.persistentVolumeClaims().create(blockPVC);

// Get a PersistentVolumeClaim
final PersistentVolumeClaim retrieved = client.persistentVolumeClaims()
    .get("default", "my-pvc");

// List PersistentVolumeClaims in a namespace
final List<PersistentVolumeClaim> pvcs = client.persistentVolumeClaims()
    .list("default");

// Delete a PersistentVolumeClaim
client.persistentVolumeClaims().delete("default", "my-pvc");
```

**kubectl equivalents:**
```bash
# Create PersistentVolumeClaim
kubectl apply -f persistentvolumeclaim.yaml

# Get PersistentVolumeClaim
kubectl get pvc my-pvc -n default

# List all PersistentVolumeClaims
kubectl get pvc -n default

# Describe PersistentVolumeClaim (shows capacity, status, volume)
kubectl describe pvc my-pvc -n default

# Delete PersistentVolumeClaim
kubectl delete pvc my-pvc -n default
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

### Watch API

Watch resources in real-time for changes:

```java
import io.elev8.core.watch.WatchEvent;
import io.elev8.core.watch.WatchOptions;
import io.elev8.core.watch.Watcher;

// Watch pods in a namespace
final WatchOptions options = WatchOptions.defaults();

client.pods().watch("default", options, new Watcher<Pod>() {
    @Override
    public void onEvent(final WatchEvent<Pod> event) {
        System.out.println("Event: " + event.getType());
        System.out.println("Pod: " + event.getObject().getName());

        if (event.isAdded()) {
            // Handle pod added
        } else if (event.isModified()) {
            // Handle pod modified
        } else if (event.isDeleted()) {
            // Handle pod deleted
        }
    }

    @Override
    public void onError(final Exception exception) {
        System.err.println("Watch error: " + exception.getMessage());
    }

    @Override
    public void onClose() {
        System.out.println("Watch closed");
    }
});

// Watch with label selector
final WatchOptions labelOptions = WatchOptions.withLabelSelector("app=myapp");
client.pods().watch("default", labelOptions, watcher);

// Watch with field selector
final WatchOptions fieldOptions = WatchOptions.withFieldSelector("status.phase=Running");
client.pods().watch("default", fieldOptions, watcher);

// Watch all namespaces
client.pods().watchAllNamespaces(options, watcher);

// Resume watch from a specific resource version
final WatchOptions resumeOptions = WatchOptions.from("12345");
client.pods().watch("default", resumeOptions, watcher);

// Watch cluster-scoped resources
client.namespaces().watch(options, watcher);
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
    .clusterName("my-cluster")
    .region("us-east-1")  // No manual token management!
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

### ConfigMap Operations

| kubectl Command | Elev8 Equivalent |
|----------------|------------------|
| `kubectl get configmaps -n default` | `client.configMaps().list("default")` |
| `kubectl get configmap my-config -n default` | `client.configMaps().get("default", "my-config")` |
| `kubectl create configmap my-config --from-literal=key=value` | `ConfigMap cm = ConfigMap.builder()<br>  .name("my-config")<br>  .namespace("default")<br>  .addData("key", "value")<br>  .build();<br>client.configMaps().create(cm);` |
| `kubectl delete configmap my-config` | `client.configMaps().delete("default", "my-config")` |
| `kubectl create configmap app-config --from-literal=env=prod` | `ConfigMap cm = ConfigMap.builder()<br>  .name("app-config")<br>  .namespace("default")<br>  .addData("env", "prod")<br>  .build();<br>client.configMaps().create(cm);` |

### Secret Operations

| kubectl Command | Elev8 Equivalent |
|----------------|------------------|
| `kubectl get secrets -n default` | `client.secrets().list("default")` |
| `kubectl get secret my-secret -n default` | `client.secrets().get("default", "my-secret")` |
| `kubectl create secret generic my-secret --from-literal=password=abc123` | `Secret s = Secret.builder()<br>  .name("my-secret")<br>  .namespace("default")<br>  .addStringData("password", "abc123")<br>  .build();<br>client.secrets().create(s);` |
| `kubectl create secret docker-registry regcred --docker-server=<server> --docker-username=<user> --docker-password=<pwd>` | `Secret s = Secret.builder()<br>  .name("regcred")<br>  .namespace("default")<br>  .dockerConfigJson("base64-encoded-config")<br>  .build();<br>client.secrets().create(s);` |
| `kubectl create secret tls tls-secret --cert=path/to/cert --key=path/to/key` | `Secret s = Secret.builder()<br>  .name("tls-secret")<br>  .namespace("default")<br>  .tls("base64-cert", "base64-key")<br>  .build();<br>client.secrets().create(s);` |
| `kubectl delete secret my-secret` | `client.secrets().delete("default", "my-secret")` |

### DaemonSet Operations

| kubectl Command | Elev8 Equivalent |
|----------------|------------------|
| `kubectl get daemonsets -n kube-system` | `client.daemonSets().list("kube-system")` |
| `kubectl get daemonset fluentd -n kube-system` | `client.daemonSets().get("kube-system", "fluentd")` |
| `kubectl create -f daemonset.yaml` | `DaemonSet ds = DaemonSet.builder()...build();<br>client.daemonSets().create(ds);` |
| `kubectl delete daemonset fluentd -n kube-system` | `client.daemonSets().delete("kube-system", "fluentd")` |
| `kubectl rollout status daemonset/fluentd -n kube-system` | `final DaemonSet ds = client.daemonSets().get("kube-system", "fluentd");<br>// Check ds.getStatus().getNumberReady() and ds.getStatus().getDesiredNumberScheduled()` |

### Job Operations

| kubectl Command | Elev8 Equivalent |
|----------------|------------------|
| `kubectl get jobs -n default` | `client.jobs().list("default")` |
| `kubectl get job my-job -n default` | `client.jobs().get("default", "my-job")` |
| `kubectl create -f job.yaml` | `Job job = Job.builder()...build();<br>client.jobs().create(job);` |
| `kubectl delete job my-job -n default` | `client.jobs().delete("default", "my-job")` |
| `kubectl logs job/my-job -n default` | `final Job job = client.jobs().get("default", "my-job");<br>// Get pod logs using job.getStatus() to find pod names` |
| `kubectl wait --for=condition=complete job/my-job` | `final Job job = client.jobs().get("default", "my-job");<br>// Poll job.getStatus().getSucceeded() until equals completions` |

### StatefulSet Operations

| kubectl Command | Elev8 Equivalent |
|----------------|------------------|
| `kubectl get statefulsets -n default` | `client.statefulSets().list("default")` |
| `kubectl get statefulset web -n default` | `client.statefulSets().get("default", "web")` |
| `kubectl create -f statefulset.yaml` | `StatefulSet sts = StatefulSet.builder()...build();<br>client.statefulSets().create(sts);` |
| `kubectl delete statefulset web -n default` | `client.statefulSets().delete("default", "web")` |
| `kubectl scale statefulset web --replicas=5` | `final StatefulSet sts = client.statefulSets().get("default", "web");<br>sts.getSpec().setReplicas(5);<br>client.statefulSets().update(sts);` |
| `kubectl rollout status statefulset/web -n default` | `final StatefulSet sts = client.statefulSets().get("default", "web");<br>// Check sts.getStatus().getReadyReplicas() and sts.getStatus().getReplicas()` |

### ReplicaSet Operations

| kubectl Command | Elev8 Equivalent |
|----------------|------------------|
| `kubectl get replicasets -n default` | `client.replicaSets().list("default")` |
| `kubectl get replicaset nginx-replicaset -n default` | `client.replicaSets().get("default", "nginx-replicaset")` |
| `kubectl create -f replicaset.yaml` | `ReplicaSet rs = ReplicaSet.builder()...build();<br>client.replicaSets().create(rs);` |
| `kubectl delete replicaset nginx-replicaset -n default` | `client.replicaSets().delete("default", "nginx-replicaset")` |
| `kubectl scale replicaset nginx-replicaset --replicas=5` | `final ReplicaSet rs = client.replicaSets().get("default", "nginx-replicaset");<br>rs.getSpec().setReplicas(5);<br>client.replicaSets().update(rs);` |
| `kubectl get replicaset nginx-replicaset -o json` | `final ReplicaSet rs = client.replicaSets().get("default", "nginx-replicaset");<br>String json = rs.toJson();` |

### Ingress Operations

| kubectl Command | Elev8 Equivalent |
|----------------|------------------|
| `kubectl get ingress -n default` | `client.ingresses().list("default")` |
| `kubectl get ingress example-ingress -n default` | `client.ingresses().get("default", "example-ingress")` |
| `kubectl create -f ingress.yaml` | `Ingress ing = Ingress.builder()...build();<br>client.ingresses().create(ing);` |
| `kubectl delete ingress example-ingress -n default` | `client.ingresses().delete("default", "example-ingress")` |
| `kubectl describe ingress example-ingress` | `final Ingress ing = client.ingresses().get("default", "example-ingress");<br>// Check ing.getStatus().getLoadBalancer()` |
| `kubectl get ingress example-ingress -o json` | `final Ingress ing = client.ingresses().get("default", "example-ingress");<br>String json = ing.toJson();` |

### CronJob Operations

| kubectl Command | Elev8 Equivalent |
|----------------|------------------|
| `kubectl get cronjobs -n default` | `client.cronJobs().list("default")` |
| `kubectl get cronjob hello -n default` | `client.cronJobs().get("default", "hello")` |
| `kubectl create -f cronjob.yaml` | `CronJob cj = CronJob.builder()...build();<br>client.cronJobs().create(cj);` |
| `kubectl delete cronjob hello -n default` | `client.cronJobs().delete("default", "hello")` |
| `kubectl patch cronjob hello -p '{"spec":{"suspend":true}}'` | `final CronJob cj = client.cronJobs().get("default", "hello");<br>cj.getSpec().setSuspend(true);<br>client.cronJobs().update(cj);` |
| `kubectl get cronjob hello -o json` | `final CronJob cj = client.cronJobs().get("default", "hello");<br>String json = cj.toJson();` |

### Namespace Operations

| kubectl Command | Elev8 Equivalent |
|----------------|------------------|
| `kubectl get namespaces` | `client.namespaces().listAllNamespaces()` |
| `kubectl get namespace production` | `client.namespaces().get("production")` |
| `kubectl create namespace development` | `Namespace ns = Namespace.builder()<br>  .name("development")<br>  .build();<br>client.namespaces().create(ns);` |
| `kubectl create namespace prod --labels=env=production` | `Namespace ns = Namespace.builder()<br>  .name("prod")<br>  .label("env", "production")<br>  .build();<br>client.namespaces().create(ns);` |
| `kubectl delete namespace development` | `client.namespaces().delete("development")` |
| `kubectl get namespace production -o json` | `final Namespace ns = client.namespaces().get("production");<br>String json = ns.toJson();` |

### ServiceAccount Operations

| kubectl Command | Elev8 Equivalent |
|----------------|------------------|
| `kubectl get serviceaccounts -n default` | `client.serviceAccounts().list("default")` |
| `kubectl get serviceaccount my-service-account -n default` | `client.serviceAccounts().get("default", "my-service-account")` |
| `kubectl create serviceaccount my-service-account -n default` | `ServiceAccount sa = ServiceAccount.builder()<br>  .name("my-service-account")<br>  .namespace("default")<br>  .build();<br>client.serviceAccounts().create(sa);` |
| `kubectl delete serviceaccount my-service-account -n default` | `client.serviceAccounts().delete("default", "my-service-account")` |
| `kubectl describe serviceaccount my-service-account -n default` | `final ServiceAccount sa = client.serviceAccounts().get("default", "my-service-account");<br>// Check sa.getStatus().getSecrets()` |
| `kubectl get serviceaccount my-service-account -o json` | `final ServiceAccount sa = client.serviceAccounts().get("default", "my-service-account");<br>String json = sa.toJson();` |

### Role Operations

| kubectl Command | Elev8 Equivalent |
|----------------|------------------|
| `kubectl get roles -n default` | `client.roles().list("default")` |
| `kubectl get role pod-reader -n default` | `client.roles().get("default", "pod-reader")` |
| `kubectl create role pod-reader --verb=get --verb=list --resource=pods -n default` | `Role role = Role.builder()<br>  .name("pod-reader")<br>  .namespace("default")<br>  .spec(RoleSpec.builder()<br>    .rule(PolicyRule.builder()<br>      .apiGroup("")<br>      .resource("pods")<br>      .verb("get")<br>      .verb("list")<br>      .build())<br>    .build())<br>  .build();<br>client.roles().create(role);` |
| `kubectl delete role pod-reader -n default` | `client.roles().delete("default", "pod-reader")` |
| `kubectl describe role pod-reader -n default` | `final Role role = client.roles().get("default", "pod-reader");<br>// Check role.getSpec().getRules()` |
| `kubectl get role pod-reader -o json` | `final Role role = client.roles().get("default", "pod-reader");<br>String json = role.toJson();` |

### RoleBinding Operations

| kubectl Command | Elev8 Equivalent |
|----------------|------------------|
| `kubectl get rolebindings -n default` | `client.roleBindings().list("default")` |
| `kubectl get rolebinding read-pods -n default` | `client.roleBindings().get("default", "read-pods")` |
| `kubectl create rolebinding read-pods --role=pod-reader --serviceaccount=default:my-sa -n default` | `RoleBinding rb = RoleBinding.builder()<br>  .name("read-pods")<br>  .namespace("default")<br>  .spec(RoleBindingSpec.builder()<br>    .subject(Subject.builder()<br>      .kind("ServiceAccount")<br>      .name("my-sa")<br>      .namespace("default")<br>      .build())<br>    .roleRef(RoleRef.builder()<br>      .apiGroup("rbac.authorization.k8s.io")<br>      .kind("Role")<br>      .name("pod-reader")<br>      .build())<br>    .build())<br>  .build();<br>client.roleBindings().create(rb);` |
| `kubectl delete rolebinding read-pods -n default` | `client.roleBindings().delete("default", "read-pods")` |
| `kubectl describe rolebinding read-pods -n default` | `final RoleBinding rb = client.roleBindings().get("default", "read-pods");<br>// Check rb.getSpec().getSubjects() and rb.getSpec().getRoleRef()` |
| `kubectl get rolebinding read-pods -o json` | `final RoleBinding rb = client.roleBindings().get("default", "read-pods");<br>String json = rb.toJson();` |

### ClusterRole Operations

| kubectl Command | Elev8 Equivalent |
|----------------|------------------|
| `kubectl get clusterroles` | `client.clusterRoles().list()` |
| `kubectl get clusterrole pod-reader` | `client.clusterRoles().get("pod-reader")` |
| `kubectl create clusterrole pod-reader --verb=get --verb=list --resource=pods` | `ClusterRole role = ClusterRole.builder()<br>  .name("pod-reader")<br>  .spec(RoleSpec.builder()<br>    .rule(PolicyRule.builder()<br>      .apiGroup("")<br>      .resource("pods")<br>      .verb("get")<br>      .verb("list")<br>      .build())<br>    .build())<br>  .build();<br>client.clusterRoles().create(role);` |
| `kubectl delete clusterrole pod-reader` | `client.clusterRoles().delete("pod-reader")` |
| `kubectl describe clusterrole pod-reader` | `final ClusterRole role = client.clusterRoles().get("pod-reader");<br>// Check role.getSpec().getRules()` |
| `kubectl get clusterrole pod-reader -o json` | `final ClusterRole role = client.clusterRoles().get("pod-reader");<br>String json = role.toJson();` |

### ClusterRoleBinding Operations

| kubectl Command | Elev8 Equivalent |
|----------------|------------------|
| `kubectl get clusterrolebindings` | `client.clusterRoleBindings().list()` |
| `kubectl get clusterrolebinding cluster-admin-binding` | `client.clusterRoleBindings().get("cluster-admin-binding")` |
| `kubectl create clusterrolebinding admin-binding --clusterrole=cluster-admin --user=admin@example.com` | `ClusterRoleBinding rb = ClusterRoleBinding.builder()<br>  .name("admin-binding")<br>  .spec(ClusterRoleBindingSpec.builder()<br>    .subject(Subject.builder()<br>      .kind("User")<br>      .name("admin@example.com")<br>      .apiGroup("rbac.authorization.k8s.io")<br>      .build())<br>    .roleRef(RoleRef.builder()<br>      .apiGroup("rbac.authorization.k8s.io")<br>      .kind("ClusterRole")<br>      .name("cluster-admin")<br>      .build())<br>    .build())<br>  .build();<br>client.clusterRoleBindings().create(rb);` |
| `kubectl delete clusterrolebinding cluster-admin-binding` | `client.clusterRoleBindings().delete("cluster-admin-binding")` |
| `kubectl describe clusterrolebinding cluster-admin-binding` | `final ClusterRoleBinding rb = client.clusterRoleBindings().get("cluster-admin-binding");<br>// Check rb.getSpec().getSubjects() and rb.getSpec().getRoleRef()` |
| `kubectl get clusterrolebinding cluster-admin-binding -o json` | `final ClusterRoleBinding rb = client.clusterRoleBindings().get("cluster-admin-binding");<br>String json = rb.toJson();` |

### NetworkPolicy Operations

| kubectl Command | Elev8 Equivalent |
|----------------|------------------|
| `kubectl get networkpolicies -n default` | `client.networkPolicies().list("default")` |
| `kubectl get networkpolicy allow-db-access -n default` | `client.networkPolicies().get("default", "allow-db-access")` |
| `kubectl create -f networkpolicy.yaml` | `NetworkPolicy np = NetworkPolicy.builder()<br>  .name("allow-db-access")<br>  .namespace("default")<br>  .spec(NetworkPolicySpec.builder()<br>    .podSelector(LabelSelector.builder()<br>      .matchLabel("role", "db")<br>      .build())<br>    .policyType("Ingress")<br>    .ingressRule(NetworkPolicyIngressRule.builder()<br>      .from(NetworkPolicyPeer.builder()<br>        .podSelector(LabelSelector.builder()<br>          .matchLabel("role", "frontend")<br>          .build())<br>        .build())<br>      .build())<br>    .build())<br>  .build();<br>client.networkPolicies().create(np);` |
| `kubectl delete networkpolicy allow-db-access -n default` | `client.networkPolicies().delete("default", "allow-db-access")` |
| `kubectl describe networkpolicy allow-db-access -n default` | `final NetworkPolicy np = client.networkPolicies().get("default", "allow-db-access");<br>// Check np.getSpec().getIngress() and np.getSpec().getEgress()` |
| `kubectl get networkpolicy allow-db-access -o json` | `final NetworkPolicy np = client.networkPolicies().get("default", "allow-db-access");<br>String json = np.toJson();` |

### HorizontalPodAutoscaler Operations

| kubectl Command | Elev8 Equivalent |
|----------------|------------------|
| `kubectl get hpa -n default` | `client.horizontalPodAutoscalers().list("default")` |
| `kubectl get hpa php-apache -n default` | `client.horizontalPodAutoscalers().get("default", "php-apache")` |
| `kubectl autoscale deployment php-apache --cpu-percent=50 --min=1 --max=10` | `HorizontalPodAutoscaler hpa = HorizontalPodAutoscaler.builder()<br>  .name("php-apache")<br>  .namespace("default")<br>  .spec(HorizontalPodAutoscalerSpec.builder()<br>    .scaleTargetRef(CrossVersionObjectReference.builder()<br>      .apiVersion("apps/v1")<br>      .kind("Deployment")<br>      .name("php-apache")<br>      .build())<br>    .minReplicas(1)<br>    .maxReplicas(10)<br>    .metric(MetricSpec.builder()<br>      .type("Resource")<br>      .resource(ResourceMetricSource.builder()<br>        .name("cpu")<br>        .target(MetricTarget.builder()<br>          .type("Utilization")<br>          .averageUtilization(50)<br>          .build())<br>        .build())<br>      .build())<br>    .build())<br>  .build();<br>client.horizontalPodAutoscalers().create(hpa);` |
| `kubectl delete hpa php-apache -n default` | `client.horizontalPodAutoscalers().delete("default", "php-apache")` |
| `kubectl describe hpa php-apache -n default` | `final HorizontalPodAutoscaler hpa = client.horizontalPodAutoscalers().get("default", "php-apache");<br>// Check hpa.getStatus().getCurrentReplicas(), hpa.getStatus().getDesiredReplicas()` |
| `kubectl get hpa php-apache -o json` | `final HorizontalPodAutoscaler hpa = client.horizontalPodAutoscalers().get("default", "php-apache");<br>String json = hpa.toJson();` |

### VerticalPodAutoscaler Operations

**NOTE**: VPA is a CRD and requires cluster installation before use.

| kubectl Command | Elev8 Equivalent |
|----------------|------------------|
| `kubectl get vpa -n default` | `client.verticalPodAutoscalers().list("default")` |
| `kubectl get vpa my-app-vpa -n default` | `client.verticalPodAutoscalers().get("default", "my-app-vpa")` |
| `kubectl apply -f vpa.yaml` | `VerticalPodAutoscaler vpa = VerticalPodAutoscaler.builder()<br>  .name("my-app-vpa")<br>  .namespace("default")<br>  .spec(VerticalPodAutoscalerSpec.builder()<br>    .targetRef(CrossVersionObjectReference.builder()<br>      .apiVersion("apps/v1")<br>      .kind("Deployment")<br>      .name("my-app")<br>      .build())<br>    .updatePolicy(VPAUpdatePolicy.builder()<br>      .updateMode("Off")<br>      .build())<br>    .build())<br>  .build();<br>client.verticalPodAutoscalers().create(vpa);` |
| `kubectl delete vpa my-app-vpa -n default` | `client.verticalPodAutoscalers().delete("default", "my-app-vpa")` |
| `kubectl describe vpa my-app-vpa -n default` | `final VerticalPodAutoscaler vpa = client.verticalPodAutoscalers().get("default", "my-app-vpa");<br>// Check vpa.getStatus().getRecommendation()` |
| `kubectl get vpa my-app-vpa -o json` | `final VerticalPodAutoscaler vpa = client.verticalPodAutoscalers().get("default", "my-app-vpa");<br>String json = vpa.toJson();` |

### ResourceQuota Operations

| kubectl Command | Elev8 Equivalent |
|----------------|------------------|
| `kubectl get resourcequota -n my-ns` | `client.resourceQuotas().list("my-ns")` |
| `kubectl get resourcequota compute-quota -n my-ns` | `client.resourceQuotas().get("my-ns", "compute-quota")` |
| `kubectl create -f quota.yaml` | `ResourceQuota quota = ResourceQuota.builder()<br>  .name("compute-quota")<br>  .namespace("my-ns")<br>  .spec(ResourceQuotaSpec.builder()<br>    .hardLimit("requests.cpu", "10")<br>    .hardLimit("requests.memory", "20Gi")<br>    .hardLimit("pods", "50")<br>    .build())<br>  .build();<br>client.resourceQuotas().create(quota);` |
| `kubectl delete resourcequota compute-quota -n my-ns` | `client.resourceQuotas().delete("my-ns", "compute-quota")` |
| `kubectl describe resourcequota compute-quota -n my-ns` | `final ResourceQuota quota = client.resourceQuotas().get("my-ns", "compute-quota");<br>// Check quota.getStatus().getUsed() vs quota.getStatus().getHard()` |
| `kubectl get resourcequota compute-quota -o json` | `final ResourceQuota quota = client.resourceQuotas().get("my-ns", "compute-quota");<br>String json = quota.toJson();` |

### PersistentVolume Operations

| kubectl Command | Elev8 Equivalent |
|----------------|------------------|
| `kubectl get pv` | `client.persistentVolumes().list()` |
| `kubectl get pv local-pv` | `client.persistentVolumes().get("local-pv")` |
| `kubectl create -f persistentvolume.yaml` | `PersistentVolume pv = PersistentVolume.builder()...build();<br>client.persistentVolumes().create(pv);` |
| `kubectl delete pv local-pv` | `client.persistentVolumes().delete("local-pv")` |
| `kubectl describe pv local-pv` | `final PersistentVolume pv = client.persistentVolumes().get("local-pv");<br>// Check pv.getSpec() and pv.getStatus()` |
| `kubectl get pv local-pv -o json` | `final PersistentVolume pv = client.persistentVolumes().get("local-pv");<br>String json = pv.toJson();` |

### PersistentVolumeClaim Operations

| kubectl Command | Elev8 Equivalent |
|----------------|------------------|
| `kubectl get pvc -n default` | `client.persistentVolumeClaims().list("default")` |
| `kubectl get pvc my-pvc -n default` | `client.persistentVolumeClaims().get("default", "my-pvc")` |
| `kubectl create -f persistentvolumeclaim.yaml` | `PersistentVolumeClaim pvc = PersistentVolumeClaim.builder()...build();<br>client.persistentVolumeClaims().create(pvc);` |
| `kubectl delete pvc my-pvc -n default` | `client.persistentVolumeClaims().delete("default", "my-pvc")` |
| `kubectl describe pvc my-pvc -n default` | `final PersistentVolumeClaim pvc = client.persistentVolumeClaims().get("default", "my-pvc");<br>// Check pvc.getSpec() and pvc.getStatus()` |
| `kubectl get pvc my-pvc -o json` | `final PersistentVolumeClaim pvc = client.persistentVolumeClaims().get("default", "my-pvc");<br>String json = pvc.toJson();` |

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
- [x] OIDC/IRSA authentication support
- [x] EKS Access Entries API integration
- [x] Comprehensive unit test coverage
- [x] ConfigMap and Secret resources
- [x] DaemonSet resource support
- [x] Job resource support
- [x] StatefulSet resource support
- [x] CronJob resource support
- [x] Namespace resource support
- [x] ReplicaSet resource support

### In Progress

#### Phase 1: Core Resources (High Priority)
- [x] Namespace resource support
- [x] ReplicaSet resource support
- [x] Ingress resource support (networking.k8s.io/v1)
- [x] ServiceAccount resource support
- [x] PersistentVolume and PersistentVolumeClaim resources

#### Phase 2: Security & RBAC ✅
- [x] Role and RoleBinding resources (rbac.authorization.k8s.io/v1)
- [x] ClusterRole and ClusterRoleBinding resources
- [x] NetworkPolicy resource support (networking.k8s.io/v1)

#### Phase 3: Scaling & Resource Management ✅
- [x] HorizontalPodAutoscaler resource support (autoscaling/v2)
- [x] VerticalPodAutoscaler resource support (autoscaling.k8s.io/v1)
- [x] ResourceQuota resource support
- [x] LimitRange resource support
- [x] PodDisruptionBudget resource support (policy/v1)

#### Phase 4: Storage & Persistence ✅
- [x] StorageClass resource support (storage.k8s.io/v1)
- [x] VolumeSnapshot support (snapshot.storage.k8s.io/v1)
  - [x] VolumeSnapshotClass (cluster-scoped)
  - [x] VolumeSnapshot (namespace-scoped)
  - [x] VolumeSnapshotContent (cluster-scoped)
- [x] CSIDriver resource support (storage.k8s.io/v1)

#### Phase 5: Advanced Operations
- [x] Watch API implementation for resource updates
- [ ] Resource change event streaming
- [ ] Pod log streaming API
- [ ] Exec into pods support
- [ ] Port forwarding support
- [ ] Patch operations (JSON Patch/Strategic Merge Patch)
- [ ] Server-side Apply operations

#### Phase 6: Events & Observability
- [ ] Event resource support (v1)
- [ ] Event watching and filtering
- [ ] Resource status condition helpers
- [ ] Metrics API support (metrics.k8s.io)

#### Phase 7: Production Patterns & Performance
- [ ] Informers pattern implementation
- [ ] SharedInformers with client-side caching
- [ ] Leader election support (coordination.k8s.io/v1)
- [ ] Work queue implementation
- [ ] Request retry with exponential backoff
- [ ] Connection pooling optimizations
- [ ] Rate limiting support

#### Phase 8: Custom Resources
- [ ] CustomResourceDefinition (CRD) resource support
- [ ] Generic custom resource CRUD operations
- [ ] Dynamic client for untyped resources
- [ ] Code generation from CRD schemas

#### Phase 9: Multi-Cloud Support
- [ ] GKE authentication (GCP IAM/Workload Identity)
- [ ] GKE cluster auto-discovery
- [ ] GkeClient implementation
- [ ] AKS authentication (Azure AD/Managed Identity)
- [ ] AKS cluster auto-discovery
- [ ] AksClient implementation
- [ ] Multi-cloud abstraction layer

#### Phase 10: Advanced Features
- [ ] Reactive API support (Project Reactor/RxJava)
- [ ] Field selectors for advanced filtering
- [ ] Label selector query enhancements
- [ ] Resource aggregation APIs
- [ ] Admission webhooks support

#### Phase 11: Release & Distribution
- [ ] Maven Central publication
- [ ] API stability guarantees
- [ ] Semantic versioning strategy
- [ ] Migration guides between versions
- [ ] Performance benchmarks and documentation
