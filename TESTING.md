# Testing Guide

This project uses a comprehensive testing approach with both unit tests and Geb-based integration tests.

## Test Structure

```
src/
  test/groovy/                    # Unit tests (Spock)
    grails/turbo/
      TurboControllerSpec.groovy
      TurboRequestSpec.groovy
      TurboStreamBuilderSpec.groovy
      ...
  
  integration-test/               # Integration tests (Geb + Spock)
    groovy/grails/turbo/
      ExampleGebSpec.groovy       # Example test
      GebIntegrationSpec.groovy   # Base class for Geb tests
    resources/
      GebConfig.groovy            # Geb configuration
      application.yml             # Test app configuration
      logback.xml                 # Test logging
    README.md                     # Detailed integration test guide
```

## Running Tests

### Unit Tests
```bash
# Run all unit tests
./gradlew test

# Run specific test class
./gradlew test --tests TurboControllerSpec

# Run tests with reports
./gradlew test
# Reports: build/reports/tests/test/index.html
```

### Integration Tests (Geb)
```bash
# Run all integration tests (headless Chrome)
./gradlew integrationTest

# Run with visible browser for debugging
./gradlew integrationTest -Dgeb.env=chromeHeadful

# Run with Firefox
./gradlew integrationTest -Dgeb.env=firefox

# Run specific test
./gradlew integrationTest --tests ExampleGebSpec

# Reports: build/reports/tests/integrationTest/index.html
```

### All Tests
```bash
./gradlew test integrationTest
```

## Writing Tests

### Unit Test Example
```groovy
package grails.turbo

import spock.lang.Specification

class MyComponentSpec extends Specification {
    
    void "test feature"() {
        given:
        def component = new MyComponent()
        
        when:
        def result = component.doSomething()
        
        then:
        result == expectedValue
    }
}
```

### Integration Test Example
```groovy
package grails.turbo

import grails.testing.mixin.integration.Integration

@Integration
class MyFeatureSpec extends GebIntegrationSpec {
    
    void "test turbo frame loads"() {
        when:
        go '/controller/action'
        
        then:
        $('turbo-frame#my-frame').displayed
        $('h1').text() == 'Expected Title'
    }
}
```

## Geb Browser Configuration

See `src/integration-test/resources/GebConfig.groovy` for browser configurations.

Available environments:
- `chrome` - Headless Chrome (default)
- `chromeHeadful` - Chrome with visible browser
- `firefox` - Headless Firefox
- `firefoxHeadful` - Firefox with visible browser

## WebDriver Setup

### macOS
```bash
brew install chromedriver geckodriver
```

### Linux
```bash
sudo apt-get install chromium-chromedriver firefox-geckodriver
```

## Continuous Integration

For CI environments, the default headless Chrome configuration works out of the box:
```bash
./gradlew clean test integrationTest
```

## Resources

- [Spock Framework](https://spockframework.org/)
- [Geb Manual](https://gebish.org/manual/current/)
- [Grails Testing Guide](https://docs.grails.org/latest/guide/testing.html)

