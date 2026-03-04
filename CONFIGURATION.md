# Grails Turbo Plugin Configuration

## Configuration in application.yml

You can configure the Turbo plugin by adding the following to your `grails-app/conf/application.yml`:

```yaml
grails:
    plugin:
        turbo:
            # Version of Turbo JavaScript library to use
            # Default: 8.0.4
            turboVersion: '8.0.4'
            
            # Whether to automatically include Turbo JavaScript
            # Default: true
            autoInclude: true
            
            # Whether to use CDN for Turbo JavaScript
            # Default: true
            useCdn: true
            
            # Custom CDN URL (if not using default)
            # Default: https://cdn.jsdelivr.net/npm/@hotwired/turbo
            cdnUrl: 'https://cdn.jsdelivr.net/npm/@hotwired/turbo'
            
            # Enable/disable Turbo Drive (automatic page navigation)
            # Default: true
            enableDrive: true
            
            # Enable/disable Turbo Frames (scoped page updates)
            # Default: true
            enableFrames: true
            
            # Enable/disable Turbo Streams (real-time updates)
            # Default: true
            enableStreams: true
            
            # Custom meta options to add to pages
            # These will be added as <meta name="turbo-{key}" content="{value}">
            metaOptions:
                cache-control: 'no-cache'
                prefetch: 'false'
```

## Configuration Examples

### Example 1: Disable Turbo Drive (traditional page loads)

```yaml
grails:
    plugin:
        turbo:
            enableDrive: false
            enableFrames: true
            enableStreams: true
```

### Example 2: Use a different Turbo version

```yaml
grails:
    plugin:
        turbo:
            turboVersion: '7.3.0'
```

### Example 3: Use a custom/self-hosted Turbo

```yaml
grails:
    plugin:
        turbo:
            useCdn: false
            cdnUrl: '/assets/turbo'
```

### Example 4: Minimal - Only Turbo Streams

```yaml
grails:
    plugin:
        turbo:
            enableDrive: false
            enableFrames: false
            enableStreams: true
```

### Example 5: Custom meta tags

```yaml
grails:
    plugin:
        turbo:
            metaOptions:
                cache-control: 'no-preview'
                prefetch: 'true'
                progress-bar-delay: '500'
```

## Using Configuration in Code

### In Controllers (via TurboConfig)

```groovy
class MyController {
    TurboConfig turboConfig
    
    def index() {
        if (turboConfig.enableDrive) {
            // Turbo Drive is enabled
        }
        
        if (turboConfig.enableStreams) {
            // Use Turbo Streams
        }
    }
}
```

### In GSP Templates (via turbo:includeTurbo tag)

The `turbo:includeTurbo` tag automatically uses the configuration:

```gsp
<head>
    <!-- Uses configured version and CDN URL -->
    <turbo:includeTurbo/>
</head>
```

Or override specific values:

```gsp
<head>
    <!-- Override version for this page -->
    <turbo:includeTurbo version="7.3.0"/>
</head>
```

## Configuration Properties Reference

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `turboVersion` | String | `'8.0.4'` | Version of Turbo JavaScript library |
| `autoInclude` | Boolean | `true` | Auto-include Turbo JS in pages |
| `useCdn` | Boolean | `true` | Use CDN for Turbo JavaScript |
| `cdnUrl` | String | `'https://cdn.jsdelivr.net/npm/@hotwired/turbo'` | CDN URL for Turbo |
| `enableDrive` | Boolean | `true` | Enable Turbo Drive (fast navigation) |
| `enableFrames` | Boolean | `true` | Enable Turbo Frames (scoped updates) |
| `enableStreams` | Boolean | `true` | Enable Turbo Streams (real-time) |
| `metaOptions` | Map | `[:]` | Custom meta tags to add |

## How Configuration is Applied

1. **Plugin loads with defaults** - All values have sensible defaults
2. **application.yml is read** - Custom configuration overrides defaults
3. **Configuration is injected** - Available via `TurboConfig` bean
4. **Tags use configuration** - `<turbo:includeTurbo>` uses configured version/URL
5. **Runtime accessible** - Can inject TurboConfig in controllers/services

## Environment-Specific Configuration

You can configure different settings per environment:

```yaml
environments:
    development:
        grails:
            plugin:
                turbo:
                    turboVersion: '8.0.4'
                    metaOptions:
                        cache-control: 'no-cache'
    
    production:
        grails:
            plugin:
                turbo:
                    turboVersion: '8.0.4'
                    metaOptions:
                        cache-control: 'must-revalidate'
                        prefetch: 'true'
    
    test:
        grails:
            plugin:
                turbo:
                    # Disable Drive in tests for predictable behavior
                    enableDrive: false
```

## Disabling Turbo Features

### Disable everything except Streams

Perfect for API-style apps that only need real-time updates:

```yaml
grails:
    plugin:
        turbo:
            enableDrive: false
            enableFrames: false
            enableStreams: true
```

### Disable Turbo completely

If you need to temporarily disable Turbo without removing the plugin:

```yaml
grails:
    plugin:
        turbo:
            autoInclude: false
            enableDrive: false
            enableFrames: false
            enableStreams: false
```

## Checking Configuration at Runtime

```groovy
class MyService {
    TurboConfig turboConfig
    
    void doSomething() {
        // Get version
        def version = turboConfig.turboVersion
        println "Using Turbo ${version}"
        
        // Check if Drive is enabled
        if (turboConfig.enableDrive) {
            // Fast navigation is available
        }
        
        // Get CDN URL
        def cdnUrl = turboConfig.cdnUrl
        
        // Check custom meta options
        def metaOptions = turboConfig.metaOptions
    }
}
```

## Configuration in Tests

In your test configuration (`application-test.yml`):

```yaml
grails:
    plugin:
        turbo:
            # Disable Drive in tests for predictable behavior
            enableDrive: false
            # Keep Frames and Streams for testing
            enableFrames: true
            enableStreams: true
```

## Default Configuration

If you don't provide any configuration, these defaults are used:

- **turboVersion**: `'8.0.4'`
- **autoInclude**: `true`
- **useCdn**: `true`
- **cdnUrl**: `'https://cdn.jsdelivr.net/npm/@hotwired/turbo'`
- **enableDrive**: `true`
- **enableFrames**: `true`
- **enableStreams**: `true`
- **metaOptions**: `[:]` (empty)

These defaults provide a fully-featured Turbo setup out of the box!

