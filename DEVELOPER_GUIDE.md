# Developer Guide - Grails Turbo Plugin

This guide provides detailed information for developers working on or with the Grails Turbo plugin.

## Architecture

### Core Components

#### 1. TurboRequest
Wraps HttpServletRequest to provide Turbo-specific functionality:
- Detects Turbo requests via `Turbo-Request` header
- Detects Turbo Frame requests via `Turbo-Frame` header
- Checks if client accepts Turbo Stream responses

#### 2. TurboStreamBuilder
Fluent API for building Turbo Stream responses:
- Chainable methods for stream actions
- Supports all Turbo Stream actions: append, prepend, replace, update, remove, before, after, refresh
- Generates proper Turbo Stream HTML

#### 3. TurboController Trait
Provides helper methods for controllers:
- Request detection methods
- `renderTurboStream()` for direct stream rendering
- `respondWithTurbo()` for format-aware responses

#### 4. TurboTagLib
GSP tag library for Turbo elements:
- `turbo:frame` - Create Turbo Frames
- `turbo:stream` - Create Turbo Stream elements
- `turbo:includeTurbo` - Include Turbo JavaScript from CDN (uses TurboConfig)

**Note:** Turbo JavaScript is loaded via the `<turbo:includeTurbo>` tag, NOT through the asset pipeline. This gives you full control over version, CDN URL, and when Turbo is loaded.

#### 5. TurboInterceptor
Request interceptor that:
- Adds Turbo request attributes to all requests
- Makes Turbo information available in GSP views
- Handles MIME type registration

#### 6. TurboStreamService
Service for programmatic Turbo Stream generation:
- Provides convenient methods for common operations
- Can be injected into services and controllers

### Request Flow

```
1. Client sends request with Turbo headers
   ↓
2. TurboInterceptor processes request
   - Creates TurboRequest wrapper
   - Adds attributes to request
   - Sets response format if needed
   ↓
3. Controller action executes
   - Can check isTurboRequest()
   - Can check isTurboFrameRequest()
   - Can render different responses
   ↓
4. Response sent
   - HTML response for regular requests
   - Turbo Stream for stream requests
   - Frame content for frame requests
```

## HTTP Headers

### Request Headers

| Header | Description | Example |
|--------|-------------|---------|
| `Turbo-Request` | Indicates request is from Turbo | `1` |
| `Turbo-Frame` | ID of requesting frame | `messages` |
| `Accept` | Includes Turbo Stream MIME type | `text/vnd.turbo-stream.html` |

### Response Headers

| Header | Description | Example |
|--------|-------------|---------|
| `Content-Type` | MIME type for response | `text/vnd.turbo-stream.html; charset=UTF-8` |

## MIME Types

The plugin registers the Turbo Stream MIME type:
- Format: `turbo_stream`
- MIME type: `text/vnd.turbo-stream.html`

This is automatically registered in `doWithApplicationContext()`.

## Controller Patterns

### Pattern 1: Simple Turbo Detection

```groovy
def index() {
    if (isTurboRequest()) {
        // Turbo-specific logic
    }
    [items: Item.list()]
}
```

### Pattern 2: Frame-Specific Rendering

```groovy
def show(Long id) {
    def item = Item.get(id)
    
    if (isTurboFrameRequest()) {
        // Render only frame content
        render template: 'item', model: [item: item]
        return
    }
    
    // Full page render
    [item: item]
}
```

### Pattern 3: Multi-Format Response

```groovy
def create() {
    def item = new Item(params)
    item.save()
    
    respondWithTurbo {
        html {
            flash.message = "Created successfully"
            redirect action: 'list'
        }
        turboStream {
            append 'items', render(template: 'item', model: [item: item])
            update 'item-count', "${Item.count()}"
        }
        json {
            render item as JSON
        }
    }
}
```

### Pattern 4: Direct Stream Rendering

```groovy
def update(Long id) {
    def item = Item.get(id)
    item.properties = params
    item.save()
    
    renderTurboStream {
        replace "item_${item.id}", render(template: 'item', model: [item: item])
    }
}
```

## GSP Patterns

### Pattern 1: Basic Frame

```gsp
<turbo:frame id="content">
    <p>This content can be updated independently</p>
</turbo:frame>
```

### Pattern 2: Lazy-Loading Frame

```gsp
<turbo:frame 
    id="expensive-content" 
    src="${createLink(action: 'loadContent')}"
    loading="lazy">
    <div class="spinner">Loading...</div>
</turbo:frame>
```

