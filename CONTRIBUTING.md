# Contributing to Grails Turbo Plugin

Thank you for your interest in contributing to the Grails Turbo Plugin! This document provides guidelines and instructions for contributing.

## Getting Started

### Prerequisites

- JDK 17 or later
- Grails 6.0.0 or later
- Git

### Setting Up Your Development Environment

1. Fork the repository on GitHub
2. Clone your fork:
   ```bash
   git clone https://github.com/YOUR_USERNAME/grails-turbo.git
   cd grails-turbo
   ```
3. Add the upstream repository:
   ```bash
   git remote add upstream https://github.com/grails/grails-turbo.git
   ```

## Development Workflow

### Running the Application

```bash
./gradlew bootRun
```

The application will start at http://localhost:8080

### Running Tests

```bash
./gradlew test
```

### Building the Plugin

```bash
./gradlew build
```

## Making Changes

### Branch Naming

Use descriptive branch names:
- `feature/add-streaming-support`
- `bugfix/frame-navigation-issue`
- `docs/update-readme`

### Commit Messages

Write clear, concise commit messages:
```
Add support for Turbo Stream refresh action

- Implement refresh action in TurboStreamBuilder
- Add tests for refresh functionality
- Update documentation with refresh examples
```

### Code Style

- Follow Groovy coding conventions
- Use meaningful variable and method names
- Add JavaDoc comments for public APIs
- Keep methods focused and concise
- Use `@CompileStatic` where appropriate for performance

### Testing

- Write unit tests for all new functionality
- Ensure existing tests pass
- Add integration tests for complex features
- Test with different Grails versions if possible

## Pull Request Process

1. Update your fork with the latest upstream changes:
   ```bash
   git fetch upstream
   git rebase upstream/main
   ```

2. Push your changes to your fork:
   ```bash
   git push origin feature/your-feature-name
   ```

3. Create a Pull Request on GitHub

4. Fill out the PR template with:
   - Description of changes
   - Related issues (if any)
   - Testing performed
   - Documentation updates

5. Wait for review and address feedback

## Adding New Features

### Tag Library Tags

To add a new tag to `TurboTagLib`:

```groovy
Closure myNewTag = { attrs, body ->
    // Validate required attributes
    if (!attrs.required) {
        throwTagError("Tag [myNewTag] is missing required attribute [required]")
    }
    
    // Generate output
    out << "<turbo-element>"
    out << body()
    out << "</turbo-element>"
}
```

### Stream Actions

To add a new stream action to `TurboStreamBuilder`:

```groovy
TurboStreamBuilder myAction(String target, String content) {
    stream('my-action', target, content)
    return this
}
```

### Controller Methods

To add new methods available to controllers via the trait:

```groovy
// In TurboController trait
String getMyTurboInfo() {
    return getTurboRequest().getMyInfo()
}
```

## Documentation

### Updating Documentation

When adding features, update:
- README.md - Main documentation
- DEVELOPER_GUIDE.md - Technical details
- EXAMPLES.md - Working examples
- CHANGELOG.md - Version history

### Code Comments

- Use JavaDoc for public APIs
- Explain "why" not "what" in comments
- Keep comments up to date with code changes

## Reporting Issues

### Bug Reports

Include:
- Grails version
- Plugin version
- Steps to reproduce
- Expected behavior
- Actual behavior
- Stack traces or error messages

### Feature Requests

Include:
- Use case description
- Proposed API or usage
- Benefits to users
- Potential implementation approach

## Release Process

(For maintainers)

1. Update version in `gradle.properties`
2. Update CHANGELOG.md
3. Create a git tag: `git tag v0.2.0`
4. Push tag: `git push origin v0.2.0`
5. Publish to Maven Central or Grails Plugin Portal
6. Create GitHub release with notes

## Code of Conduct

- Be respectful and inclusive
- Welcome newcomers
- Accept constructive criticism gracefully
- Focus on what's best for the community
- Show empathy towards others

## Questions?

- Open an issue for questions about contributing
- Join the Grails Slack channel
- Check existing issues and pull requests

## License

By contributing, you agree that your contributions will be licensed under the Apache License 2.0.

Thank you for contributing to Grails Turbo!

