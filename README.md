# Grails Turbo Plugin

A Grails plugin that integrates [Hotwired Turbo](https://turbo.hotwired.dev/) to provide single-page application behavior with server-rendered HTML.

This plugin is inspired by [turbo-rails](https://github.com/hotwired/turbo-rails) and brings the same powerful features to Grails applications.

## Features

- **Turbo Drive**: Fast page navigation without full page reloads
- **Turbo Frames**: Lazy-loading and scoped page updates
- **Turbo Streams**: Real-time partial page updates over HTTP and WebSocket
- **Action Cable–compatible WebSocket**: Optional **`/cable`** endpoint for **`turbo-cable-stream-source`** (Turbo Streams subscriptions)
- **Easy Integration**: Tag libraries, traits, and services for seamless Grails integration
- **Request Detection**: Automatic detection of Turbo requests and frame requests

## Installation

Add the dependency in `build.gradle` (coordinates match this project’s `group` and `version` in [`gradle.properties`](gradle.properties); adjust if you consume a published artifact with different naming):

```gradle
dependencies {
    implementation 'grails.turbo:grails-turbo:0.2.0'
}
```

Register the Turbo Stream MIME type if it is not already present (the plugin also tries to register it at runtime):

```yaml
grails:
  mime:
    types:
      turbo_stream:
      - text/vnd.turbo-stream.html
```

## Quick Start

### 1. Include Turbo in your layout

Turbo is **not** injected automatically. Use **`turbo:includeTurbo`** in your layouts (the plugin demo uses the [Asset Pipeline](https://github.com/bertramdev/asset-pipeline) but Turbo itself is loaded from the CDN by default, not from `application.js`).

**Recommended** (matches **turbo-rails** load order: metas in `<head>`, script after DOM, before your app JS):

```gsp
<!DOCTYPE html>
<html>
<head>
    <title><g:layoutTitle default="My App"/></title>
    <turbo:includeTurbo metasOnly="true"/>
    <g:layoutHead/>
</head>
<body>
    <g:layoutBody/>
    <turbo:includeTurbo scriptsOnly="true"/>
    <asset:javascript src="application.js"/>
</body>
</html>
```

**Single tag** (metas + script together): `<turbo:includeTurbo/>` — still place the script **before** `application.js` if that bundle depends on Turbo.

When **`enableStreams`** and **`enableActionCable`** are both true, the tag loads **`@hotwired/turbo-rails`** from jsDelivr as a **`type="module"`** script (`turbo.min.js`: Turbo, Action Cable, and `turbo-cable-stream-source` support). Otherwise it loads **`@hotwired/turbo`** only (`turbo.es2017-esm.js`). With **`useCdn: false`**, no script is emitted (host the bundle yourself via the Asset Pipeline or static assets).

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

In your controller, implement **`TurboController`** and use **`respondWithTurbo`** (or **`renderTurboStream`**). For **`renderTemplate()`** / fragment strings inside those closures, declare **`def groovyPageRenderer`** on the controller (Grails injects it; the trait documents this requirement).

```groovy
import grails.turbo.TurboController

class MessageController implements TurboController {

    def groovyPageRenderer

    def create() {
        def message = new Message(params)
        message.save()
        
        respondWithTurbo {
            html { 
                redirect action: 'list', status: 303
            }
            turboStream {
                append 'messages', render(template: 'message', model: [message: message])
            }
        }
    }
}
```

Or render Turbo Streams directly (same **`groovyPageRenderer`** requirement if you use **`render(...)`** for templates):

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

> **Note:** Without `groovyPageRenderer` declared on the controller, `render(template: ...)` inside `renderTurboStream` / `respondWithTurbo` may not work as expected.

## Tag Library Reference

### turbo:frame

Creates a Turbo Frame element:

```gsp
<turbo:frame id="cart" src="${createLink(controller: 'cart', action: 'show')}">
    Loading cart...
</turbo:frame>
```

Rails-style `dom_id`: use `bean` (or composite `ids`) instead of hand-building the id:

```gsp
<turbo:frame bean="${message}" class="card">...</turbo:frame>
<turbo:frame ids="${[userId, 'tray']}" src="...">...</turbo:frame>
```

`TurboTagLib.turboDomId(bean)` produces ids like `message_42` or `new_message` when `id` is null (similar to `ActionView::RecordIdentifier#dom_id`).

**Attributes:**
- `id`: Frame element id (omit if `bean` or `ids` sets it)
- `bean`: Domain / map-like object for derived id (`simpleName_id` or `new_simpleName`)
- `ids`: List of parts joined with `_` for composite ids
- `src`: URL to lazy-load content
- `loading`: 'eager' or 'lazy' (default: 'eager')
- `target`: Target frame for navigation
- `busy`, `disabled`: Frame element boolean attributes (see taglib)
- `autoscroll`: Auto-scroll to frame on update
- Any other attributes (`class`, `style`, `data-*`, `aria-*`, etc.) are passed through to `<turbo-frame>` (or to the fallback `<div>` when `enableFrames: false`).

#### Minimal layout for frame requests (turbo-rails parity)

turbo-rails swaps in a minimal layout for `Turbo-Frame` requests. This plugin ships [`grails-app/views/layouts/turbo_frame.gsp`](grails-app/views/layouts/turbo_frame.gsp) (minimal `<head>` + `<g:layoutBody/>`). In your application, use a **dynamic layout** closure that checks the `TurboInterceptor` flag:

```groovy
static layout = { ->
    request.getAttribute('isTurboFrameRequest') ? 'turbo_frame' : 'main'
}
```

(Exact `layout` syntax may vary by Grails version; see Grails documentation.) Alternatively, `render template:` / branch in the action when `isTurboFrameRequest()` (from `TurboController`) is true. For HTTP caching, vary ETags or cache keys so full-page responses and frame responses are not mixed up (Rails adds an etag segment for frame requests).

### turbo:stream

Creates a Turbo Stream element (typically used in views):

```gsp
<turbo:stream action="append" target="messages">
    <div class="message">${message.text}</div>
</turbo:stream>
```

**Attributes:**
- `action` (required): `append`, `prepend`, `replace`, `update`, `remove`, `before`, `after`, `refresh`
- `target`: Target element ID (`refresh` ignores this; Turbo does not scope refresh to an element ID)
- `targets`: CSS selector for multiple targets
- `morph`: on `replace` / `update`, sets `method="morph"`; on **`refresh`**, selects a morphing page refresh (`method="morph"` on the stream)
- **`refresh` extras:** `requestId` (maps to `request-id` on the element), `scroll` (e.g. `preserve` / `reset`)
- Other HTML attributes (`class`, `style`, `data-*`, …) pass through **except** conflicting reserved names

For **`action="refresh"`**, the tag emits **no** `<template>` body (same as **`remove`**). Optionally use `<turbo:stream action="refresh"/>`.

### turbo:streamFrom

Subscribes to Turbo Streams over Action Cable (Rails **`turbo_stream_from`** style). Renders **`turbo-cable-stream-source`** for channel **`Turbo::StreamsChannel`** with a **signed** stream name (`signed-stream-name` attribute).

```gsp
<turbo:streamFrom streamables="${[accountKey, message]}"/>
```

- **`streamables`** (required): strings and/or domain objects (see `TurboStreamName`); objects are encoded with `gid://{globalIdApp}/…` semantics.
- Configure a non-empty **`streamSigningSecret`** (`grails.plugin.turbo`). Signing matches Rails **`ActiveSupport::MessageVerifier`** (SHA256 digest, JSON serialization); implementation is **`TurboMessageVerifier`**.
- With **`enableStreams: false`**, the tag emits an HTML comment and no subscription element.
- With **`enableStreams: true`** and **`enableActionCable: false`**, HTTP Turbo Streams still work, but **`turbo:streamFrom`** and the Action Cable meta tag are skipped; **`includeTurbo`** loads **`@hotwired/turbo`** only (no Rails cable bundle).
- With **`enableActionCable: true`**, the plugin exposes a WebSocket endpoint (default **`/cable`**) compatible with **`@rails/actioncable`**. Use both **`enableStreams`** and **`enableActionCable`** for **`turbo-cable-stream-source`** unless you supply your own publisher.

Prefer **`turbo:streamFrom`** over **`turbo:cableStreamSource`**, which is deprecated for manually wiring the same markup.

### turbo:includeTurbo

Loads Turbo from the CDN according to configuration (see Quick Start).

**Attributes:**
- **`metasOnly="true"`** — emit only `<meta>` tags (e.g. `action-cable-url`, `turbo-*` from `metaOptions`, drive disable) for the `<head>`.
- **`scriptsOnly="true"`** — emit only the script tag(s) at the end of `<body>`.
- **`version`** — Turbo version; also used as default **`turbo-rails`** version unless **`turboRailsVersion`** is set.
- **`turboRailsVersion`** — override npm version for **`@hotwired/turbo-rails`** when streams + cable are enabled.
- **`cdnUrl`** — base URL for **`@hotwired/turbo`** ESM only (not used for the turbo-rails bundle).

Relative **`actionCablePath`** / **`actionCableUrl`** values are turned into a full **`ws:`** / **`wss:`** URL in the `action-cable-url` meta when possible so browsers connect on non-default ports.

```gsp
<turbo:includeTurbo version="8.0.4" turboRailsVersion="8.0.4"/>
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

### Redirects after Turbo form posts

Turbo Drive submits forms via `fetch`. For **HTML** branches that **redirect** after POST/PUT/PATCH/DELETE, use **303 See Other** so the browser follows up with **GET** (RFC 9110 semantics; avoids repeating the mutation method). Example:

```groovy
respondWithTurbo {
    html { redirect(action: 'index', status: 303) }
    turboStream { /* ... */ }
}
```

Without an explicit status, redirects may still work in common cases, but **explicit 303 matches turbo-rails best practice.**

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
    refresh()                                                     // Minimal page refresh stream
    refresh(requestId: 'abc', morph: true, scroll: 'preserve')  // Turbo 8 extras
}
```

## Service Support

### TurboStreamService

Inject the service to build stream markup or **broadcast** over the configured **`TurboStreamPublisher`** (default: in-process Action Cable fan-out):

```groovy
class MyService {
    TurboStreamService turboStreamService

    String buildAppend(String html) {
        turboStreamService.builder()
            .append('messages', html)
            .update('unread-count', '3')
            .build()
    }
}
```

Use **`broadcastUpdateTo`**, **`broadcastAppendTo`**, and related methods from controllers/services to push updates to **`turbo:streamFrom`** subscribers (requires Action Cable enabled and clients subscribed).

## Request Detection

The plugin registers **`TurboInterceptor`** (order **100**) so attributes are available on every request. Interceptors with order **&lt; 100** run before it.

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

Configure the plugin under **`grails.plugin.turbo`** (not `grails.turbo`) in `application.yml`:

```yaml
grails:
  plugin:
    turbo:
      turboVersion: '8.0.4'
      useCdn: true
      cdnUrl: 'https://cdn.jsdelivr.net/npm/@hotwired/turbo'  # Turbo-only ESM; optional override
      enableDrive: true
      enableFrames: true
      enableStreams: true
      enableActionCable: true
      actionCablePath: '/cable'
      actionCableAllowedOrigins: '*'   # comma-separated, or * 
      actionCableUrl: null             # optional; full ws(s) URL for meta, else path + request host
      actionCablePingIntervalSeconds: 3
      streamSigningSecret: 'change-me-in-production'   # required for turbo:streamFrom signing
      globalIdApp: 'application'                        # gid:// segment for streamable domain objects
      metaOptions:                                       # -> <meta name="turbo-{key}" content="...">
        cache-control: 'no-cache'
```

With **`enableStreams: false`**, the plugin does not promote the `turbo_stream` format from `Accept` and `acceptsTurboStream()` is always false; subscription tags that depend on streams emit a skip comment instead of markup.

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

This repository is a **plugin and runnable demo**. See:

- [`grails-app/controllers/grails/turbo/example/ExampleController.groovy`](grails-app/controllers/grails/turbo/example/ExampleController.groovy) — frames, HTTP turbo streams, Action Cable demo (`streamJobDemo`), lazy-load action
- [`grails-app/views/example/`](grails-app/views/example/) — Turbo Frame message UI, stream job page, templates
- [`grails-app/views/layouts/turbo.gsp`](grails-app/views/layouts/turbo.gsp) — reference layout (`metasOnly` / `scriptsOnly`)

Geb smoke tests under [`src/integration-test/groovy/grails/turbo/`](src/integration-test/groovy/grails/turbo/) cover the messages demo, cable WebSocket broadcast, and **streamJobDemo** in a browser.

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
