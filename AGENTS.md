# AI Agent Guide - Grails Turbo Plugin

This is a **Grails 6.x plugin** that integrates Hotwired Turbo for SPA-like behavior with server-rendered HTML. Inspired by turbo-rails.

## Architecture Overview

**Core Pattern**: HTTP header detection → trait-based controller support → templated responses
- `TurboInterceptor` (order=100) detects headers (`Turbo-Request`, `Turbo-Frame`) on ALL requests, adds attributes to request scope (frame headers ignored when `enableFrames: false`)
- `TurboController` trait provides methods to controllers (NOT a base class - use `implements TurboController`)
- `TurboStreamBuilder` creates fluent chainable responses (e.g., `.append().update().remove()`)
- Turbo JS loaded via `<turbo:includeTurbo>` tag from CDN (NOT asset pipeline)

**Key Files**:
- `src/main/groovy/grails/turbo/` - Core trait, builder, constants, request wrapper
- `grails-app/controllers/grails/turbo/TurboInterceptor.groovy` - Header detection
- `grails-app/taglib/grails/turbo/TurboTagLib.groovy` - GSP tags (`turbo:frame`, `turbo:stream`)
- `grails-app/services/grails/turbo/TurboStreamService.groovy` - Service-layer support
- `grails-app/views/layouts/turbo_frame.gsp` - Optional minimal layout for frame navigations (turbo-rails-style; opt in via controller `layout` closure)

### turbo:frame tag
- Provide **`id`**, or **`bean`** (Rails-style dom_id via `TurboTagLib.turboDomId`), or **`ids`** (list joined with `_`).
- **Arbitrary HTML attributes** (`class`, `style`, `data-*`, `aria-*`, …) pass through to `<turbo-frame>` (HTML-escaped).
- Optional layout **`turbo_frame`** when `request.getAttribute('isTurboFrameRequest')` is true; vary ETags if you cache responses.

## Critical Conventions

### Controllers MUST implement the trait:
```groovy
class MyController implements TurboController {
    // Now has: isTurboRequest(), renderTurboStream{}, respondWithTurbo{}
}
```

### Template rendering has a KNOWN LIMITATION (see TurboController.groovy:13):
Controllers using `renderTemplate()` MUST declare `def groovyPageRenderer` (injected by Grails). The trait declares it as abstract. Without this, template rendering fails silently.

### Response pattern uses closure delegation:
```groovy
renderTurboStream {
    append 'target', renderTemplate('template', [model: data])
    // 'this' still references controller due to OWNER_FIRST resolveStrategy
}
```
Note: Changed from `DELEGATE_FIRST` to allow controller method access.

### MIME type registration:
- Format: `turbo_stream` (underscore, not dash)
- MIME: `text/vnd.turbo-stream.html`
- Auto-registered in plugin's `doWithApplicationContext()`, manually in `application.yml` under `grails.mime.types.turbo_stream`

## Configuration (application.yml)

Located under `grails.plugin.turbo` (not `grails.turbo`):
```yaml
grails:
  plugin:
    turbo:
      turboVersion: '8.0.4'          # CDN version
      useCdn: true                   # false = don't load JS
      cdnUrl: 'https://...'          # Override CDN
      enableDrive: true              # false adds meta tag to disable
      enableFrames: true             # false: ignore Turbo-Frame + turbo:frame renders a div
      enableStreams: true            # false: acceptsTurboStream() false; no turbo_stream MIME promotion
      streamSigningSecret: '...'     # required for turbo:streamFrom (Rails MessageVerifier SHA256+JSON)
      globalIdApp: 'application'     # app segment in gid:// when streamables include domain objects
      metaOptions:                   # Converted to <meta name="turbo-{key}">
        cache-control: 'no-cache'
```
Accessed via injected `TurboConfig` bean. Configuration applied in plugin's `doWithApplicationContext()`.

## Build & Test Commands

**Run tests**: `./gradlew test` (unit tests in `src/test/groovy/grails/turbo/`)
**Integration tests**: `./gradlew integrationTest` (Geb-based tests in `src/integration-test/`)
**Build plugin**: `./gradlew build` (creates plugin JAR)
**Run as app**: `./gradlew bootRun` (plugin doubles as demo app)

**Gradle**: 6.1.2, **Grails**: 6.1.2, **Java**: 17 (see `build.gradle` line 66)

### Geb Integration Tests
- Base spec: `GebIntegrationSpec` (extend for new tests)
- Config: `src/integration-test/resources/GebConfig.groovy`
- Run with visible browser: `./gradlew integrationTest -Dgeb.env=chromeHeadful`
- Browsers: chrome (default headless), chromeHeadful, firefox, firefoxHeadful

## Common Patterns

