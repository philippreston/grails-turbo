# Grails Turbo - Quick Reference Card

## Installation
```gradle
dependencies {
    implementation 'grails.turbo:grails-turbo:0.1'
}
```

## Controller Setup
```groovy
import grails.turbo.TurboController

class MyController implements TurboController {
    // Turbo methods now available
}
```

## Detection Methods
```groovy
isTurboRequest()         // true if request from Turbo
isTurboFrameRequest()    // true if from Turbo Frame
getTurboFrameId()        // Get requesting frame ID
acceptsTurboStream()     // true if accepts stream responses
```

## Rendering Turbo Streams
### Simple Stream
```groovy
renderTurboStream {
    append 'items', '<div>New item</div>'
}
```

### Multi-Format Response
```groovy
respondWithTurbo {
    html { redirect action: 'list' }
    turboStream {
        append 'items', render(template: 'item', model: [item: item])
        update 'count', "${Item.count()}"
    }
    json { render item as JSON }
}
```

## Stream Actions
```groovy
append 'target', content      // Add to end
prepend 'target', content     // Add to beginning
replace 'target', content     // Replace entire element
update 'target', content      // Replace inner HTML
remove 'target'               // Delete element
before 'target', content      // Insert before
after 'target', content       // Insert after
refresh()                     // Trigger page refresh
```

## GSP Tags

### Turbo Frame
```gsp
<turbo:frame id="unique-id">
    <!-- Content that can update independently -->
</turbo:frame>
```

### Lazy-Loading Frame
```gsp
<turbo:frame id="lazy-content" 
             src="${createLink(action: 'loadContent')}"
             loading="lazy">
    Loading...
</turbo:frame>
```

### Turbo Stream
```gsp
<turbo:stream action="append" target="items">
    <div class="item">New item</div>
</turbo:stream>
```

### Include Turbo JavaScript
```gsp
<turbo:includeTurbo version="8.0.4"/>
```

## Frame Attributes
| Attribute | Description | Values |
|-----------|-------------|--------|
| `id` | Unique identifier | Required |
| `src` | URL to load | Optional |
| `loading` | Load behavior | eager (default), lazy |
| `target` | Target frame | Frame ID or _top |
| `autoscroll` | Auto-scroll on update | Boolean |
| `disabled` | Disable navigation | Boolean |

## Link Attributes
```gsp
<!-- Navigate within frame -->
<g:link action="show" data-turbo-frame="frame-id">Link</g:link>

<!-- Break out of frame -->
<g:link action="index" data-turbo-frame="_top">Home</g:link>

<!-- Disable Turbo for link -->
<g:link action="download" data-turbo="false">Download</g:link>

<!-- Confirm before navigation -->
<g:link action="delete" data-turbo-confirm="Are you sure?">Delete</g:link>
```

## Form Attributes
```gsp
<!-- Form that triggers Turbo Stream -->
<g:form controller="message" action="create">
    <!-- Form fields -->
</g:form>

<!-- Disable Turbo for form -->
<g:form action="upload" data-turbo="false">
    <!-- File upload -->
</g:form>
```

## Request Detection in GSP
```gsp
<g:if test="${isTurboRequest}">
    <!-- Turbo-specific content -->
</g:if>

<g:if test="${isTurboFrameRequest}">
    <!-- Frame: ${turboFrameId} -->
</g:if>
```

## Service Usage
```groovy
class MyService {
    TurboStreamService turboStreamService
    
    String buildUpdate(Item item) {
        return turboStreamService.builder()
            .append('items', renderItem(item))
            .update('count', item.count.toString())
            .build()
    }
}
```

## HTTP Headers
### Request Headers
- `Turbo-Request: 1` - Indicates Turbo request
- `Turbo-Frame: frame-id` - Frame making request
- `Accept: text/vnd.turbo-stream.html` - Accepts streams

### Response Headers
- `Content-Type: text/vnd.turbo-stream.html` - Stream response

## MIME Types
```yaml
grails:
  mime:
    types:
      turbo_stream:
        - text/vnd.turbo-stream.html
```

## Common Patterns

### CRUD with Streams
```groovy
// Create
respondWithTurbo {
    html { redirect action: 'list' }
    turboStream {
        append 'items', render(template: 'item', model: [item: item])
    }
}

// Update
renderTurboStream {
    replace "item_${id}", render(template: 'item', model: [item: item])
}

// Delete
renderTurboStream {
    remove "item_${id}"
}
```

### Conditional Rendering
```groovy
def show(Long id) {
    def item = Item.get(id)
    
    if (isTurboFrameRequest()) {
        render template: 'item', model: [item: item]
        return
    }
    
    [item: item]
}
```

### Multiple Updates
```groovy
renderTurboStream {
    append 'notifications', render(template: 'notification', model: [message: msg])
    update 'unread-count', "${unreadCount}"
    remove 'old-notification'
}
```

## Troubleshooting

### Frame not updating
✓ Check frame IDs match  
✓ Verify response includes frame with same ID  
✓ Ensure no JavaScript errors

### Full page reload
✓ Verify Turbo JavaScript is loaded  
✓ Check link doesn't have `data-turbo="false"`  
✓ Ensure not using `target="_blank"`

### Stream not working
✓ Check Accept header includes stream MIME type  
✓ Verify response Content-Type is correct  
✓ Ensure target element exists in DOM

## Resources
- [Turbo Handbook](https://turbo.hotwired.dev/handbook/introduction)
- [Plugin README](README.md)
- [Developer Guide](DEVELOPER_GUIDE.md)
- [Examples](EXAMPLES.md)

---
**Version**: 0.1 | **License**: Apache 2.0

