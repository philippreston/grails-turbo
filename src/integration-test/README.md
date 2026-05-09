# Integration Tests with Geb

This directory contains Geb-based integration tests for the Grails Turbo plugin.

## Running Tests

### Run all integration tests:
```bash
./gradlew integrationTest
```

### Run with a specific browser:

**Chrome (headless - default):**
```bash
./gradlew integrationTest
```

**Chrome (visible browser for debugging):**
```bash
./gradlew integrationTest -Dgeb.env=chromeHeadful
```

**Firefox (headless):**
```bash
./gradlew integrationTest -Dgeb.env=firefox
```

**Firefox (visible browser):**
```bash
./gradlew integrationTest -Dgeb.env=firefoxHeadful
```

## WebDriver Setup

### Chrome
Download ChromeDriver from https://chromedriver.chromium.org/
Or install via package manager:
```bash
# macOS
brew install chromedriver

# Linux
sudo apt-get install chromium-chromedriver
```

### Firefox
Download GeckoDriver from https://github.com/mozilla/geckodriver/releases
Or install via package manager:
```bash
# macOS
brew install geckodriver

# Linux
sudo apt-get install firefox-geckodriver
```

## Writing Tests

Create a test spec extending `GebIntegrationSpec`:

```groovy
package grails.turbo

import grails.testing.mixin.integration.Integration

@Integration
class MyFeatureSpec extends GebIntegrationSpec {
    
    void "test page loads"() {
        when:
        go '/controller/action'
        
        then:
        title == 'Expected Page Title'
        $('h1').text() == 'Expected Heading'
    }
}
```

## Geb Documentation

- [Geb Manual](https://gebish.org/manual/current/)
- [Geb API](https://gebish.org/manual/current/api/geb/)
- [Grails Testing Guide](https://docs.grails.org/latest/guide/testing.html)

## Configuration

- **GebConfig.groovy** - Geb configuration (browser settings, timeouts, etc.)
- **application.yml** - Integration test application configuration
- **logback.xml** - Logging configuration for tests

