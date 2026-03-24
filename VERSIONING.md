# Versioning Strategy

Elev8 follows [Semantic Versioning 2.0.0](https://semver.org/) with version format `MAJOR.MINOR.PATCH`.

## Version Increments

| Change Type | Version Bump | Examples |
|---|---|---|
| Breaking changes to `@Stable` APIs | **MAJOR** | Removal, signature changes, behavioral changes |
| New features, modules, `@Beta` API changes, deprecations | **MINOR** | New resource manager, new auth provider |
| Bug fixes, documentation, performance improvements | **PATCH** | Fix serialization bug, update dependency |

## Pre-release Versions (0.x.y)

While the version is `0.x.y`, all APIs are subject to change. The bump to `1.0.0` signals a stability commitment for all `@Stable` APIs.

## API Stability Levels

Elev8 uses three annotations to communicate API stability:

### @Stable

Breaking changes only in **major** releases. Deprecated for at least one minor release before removal.

### @Beta

May change in **minor** releases, but not in patch releases. Suitable for production use with awareness of potential changes.

### @Alpha

May change in **any** release (minor or patch). Not recommended for production use.

## Deprecation Policy

1. A `@Deprecated` annotation is added with a `@deprecated` Javadoc tag explaining the replacement.
2. The deprecation persists for at least one minor release.
3. The deprecated API is removed in the next major release.

## Dependency Version Management

Use the `elev8-bom` module for consistent dependency versions:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.elev8</groupId>
            <artifactId>elev8-bom</artifactId>
            <version>${elev8.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

Then declare dependencies without explicit versions:

```xml
<dependency>
    <groupId>io.elev8</groupId>
    <artifactId>elev8-eks</artifactId>
</dependency>
```
