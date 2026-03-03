# Grails Turbo Plugin

A Grails plugin that integrates [Hotwired Turbo](https://turbo.hotwired.dev/) to provide single-page application behavior with server-rendered HTML.

This plugin is inspired by [turbo-rails](https://github.com/hotwired/turbo-rails) and brings the same powerful features to Grails applications.

## Features

- **Turbo Drive**: Fast page navigation without full page reloads
- **Turbo Frames**: Lazy-loading and scoped page updates
- **Turbo Streams**: Real-time partial page updates over HTTP and WebSocket
- **Easy Integration**: Tag libraries, traits, and services for seamless Grails integration
- **Request Detection**: Automatic detection of Turbo requests and frame requests

## Installation

Add the plugin to your `build.gradle`:

```gradle
dependencies {
    implementation 'grails.turbo:grails-turbo:0.1'
}
```

## Quick Start

### 1. Include Turbo in Your Layout

The plugin automatically includes Turbo JavaScript. You can also manually include it in your layout:

```gsp
<!DOCTYPE html>
<html>
<head>
    <title><g:layoutTitle default="My App"/></title>
    <asset:javascript src="application.js"/>
    <turbo:includeTurbo/>
</head>
<body>
    <g:layoutBody/>
</body>
</html>
```

### 2. Using Turbo Frames

Wrap parts of your page in Turbo Frames for scoped updates:

```gsp
<turbo:frame id="messages">
    <g:each in="${messages}" var="message">
        <div>${message.text}</div>
    </g:each>
</turbo:frame>
```

Link to update just that frame:

```gsp
<g:link controller="message" action="list" params="[page: 2]">
    Next Page
</g:link>
```

### 3. Using Turbo Streams

In your controller, use the `TurboController` trait:

```groovy
import grails.turbo.TurboController

class MessageController implements TurboController {
    
    def create() {
        def message = new Message(params)
        message.save()
        
        respondWithTurbo {
            html { 
                redirect action: 'list' 
            }
            turboStream {
                append 'messages', render(template: 'message', model: [message: message])
            }
        }
    }
}
```

Or render Turbo Streams directly:

```groovy
def update() {
    def message = Message.get(params.id)
    message.properties = params
    message.save()
    
    renderTurboStream {
        replace "message_${message.id}", render(template: 'message', model: [message: message])
    }
}
```

## Tag Library Reference

### turbo:frame

Creates a Turbo Frame element:

```gsp
<turbo:frame id="cart" src="${createLink(controller: 'cart', action: 'show')}">
    Loading cart...
</turbo:frame>
```

**Attributes:**
- `id` (required): Unique identifier for the frame
- `src`: URL to lazy-load content
- `loading`: 'eager' or 'lazy' (default: 'eager')
- `target`: Target frame for navigation
- `autoscroll`: Auto-scroll to frame on update

### turbo:stream

Creates a Turbo Stream element (typically used in views):

```gsp
<turbo:stream action="append" target="messages">
    <div class="message">${message.text}</div>
</turbo:stream>
```

**Attributes:**
- `action` (required): append, prepend, replace, update, remove, before, after
- `target`: Target element ID
- `targets`: CSS selector for multiple targets

### turbo:includeTurbo

Includes the Turbo JavaScript library:

```gsp
<turbo:includeTurbo version="8.0.4"/>
```

## Controller Support

### TurboController Trait

Implement this trait in your controllers for Turbo support:

```groovy
import grails.turbo.TurboController

class MyController implements TurboController {
    
    def index() {
        // Check if request is from Turbo
        if (isTurboRequest()) {
            // Handle Turbo-specific logic
        }
        
        // Check if request is from a Turbo Frame
        if (isTurboFrameRequest()) {
            def frameId = getTurboFrameId()
            // Render only the frame content
        }
    }
}
```

### Available Methods

- `isTurboRequest()`: Returns true if request is from Turbo
- `isTurboFrameRequest()`: Returns true if request is from a Turbo Frame
- `getTurboFrameId()`: Returns the ID of the requesting frame
- `acceptsTurboStream()`: Returns true if client accepts Turbo Stream responses
- `renderTurboStream(Closure)`: Render a Turbo Stream response
- `respondWithTurbo(Closure)`: Respond with different formats including Turbo Streams

### TurboStreamBuilder Methods

When using `renderTurboStream` or `respondWithTurbo`, you have access to:

```groovy
renderTurboStream {
    append 'target-id', '<div>New content</div>'
    prepend 'target-id', '<div>New content</div>'
    replace 'target-id', '<div>Replacement</div>'
    update 'target-id', '<div>New inner HTML</div>'
    remove 'target-id'
    before 'target-id', '<div>Before content</div>'
    after 'target-id', '<div>After content</div>'
    refresh() // Trigger page refresh
}
```

## Service Support

### TurboStreamService

Inject and use the service for building Turbo Stream responses:

```groovy
class MyService {
    TurboStreamService turboStreamService
    
    String buildUpdate(Message message) {
        return turboStreamService.builder()
            .append('messages', renderMessage(message))
            .update('unread-count', message.unreadCount.toString())
            .build()
    }
}
```

## Request Detection

The plugin adds an interceptor that automatically detects Turbo requests and adds attributes:

In GSP views:
```gsp
<g:if test="${isTurboRequest}">
    <!-- Turbo-specific content -->
</g:if>

<g:if test="${isTurboFrameRequest}">
    <!-- Frame: ${turboFrameId} -->
</g:if>
```

## Configuration

Configure the plugin in `application.yml`:

```yaml
grails:
    turbo:
        turboVersion: '8.0.4'
        autoInclude: true
        useCdn: true
        enableDrive: true
        enableFrames: true
        enableStreams: true
```

## Advanced Usage

### Custom Turbo Stream Actions

```groovy
renderTurboStream {
    stream('custom-action', 'target-id', '<div>Content</div>')
}
```

### Multiple Stream Actions

```groovy
renderTurboStream {
    append 'messages', render(template: 'message', model: [message: newMessage])
    update 'message-count', "${Message.count()}"
    remove "message_${oldMessage.id}"
}
```

### Conditional Responses

```groovy
respondWithTurbo {
    html {
        render view: 'index', model: [messages: messages]
    }
    turboStream {
        update 'messages', render(template: 'messages', collection: messages)
    }
    json {
        render messages as JSON
    }
}
```

## Examples

See the example application in the `examples` directory for complete working examples of:
- CRUD operations with Turbo Streams
- Real-time updates
- Lazy-loaded frames
- Nested frames
- Form submissions with Turbo

## Resources

- [Hotwired Turbo Documentation](https://turbo.hotwired.dev/)
- [Turbo Handbook](https://turbo.hotwired.dev/handbook/introduction)
- [turbo-rails](https://github.com/hotwired/turbo-rails)
- [Grails Documentation](https://docs.grails.org/)

## Contributing

Contributions are welcome! Please open an issue or submit a pull request.

## License

Apache License 2.0

## Credits

Inspired by [turbo-rails](https://github.com/hotwired/turbo-rails) and [micronaut-views-turbo](https://github.com/micronaut-projects/micronaut-views).
