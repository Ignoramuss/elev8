# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- **GKE Support** - Google Kubernetes Engine authentication and client (Phase 9 - Part 1)
  - `elev8-auth-gcp` module with `GcpAuthProvider` implementing `AuthProvider`
  - Application Default Credentials (ADC) support for automatic authentication
  - Explicit `GoogleCredentials` support for programmatic configuration
  - Service account JSON key file authentication
  - Workload Identity support (automatic via ADC when running in GKE)
  - Token caching with automatic refresh before expiry
  - Scoped to `https://www.googleapis.com/auth/cloud-platform`
  - `elev8-gke` module with `GkeClient` mirroring `EksClient` API
  - GKE cluster auto-discovery using `ClusterManagerClient`
  - Automatic endpoint and CA certificate extraction
  - All resource managers (pods, deployments, services, etc.)
  - Generic resource and cluster resource support
  - Dynamic client for runtime API discovery
  - Lombok `@Builder(toBuilder = true)` for flexible configuration
  - Google Cloud BOM integration for dependency management
  - Comprehensive unit test coverage
- **CRD Code Generator** - Maven plugin for generating type-safe Java classes from CRD schemas (Phase 8)
  - `elev8-codegen` Maven plugin module with goal prefix `crd`
  - `CrdParser` for parsing CRD YAML/JSON files with validation
  - `TypeMapper` for JSON Schema to Java type mapping (string, integer, boolean, array, object, etc.)
  - `SpecStatusGenerator` for generating spec and status classes with Lombok annotations
  - `ResourceClassGenerator` for generating resource classes extending `AbstractResource` with builder pattern
  - `ManagerClassGenerator` for generating resource manager classes (namespaced and cluster-scoped)
  - `SchemaToJavaGenerator` orchestrator for complete code generation pipeline
  - `CrdGeneratorMojo` Maven plugin entry point with configurable parameters
  - Support for nested types, arrays, maps, and date-time formats
  - Automatic generation of `@JsonProperty` annotations for non-standard field names
  - Plugin configuration: `crdDirectory`, `outputDirectory`, `targetPackage`, `generateManagers`, `useLombok`
  - Comprehensive unit test coverage (64 tests)
- **Dynamic Client for Untyped Resources** - Runtime API discovery and automatic resource manager creation (Phase 8)
  - `DiscoveryClient` interface for querying Kubernetes API server discovery endpoints
  - `DefaultDiscoveryClient` implementation for fetching API groups and resources from `/api` and `/apis`
  - `CachedDiscoveryClient` decorator with configurable TTL for caching discovery results
  - Discovery models: `APIGroup`, `APIGroupVersion`, `APIResource`, `APIResourceList`, `APIGroupList`
  - `DiscoveryException` for discovery-related errors with optional HTTP status code
  - `DynamicClient` interface for unified entry point to dynamic resource access
  - `DefaultDynamicClient` implementation using discovery + GenericResourceManager
  - Auto-discovery of resources by kind name (searches all API groups)
  - Convenience methods: `get()`, `list()`, `create()`, `update()`, `delete()` by kind
  - Separate methods for namespace-scoped (`resourcesForKind`) and cluster-scoped (`clusterResourcesForKind`) resources
  - Manager caching for efficient repeated access to the same resource types
  - Integration with `EksClient.dynamic()` factory method
  - Support for both core API resources (e.g., Pod, Service) and custom resources (e.g., CronTab)
  - Comprehensive unit test coverage (95+ tests across discovery and dynamic client)
- **Generic Custom Resource CRUD Operations** - Work with custom resources without compile-time type information (Phase 8)
  - `GenericResourceContext` immutable configuration for resource type with API group, version, kind, plural, and scope
  - `GenericKubernetesResource` flexible resource class using `Map<String, Object>` for spec/status
  - Dot-notation accessors for nested field access (e.g., `getSpec("config.database.host")`)
  - `@JsonAnySetter`/`@JsonAnyGetter` for handling unknown fields during deserialization
  - `GenericResourceManager` for namespace-scoped custom resources with full CRUD operations
  - `GenericClusterResourceManager` for cluster-scoped custom resources with full CRUD operations
  - Factory methods for core resources (`forCoreResource`) and custom resources (`forCustomResource`)
  - Convenience constructors for namespaced (`forNamespacedResource`) and cluster-scoped (`forClusterResource`) resources
  - Full support for list, get, create, update, delete, patch, and apply operations
  - Watch and stream support for real-time resource monitoring
  - Builder pattern with `fromContext()` for easy resource construction
  - Integration with `EksClient.genericResources()` and `EksClient.genericClusterResources()` factory methods
  - Comprehensive unit test coverage (50+ tests)