### Multi-format responses (graceful degradation):
```groovy
respondWithTurbo {
    html { redirect action: 'list', status: 303 }
    turboStream { append 'items', renderTemplate('item', [item: item]) }
    json { render item as JSON }
}
```
Format selection: checks `acceptsTurboStream()` first, then `params.format`, defaults to `html`. For **`html`** branches that **redirect** after a Turbo-driven form mutation, prefer **`redirect(..., status: 303)`** (See Other) so the next navigation uses **GET**—same rationale as turbo-rails / Turbo Drive conventions.

### Declarative GORM broadcasts vs Rails `broadcasts`

Rails **`Turbo::Broadcastable`** exposes a **`broadcasts`** class DSL (insert target, lifecycle hooks, **`broadcasts_to`**, **`renders`/partial options**) that wires Active Record callbacks for you.

**This plugin intentionally keeps broadcasts explicit:** implement **`TurboBroadcastable`** and call **`turboBroadcast*`** methods from **`afterInsert`** / **`afterUpdate`** / **`beforeDelete`** (or Hibernate listeners) yourself. Reasons:

1. **GORM lifecycle** differs from Rails AR (transactions, flushing, cascades).
2. **No hidden global behavior** — you control ordering relative to validations and cascades.
3. **Closer to “plain Groovy”** without compile-time DSL magic.

If you want **Rails-like centralization**:

- **`static`** rules map + **`@PostConstruct` / BootStrap** registration calling a helper that attaches listeners (explicit scan of domain classes).
- **Grails application events**: publish domain events from GORM callbacks and subscribe with a **`@Listener`** bean that broadcasts.
- **`AbstractRoutingDataSource`/multi-tenancy**: keep streamables explicit via **`turboBroadcastStreamables()`** per record (already supported).

A future enhancement could introduce an **opt-in artifact** (e.g. annotated domains or **`static Closure turboBroadcasts`**) interpreted at startup—the above patterns are what we recommend documenting first.

### Frame-specific rendering:
```groovy
if (isTurboFrameRequest()) {
    render template: 'item', model: [item: item]
    return
}
```
Frame ID available via `getTurboFrameId()`.

### Naming convention for DOM IDs:
Use `item_${id}` pattern for entity elements (e.g., `task_123`). This appears throughout examples and enables easy remove/replace operations.

## GSP Tag Patterns

### Frame IDs MUST match between request and response:
```gsp
<turbo:frame id="messages">  <!-- This ID must match in target response -->
```

### Lazy loading requires src attribute:
```gsp
<turbo:frame id="stats" src="${createLink(action: 'loadStats')}" loading="lazy">
    Loading...  <!-- Shown until src loads -->
</turbo:frame>
```

### Stream tags typically in views, not layouts:
Used when rendering `.gsp` files as Turbo Stream responses (rare — usually built in controller).

### `turbo:streamFrom` (Action Cable / Turbo Streams subscriptions)
Rails-style **`turbo-cable-stream-source`**: signed stream names for `Turbo::StreamsChannel`. Use **`streamables`** (strings and/or domain instances); names are canonicalized (`TurboStreamName`) and signed with [`TurboRailsMessageVerifier`](src/main/groovy/grails/turbo/TurboRailsMessageVerifier.groovy) (compatible with Rails `ActiveSupport::MessageVerifier`, digest **SHA256**, **JSON** serializer, same format as `Turbo.signed_stream_verifier`). Configure a non-blank **`streamSigningSecret`**; the taglib errors if it is missing. Set **`globalIdApp`** to match your app segment in `gid://` when encoding entities. Prefer **`turbo:streamFrom`**; **`turbo:cableStreamSource`** is deprecated for hand-built sources.

## Plugin Development

**Plugin class**: `GrailsTurboGrailsPlugin.groovy` registers beans and config
**Reloading**: Controllers auto-reload via Grails. Trait methods available immediately.
**Constants**: All headers/MIME types in `TurboConstants.groovy` (use instead of string literals)
**Testing**: Spock specs test individual components (Request, Builder, TagLib, etc.)

## Known Issues & Gotchas

1. **Template rendering**: Controllers need `def groovyPageRenderer` declared (FIXME in code)
2. **MIME type**: Use `turbo_stream` format, not `turbo-stream` (Grails convention)
3. **Interceptor order**: TurboInterceptor runs at 100, custom interceptors <100 run before it
4. **Request attributes**: Set in before(), available in GSP via `${isTurboRequest}`, `${turboFrameId}`
5. **No asset pipeline**: Turbo JS loaded from CDN via tag, not bundled in application.js
6. **Streams gating**: With `enableStreams: false`, stream subscription tags are skipped and HTTP turbo-stream negotiation is suppressed (see `TurboConstants.TURBO_STREAMS_DISABLED_ATTR`)

## Testing Approach

Check `src/test/groovy/grails/turbo/` for examples:
- Mock `HttpServletRequest` for header tests
- Test builders independently (no HTTP context needed)
- Controller specs use Grails testing support with mocked headers

---

**Key insight for agents**: This is a Grails plugin that provides a trait-based API. Most functionality comes from implementing the trait, not extending classes. The interceptor makes Turbo transparent to GSPs, while controllers opt-in via the trait.

