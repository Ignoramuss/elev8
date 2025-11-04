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

### Changed

### Deprecated

### Removed

### Fixed

### Security

## [0.1.0] - TBD

Initial release - EKS-native Kubernetes Java client