- **CustomResourceDefinition (CRD) Support** - Full support for Kubernetes CRD resources (Phase 8)
  - `CustomResourceDefinition` class with full spec, status, and builder support
  - `CustomResourceDefinitionSpec` with group, names, scope, and versions configuration
  - `CustomResourceDefinitionStatus` with conditions, accepted names, and stored versions
  - `CRDNames` for kind, plural, singular, short names, and categories
  - `CRDVersion` for version configuration with schema, subresources, and printer columns
  - `CRDCondition` implementing the `Condition` interface
  - `JSONSchemaProps` for OpenAPI v3 schema with Kubernetes extensions (x-kubernetes-*)
  - `CustomResourceValidation` wrapper for openAPIV3Schema
  - `CustomResourceSubresources` for status and scale subresource configuration
  - `CustomResourceSubresourceScale` for scale subresource fields
  - `CustomResourceColumnDefinition` for additional printer columns
  - `ValidationRule` for CEL validation rules
  - `ExternalDocumentation` for external docs reference
  - Conversion support with `CustomResourceConversion`, `WebhookConversion`, `WebhookClientConfig`, `ServiceReference`
  - `CustomResourceDefinitionManager` extending `AbstractClusterResourceManager` for CRUD operations
  - Full integration with `EksClient.customResourceDefinitions()` getter
  - Comprehensive unit test coverage (38 tests)
- Initial project structure with Maven multi-module setup
- Core abstractions for Kubernetes client
- IAM authentication module with automatic STS token generation
- OIDC/IRSA authentication support for EKS service accounts
- EKS Access Entries API integration
- Service account token authentication
- Resource plugin architecture
- StatefulSet resource support with full CRUD operations
- CronJob resource support with scheduling and job template management
- Cloud-native client with fluent builder API (EKS/GKE/AKS support)
- Comprehensive examples for all authentication modes
- GitHub Actions CI/CD pipeline
- Open source project setup (LICENSE, README, CONTRIBUTING)
- **Watch API** for real-time resource change monitoring (Phase 5)
  - `WatchEvent<T>` class for wrapping watch events (ADDED, MODIFIED, DELETED, BOOKMARK, ERROR)
  - `Watcher<T>` callback interface for consuming watch events
  - `WatchOptions` for configuring watch behavior (resource version, timeouts, selectors)
  - HTTP streaming support in `OkHttpClientImpl`
  - Watch methods in `AbstractResourceManager` and `AbstractClusterResourceManager`
  - Support for namespace-scoped and cluster-scoped watch operations
  - Label selector and field selector filtering
  - Bookmark support for efficient watch resumption
  - Comprehensive unit test coverage for watch functionality
- **Pod Log Streaming API** for real-time container log monitoring (Phase 5)
  - `LogOptions` class with fluent builder for log configuration
  - `LogWatch` callback interface for consuming log streams
  - Support for follow mode (tail -f behavior)
  - Tail lines option for viewing last N lines
  - Timestamp support in log output
  - Container-specific logs for multi-container pods
  - Previous container logs for debugging crashes
  - Time-based filtering (sinceSeconds, sinceTime)
  - Byte limit support for log retrieval
  - Integration with existing HTTP streaming infrastructure
  - Convenience methods in `PodManager` for single and multi-container pods
  - Comprehensive unit test coverage for log streaming functionality
- **Patch Operations** for efficient partial resource updates (Phase 5)
  - `PatchType` enum for three patch strategies (JSON_PATCH, MERGE_PATCH, STRATEGIC_MERGE_PATCH)
  - `PatchOptions` class with fluent builder for patch configuration
  - Support for JSON Patch (RFC 6902) with precise operation-based updates
  - Support for JSON Merge Patch (RFC 7396) with simple partial object updates
  - Support for Strategic Merge Patch (Kubernetes-specific) with smart array merging
  - Proper Content-Type headers for each patch type
  - Dry-run support for testing patches before applying
  - Field manager support for Server-side Apply compatibility
  - Force option for taking ownership of conflicting fields
  - Enhanced `KubernetesClient.patch()` method with PatchOptions support
  - Patch methods in `AbstractResourceManager` and `AbstractClusterResourceManager`
  - Support for both namespace-scoped and cluster-scoped resources
  - Comprehensive unit test coverage for patch functionality