### Pattern 3: Frame Navigation

```gsp
<turbo:frame id="modal">
    <g:link action="showForm" data-turbo-frame="modal">
        Open in Frame
    </g:link>
</turbo:frame>
```

### Pattern 4: Break Out of Frame

```gsp
<turbo:frame id="content">
    <!-- This link navigates the whole page -->
    <g:link action="index" data-turbo-frame="_top">
        Back to Home
    </g:link>
</turbo:frame>
```

### Pattern 5: Multiple Stream Actions

```gsp
<turbo:stream action="append" target="items">
    <div class="item">New item</div>
</turbo:stream>

<turbo:stream action="update" target="counter">
    42
</turbo:stream>
```

## Testing

### Unit Testing TurboRequest

```groovy
void "test Turbo request detection"() {
    given:
    HttpServletRequest request = Mock(HttpServletRequest)
    request.getHeader('Turbo-Request') >> '1'
    
    when:
    TurboRequest turboRequest = new TurboRequest(request)
    
    then:
    turboRequest.isTurboRequest()
}
```

### Unit Testing TurboStreamBuilder

```groovy
void "test building stream response"() {
    given:
    TurboStreamBuilder builder = new TurboStreamBuilder()
    
    when:
    String result = builder
        .append('items', '<div>Item</div>')
        .build()
    
    then:
    result.contains('action="append"')
    result.contains('target="items"')
}
```

### Integration Testing Controller

```groovy
void "test Turbo Stream response"() {
    when:
    request.addHeader('Accept', 'text/vnd.turbo-stream.html')
    controller.create()
    
    then:
    response.contentType.startsWith('text/vnd.turbo-stream.html')
    response.text.contains('<turbo-stream')
}
```

## Best Practices

### 1. Frame IDs
- Use unique, descriptive IDs
- Use consistent naming conventions (e.g., `item_${id}`)
- Avoid special characters

### 2. Stream Actions
- Use `append`/`prepend` for adding to lists
- Use `replace` to swap entire elements
- Use `update` to change inner HTML
- Use `remove` to delete elements

### 3. Error Handling
- Always handle non-Turbo requests
- Provide fallbacks for JavaScript-disabled clients
- Use `respondWithTurbo` for graceful degradation

### 4. Performance
- Use lazy loading for expensive content
- Cache frame content when appropriate
- Minimize stream action count

### 5. Accessibility
- Ensure frame content is accessible
- Provide loading states
- Use semantic HTML

## Common Issues

### Issue 1: Frame Not Updating
**Problem**: Click on link but frame doesn't update

**Solution**: Ensure:
- Link has `data-turbo-frame` attribute matching frame ID
- Target action returns content with same frame ID
- Frame is not disabled

### Issue 2: Stream Response Not Working
**Problem**: Controller returns stream but page doesn't update

**Solution**: Check:
- Client sends `Accept: text/vnd.turbo-stream.html` header
- Response content-type is `text/vnd.turbo-stream.html`
- Target element exists in DOM

### Issue 3: Full Page Reload
**Problem**: Turbo Drive not working, page fully reloads

**Solution**: Verify:
- Turbo JavaScript is loaded
- Link doesn't have `data-turbo="false"`
- Not using `target="_blank"`

## Extending the Plugin

### Adding Custom Stream Actions

```groovy
class CustomTurboStreamBuilder extends TurboStreamBuilder {
    CustomTurboStreamBuilder highlight(String target) {
        stream('highlight', target, '')
        return this
    }
}
```

### Custom Tag Library

```groovy
class CustomTurboTagLib {
    static namespace = "cturbo"
    
    Closure customFrame = { attrs, body ->
        // Custom frame logic
        out << "<turbo-frame id=\"${attrs.id}\" class=\"custom\">"
        out << body()
        out << "</turbo-frame>"
    }
}
```

### Custom Interceptor

```groovy
class CustomTurboInterceptor {
    int order = 99 // Run before TurboInterceptor
    
    boolean before() {
        // Custom logic
        true
    }
}
```

## Resources

- [Turbo Handbook](https://turbo.hotwired.dev/handbook/introduction)
- [Turbo Reference](https://turbo.hotwired.dev/reference/drive)
- [Grails Documentation](https://docs.grails.org/)
- [Plugin Source](https://github.com/grails/grails-turbo)

## Contributing

1. Fork the repository
2. Create a feature branch
3. Write tests for your changes
4. Implement your feature
5. Ensure all tests pass
6. Submit a pull request

## License

Apache License 2.0

