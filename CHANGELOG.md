# Changelog

All notable changes to the Grails Turbo Plugin will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Geb integration test support
  - GebConfig.groovy with Chrome and Firefox driver configurations
  - GebIntegrationSpec base class for integration tests
  - Example integration test demonstrating Geb usage
  - Integration test resources (application.yml, logback.xml)
  - TESTING.md guide for running unit and integration tests
  - Support for multiple browser environments (headless and headful modes)

## [0.1.0] - 2026-03-03

### Added
- Initial release of Grails Turbo Plugin
- TurboTagLib with support for Turbo Frames and Turbo Streams
  - `turbo:frame` tag for creating Turbo Frames
  - `turbo:stream` tag for creating Turbo Stream elements
  - `turbo:includeTurbo` tag for including Turbo JavaScript
  - `turbo:cableStreamSource` tag for WebSocket streaming
  - `turbo:pageRefresh` tag for page refresh configuration
- TurboController trait for easy controller integration
  - `isTurboRequest()` method
  - `isTurboFrameRequest()` method
  - `getTurboFrameId()` method
  - `acceptsTurboStream()` method
  - `renderTurboStream()` method
  - `respondWithTurbo()` method for multi-format responses
- TurboRequest class for request detection
  - Detect Turbo requests via headers
  - Detect Turbo Frame requests
  - Check for Turbo Stream acceptance
- TurboStreamBuilder for building Turbo Stream responses
  - Support for all Turbo Stream actions: append, prepend, replace, update, remove, before, after, refresh
  - Fluent API for chaining multiple actions
- TurboStreamService for service layer integration
  - Convenient methods for common stream operations
  - Template rendering support
- TurboInterceptor for automatic request processing
  - Adds Turbo attributes to all requests
  - Makes Turbo information available in GSP views
  - Handles MIME type registration
- TurboConstants for consistent header and MIME type usage
- TurboConfig for plugin configuration
- Automatic MIME type registration for Turbo Streams (`text/vnd.turbo-stream.html`)
- Comprehensive documentation
  - README with quick start guide
  - DEVELOPER_GUIDE with architecture and patterns
  - EXAMPLES with working code samples
- Unit tests for core functionality
  - TurboRequest tests
  - TurboStreamBuilder tests
- Example controller and views demonstrating plugin usage
- CDN integration for Turbo JavaScript (v8.0.4)

### Features
- Full Turbo Drive support for fast page navigation
- Turbo Frames for lazy-loading and scoped updates
- Turbo Streams for real-time partial page updates
- Automatic request detection and attribute injection
- Compatible with Grails 6.0.0 and above
- Works seamlessly with existing Grails applications
- No JavaScript build process required
- Progressive enhancement support

### Documentation
- Comprehensive README with examples
- Developer guide with patterns and best practices
- Example application demonstrating all features
- In-code documentation and JavaDoc comments
- Configuration guide

### Compatibility
- Grails 6.0.0+
- Groovy 3.0+
- Java 17+
- Hotwired Turbo 8.0.4

[0.1.0]: https://github.com/grails/grails-turbo/releases/tag/v0.1.0

