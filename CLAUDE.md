- Always add unit tests when you add new code unless it is configuration or other files where unit tests make no sense
- Always create a new feature branch (e.g., `feat/aks-support`) from `main` for every new roadmap item before starting implementation. Never add unrelated roadmap items to an existing feature branch.
- When a feature branch depends on code from another unmerged branch, MUST NOT merge that branch into the feature branch. Instead, MUST first rebase the feature branch onto `main` after the dependency is merged, or create the feature branch from the dependency branch directly and set the PR base to that branch. Merging unmerged branches creates divergent histories that cause conflicts when both branches land on `main`.
- Before creating a PR, MUST run `git fetch origin main` and verify the feature branch is up to date with `origin/main`. If `main` has advanced, rebase onto it before pushing.

## Module Structure

- `elev8-core` - HTTP client abstraction, JSON serialization, base interfaces
- `elev8-auth-*` - Authentication providers (IAM, OIDC, Token, GCP, Azure)
- `elev8-resources` - Kubernetes resource types and manager interfaces
- `elev8-cloud` - Abstract cloud client base class
- `elev8-eks`, `elev8-gke`, `elev8-aks` - Cloud-specific Kubernetes clients
- `elev8-reactor` - Reactive/async wrappers using Project Reactor (Mono/Flux)
- `elev8-codegen` - Maven plugin for CRD code generation

## Reactive Module Patterns

When working on `elev8-reactor`:
- Use `Mono.fromCallable()` with `Schedulers.boundedElastic()` for wrapping sync calls
- Use `Mono.fromCallable(() -> { ...; return null; }).then()` for void methods that throw checked exceptions
- Use `Flux.create()` with a bridging `Watcher` for watch/stream operations
- Use `doReturn().when()` instead of `when().thenReturn()` for mocking methods that throw checked exceptions
- Add `throws ResourceException` to test methods when stubbing ResourceManager methods