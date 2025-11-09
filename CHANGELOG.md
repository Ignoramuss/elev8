# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
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

### Changed

### Deprecated

### Removed

### Fixed

### Security

## [0.1.0] - TBD

Initial release - EKS-native Kubernetes Java client
