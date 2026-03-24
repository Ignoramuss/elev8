# Releasing Elev8

This document describes how to publish a new release of Elev8 to Maven Central.

## Prerequisites

- GPG key for signing artifacts
- Sonatype Central Portal account with publish permissions for `io.elev8`
- Repository secrets configured in GitHub:
  - `GPG_PRIVATE_KEY` - ASCII-armored GPG private key
  - `GPG_PASSPHRASE` - passphrase for the GPG key
  - `CENTRAL_USERNAME` - Sonatype Central Portal username
  - `CENTRAL_TOKEN` - Sonatype Central Portal API token

## Release Steps

### 1. Verify main is stable

Ensure all tests pass on `main` and the CI pipeline is green.

### 2. Set the release version

```bash
mvn versions:set -DnewVersion=X.Y.Z
git add -A
git commit -m "release: prepare version X.Y.Z"
```

### 3. Tag the release

```bash
git tag vX.Y.Z
git push origin main --tags
```

### 4. Automated deployment

Pushing the tag triggers the `release.yml` GitHub Actions workflow, which:

1. Builds the project
2. Signs artifacts with GPG
3. Deploys to Maven Central via the Sonatype Central Portal
4. Creates a GitHub Release with auto-generated notes

### 5. Bump to next development version

```bash
mvn versions:set -DnewVersion=X.Y.Z+1-SNAPSHOT
git add -A
git commit -m "release: prepare next development version"
git push origin main
```

### 6. Post-release

- Update `CHANGELOG.md` with the release notes
- Update `MIGRATION.md` if there are breaking changes
- Verify the artifacts are available on Maven Central

## Local Release Testing

To test the release profile locally (without deploying):

```bash
mvn -B clean verify -Prelease -Dgpg.skip=true
```