- **Server-side Apply** for declarative resource management with field ownership (Phase 5)
  - `ApplyOptions` class with required fieldManager validation
  - APPLY_PATCH type added to `PatchType` enum (application/apply-patch+yaml)
  - Field-level ownership tracking for conflict prevention
  - Support for declarative resource management (GitOps patterns)
  - Factory methods for common use cases (of, dryRun, force)
  - Required fieldManager parameter ensures proper field ownership
  - Dry-run support for testing manifests before applying
  - Force option for taking ownership of conflicting fields
  - Automatic validation of required parameters
  - Conversion to PatchOptions for seamless integration
  - Apply methods in `AbstractResourceManager` and `AbstractClusterResourceManager`
  - Support for both namespace-scoped and cluster-scoped resources
  - Support for JSON and YAML manifest formats
  - Comprehensive unit test coverage for Server-side Apply functionality
  - Completes modern resource management toolkit
- **Exec into Pods** - Complete WebSocket-based command execution in containers (Phase 5)
  - Full bidirectional WebSocket streaming implementation
  - WebSocket client abstraction (`WebSocketClient` interface)
  - `OkHttpWebSocketClient` implementation using OkHttp's built-in WebSocket support
  - `WebSocketListener` callback interface for WebSocket events
  - `WebSocketException` for WebSocket-specific error handling
  - Channel multiplexing protocol via `ChannelMessage` class (Kubernetes exec protocol v4.channel.k8s.io)
  - Support for all Kubernetes exec channels (STDIN/STDOUT/STDERR/ERROR/RESIZE)
  - `ExecOptions` configuration class with fluent builder pattern
  - `ExecWatch` callback interface for consuming exec streams
  - `ExecWebSocketAdapter` bridging WebSocket events to ExecWatch callbacks
  - `ExecStatus` class for parsing JSON exit codes from ERROR channel
  - Factory methods for common exec scenarios (simple command, interactive shell, container-specific)
  - Complete WebSocket URL construction with query parameter encoding
  - Full `KubernetesClient.exec()` implementation with authentication and connection management
  - Functional `PodManager.exec()` implementation for executing commands
  - Exit code extraction and reporting via ERROR channel
  - STDIN write support for interactive commands
  - Binary message framing and channel demultiplexing
  - Support for TTY and non-TTY modes
  - Multi-container pod support with container parameter
  - Proper resource cleanup via AutoCloseable pattern
  - Comprehensive unit test coverage (46 tests total including WebSocket, exec, and adapter tests)
  - Foundation for WebSocket-based features (Port Forward, Attach)
- **Port Forwarding** - WebSocket-based network traffic tunneling to pod ports (Phase 5)
  - Full bidirectional WebSocket streaming for network traffic
  - `PortForwardOptions` configuration class with fluent builder pattern
  - Support for single and multiple ports in one connection
  - `PortForwardWatch` callback interface for consuming port forward streams
  - `PortForwardWebSocketAdapter` bridging WebSocket events to PortForwardWatch callbacks
  - Channel multiplexing protocol via Kubernetes port-forward protocol (v4.channel.k8s.io)
  - Channel pairs per port (even channels=data, odd channels=errors)
  - Factory methods for common scenarios (single port, multiple ports, container-specific)
  - Complete WebSocket URL construction with ports query parameters
  - Full `KubernetesClient.portForward()` implementation with authentication and connection management
  - Functional `PodManager.portForward()` implementation for port forwarding
  - Convenience method for single port forwarding
  - Binary data handling for any protocol (HTTP, TCP, etc.)
  - Port validation (1-65535 range)
  - Multi-container pod support with container parameter
  - Proper resource cleanup via AutoCloseable pattern
  - Comprehensive unit test coverage (40 tests: PortForwardOptions: 21, PortForwardWebSocketAdapter: 19)
  - Reuses existing WebSocket infrastructure from Exec feature
  - All 700 tests passing across project (up from 659)

### Changed

### Deprecated

### Removed

### Fixed

### Security

## [0.1.0] - TBD

Initial release - EKS-native Kubernetes Java client
