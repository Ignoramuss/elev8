# Elev8 Roadmap

This document outlines the development roadmap for Elev8, a cloud-native Kubernetes Java client.

> For installation and usage instructions, see [README.md](README.md)

**Last Updated:** December 2025

## Completed

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

## In Progress

### Phase 1: Core Resources (High Priority)
- [x] Namespace resource support
- [x] ReplicaSet resource support
- [x] Ingress resource support (networking.k8s.io/v1)
- [x] ServiceAccount resource support
- [x] PersistentVolume and PersistentVolumeClaim resources

### Phase 2: Security & RBAC
- [x] Role and RoleBinding resources (rbac.authorization.k8s.io/v1)
- [x] ClusterRole and ClusterRoleBinding resources
- [x] NetworkPolicy resource support (networking.k8s.io/v1)

### Phase 3: Scaling & Resource Management
- [x] HorizontalPodAutoscaler resource support (autoscaling/v2)
- [x] VerticalPodAutoscaler resource support (autoscaling.k8s.io/v1)
- [x] ResourceQuota resource support
- [x] LimitRange resource support
- [x] PodDisruptionBudget resource support (policy/v1)

### Phase 4: Storage & Persistence
- [x] StorageClass resource support (storage.k8s.io/v1)
- [x] VolumeSnapshot support (snapshot.storage.k8s.io/v1)
  - [x] VolumeSnapshotClass (cluster-scoped)
  - [x] VolumeSnapshot (namespace-scoped)
  - [x] VolumeSnapshotContent (cluster-scoped)
- [x] CSIDriver resource support (storage.k8s.io/v1)

### Phase 5: Advanced Operations
- [x] Watch API implementation for resource updates
- [x] Resource change event streaming
- [x] Pod log streaming API
- [x] Exec into pods support
- [x] Port forwarding support
- [x] Patch operations (JSON Patch/Strategic Merge Patch)
- [x] Server-side Apply operations

### Phase 6: Events & Observability
- [x] Event resource support (v1)
- [x] Event watching and filtering
- [x] Resource status condition helpers
- [x] Metrics API support (metrics.k8s.io)

### Phase 7: Production Patterns & Performance
- [x] Informers pattern implementation (basic)
- [x] Informer resync support
- [x] Informer indexing (secondary indices)
- [x] SharedInformers with store sharing
- [x] Leader election support (coordination.k8s.io/v1)
- [x] Work queue implementation
- [x] Request retry with exponential backoff
- [x] Connection pooling optimizations
- [x] Rate limiting support

### Phase 8: Custom Resources
- [x] CustomResourceDefinition (CRD) resource support
- [x] Generic custom resource CRUD operations
- [x] Dynamic client for untyped resources
- [x] Code generation from CRD schemas

### Phase 9: Multi-Cloud Support
- [x] GKE authentication (GCP IAM/Workload Identity)
- [x] GKE cluster auto-discovery
- [x] GkeClient implementation
- [x] AKS authentication (Azure AD/Managed Identity)
- [x] AKS cluster auto-discovery
- [x] AksClient implementation
- [x] Multi-cloud abstraction layer

### Phase 10: Advanced Features
- [x] Reactive API support (Project Reactor)
- [x] Field selectors for advanced filtering
- [ ] Label selector query enhancements
- [ ] Resource aggregation APIs
- [ ] Admission webhooks support

### Phase 11: Release & Distribution
- [ ] Maven Central publication
- [ ] API stability guarantees
- [ ] Semantic versioning strategy
- [ ] Migration guides between versions
- [ ] Performance benchmarks and documentation

## Contributing

Interested in contributing? Check our [Contributing Guide](CONTRIBUTING.md) or pick an item from the roadmap above!
