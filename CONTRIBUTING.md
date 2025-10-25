# Contributing to Elev8

Thank you for your interest in contributing to Elev8! This document provides guidelines and information for contributors.

## Code of Conduct

This project adheres to a code of conduct. By participating, you are expected to uphold this code. Please report unacceptable behavior to the project maintainers.

## How to Contribute

### Reporting Bugs

Before creating bug reports, please check existing issues to avoid duplicates. When creating a bug report, include:

- A clear and descriptive title
- Steps to reproduce the issue
- Expected behavior
- Actual behavior
- Your environment (Java version, AWS SDK version, OS, etc.)
- Relevant logs or error messages

### Suggesting Enhancements

Enhancement suggestions are tracked as GitHub issues. When creating an enhancement suggestion, include:

- A clear and descriptive title
- A detailed description of the proposed functionality
- Explain why this enhancement would be useful
- List any alternatives you've considered

### Pull Requests

1. **Fork the Repository**: Create your own fork of the code
2. **Create a Branch**: Create a branch for your changes (`git checkout -b feature/my-new-feature`)
3. **Make Your Changes**: Implement your changes following our coding standards
4. **Write Tests**: Add tests for your changes
5. **Run Tests**: Ensure all tests pass (`mvn clean test`)
6. **Commit Your Changes**: Use clear and meaningful commit messages
7. **Push to Your Fork**: Push your changes to your fork
8. **Submit a Pull Request**: Create a pull request to the main repository

#### Pull Request Guidelines

- Follow the existing code style and conventions
- Write meaningful commit messages
- Include tests for new functionality
- Update documentation as needed
- Keep pull requests focused on a single feature or bug fix
- Reference any related issues in your pull request description

## Development Setup

### Prerequisites

- Java 17 or later
- Maven 3.8+
- Git
- An AWS account with EKS cluster access (for integration testing)

### Building the Project

```bash
git clone https://github.com/yourusername/elev8.git
cd elev8
mvn clean install
```

### Running Tests

```bash
# Run all tests
mvn clean test

# Run tests for a specific module
cd elev8-auth-iam
mvn test

# Run with coverage
mvn clean test jacoco:report
```

### Code Style

- Follow standard Java conventions
- Use meaningful variable and method names
- Add Javadoc comments for public APIs
- Keep methods focused and concise
- Maximum line length: 120 characters

### Project Structure

```
elev8/
├── elev8-core/              # Core abstractions - no AWS dependencies
├── elev8-auth-iam/          # IAM authentication implementation
├── elev8-auth-oidc/         # OIDC/IRSA implementation
├── elev8-auth-accessentries/# Access Entries API integration
├── elev8-auth-token/        # Token-based auth
├── elev8-resources/         # Kubernetes resource models
├── elev8-eks/               # EKS-specific client and features
└── examples/                # Usage examples
```

### Module Guidelines

- **elev8-core**: Should remain cloud-agnostic
- **Auth modules**: Should be independent and pluggable
- **elev8-eks**: Can depend on all auth modules
- **examples**: Should demonstrate real-world usage

## Testing Guidelines

### Unit Tests

- Write unit tests for all new code
- Use JUnit 5 for test framework
- Use Mockito for mocking
- Use AssertJ for fluent assertions
- Aim for >80% code coverage

### Integration Tests

- Mark integration tests with `@Tag("integration")`
- Integration tests should clean up resources
- Document any AWS permissions required

### Test Naming

```java
@Test
void shouldGenerateValidStsToken() {
    // Given
    // When
    // Then
}
```

## Documentation

- Add Javadoc for all public APIs
- Update README.md for user-facing changes
- Add examples for new features
- Keep documentation concise and accurate

## Commit Messages

Follow these guidelines for commit messages:

```
<type>: <subject>

<body>

<footer>
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Maintenance tasks

**Example:**
```
feat: add support for EKS access entries migration

Implement automatic migration from aws-auth ConfigMap to
Access Entries API, including validation and rollback.

Closes #123
```

## Release Process

Releases are managed by project maintainers. The process includes:

1. Version bump
2. Changelog update
3. Tag creation
4. Maven Central deployment
5. GitHub release with notes

## Questions?

If you have questions, feel free to:

- Open an issue
- Start a discussion
- Contact the maintainers

Thank you for contributing to Elev8!
