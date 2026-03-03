# Quick Start Guide - Grails Turbo Plugin

Get up and running with Turbo in your Grails application in minutes!

## Installation

### Step 1: Add the Plugin

Add to your `build.gradle`:

```gradle
dependencies {
    implementation 'grails.turbo:grails-turbo:0.1'
}
```

### Step 2: Configure MIME Types

The plugin automatically registers the Turbo Stream MIME type, but verify it's in your `application.yml`:

```yaml
grails:
  mime:
    types:
      turbo_stream:
        - text/vnd.turbo-stream.html
```

### Step 3: Include Turbo in Your Layout

Update your main layout (e.g., `grails-app/views/layouts/main.gsp`):

```gsp
<!DOCTYPE html>
<html>
<head>
    <title><g:layoutTitle/></title>
    <asset:javascript src="application.js"/>
    <turbo:includeTurbo/>
</head>
<body>
    <g:layoutBody/>
</body>
</html>
```

## Your First Turbo Frame

### Create a Controller

```groovy
package myapp

import grails.turbo.TurboController

class TaskController implements TurboController {
    
    def list() {
        [tasks: Task.list()]
    }
    
    def show(Long id) {
        def task = Task.get(id)
        
        // If requested from a Turbo Frame, render just the template
        if (isTurboFrameRequest()) {
            render template: 'task', model: [task: task]
            return
        }
        
        [task: task]
    }
}
```

### Create a View

`grails-app/views/task/list.gsp`:

```gsp
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Tasks</title>
</head>
<body>
    <h1>Tasks</h1>
    
    <turbo:frame id="tasks">
        <g:each in="${tasks}" var="task">
            <div id="task_${task.id}">
                <h3>${task.title}</h3>
                <g:link action="show" id="${task.id}">View Details</g:link>
            </div>
        </g:each>
    </turbo:frame>
</body>
</html>
```

### Create a Template

`grails-app/views/task/_task.gsp`:

```gsp
<turbo:frame id="task_${task.id}">
    <h3>${task.title}</h3>
    <p>${task.description}</p>
    <g:link action="list">Back to List</g:link>
</turbo:frame>
```

**That's it!** Click "View Details" and watch the content update without a full page reload.

## Your First Turbo Stream

### Update Your Controller

```groovy
def create() {
    def task = new Task(params)
    task.save()
    
    respondWithTurbo {
        html {
            redirect action: 'list'
        }
        turboStream {
            append 'tasks', render(template: 'task', model: [task: task])
        }
    }
}
```

### Create a Form

```gsp
<turbo:frame id="new-task">
    <g:form controller="task" action="create">
        <input type="text" name="title" placeholder="Task title"/>
        <textarea name="description" placeholder="Description"></textarea>
        <button type="submit">Create Task</button>
    </g:form>
</turbo:frame>

<div id="tasks">
    <g:each in="${tasks}" var="task">
        <g:render template="task" model="[task: task]"/>
    </g:each>
</div>
```

**Submit the form** and the new task appears instantly at the bottom of the list!

## Common Patterns

### Pattern 1: Delete with Stream

```groovy
def delete(Long id) {
    def task = Task.get(id)
    task.delete()
    
    renderTurboStream {
        remove "task_${id}"
    }
}
```

### Pattern 2: Update with Stream

```groovy
def update(Long id) {
    def task = Task.get(id)
    task.properties = params
    task.save()
    
    renderTurboStream {
        replace "task_${id}", render(template: 'task', model: [task: task])
    }
}
```

### Pattern 3: Lazy Loading

```gsp
<turbo:frame 
    id="expensive-content" 
    src="${createLink(action: 'loadExpensive')}"
    loading="lazy">
    <p>Loading...</p>
</turbo:frame>
```

### Pattern 4: Multiple Updates

```groovy
renderTurboStream {
    append 'tasks', render(template: 'task', model: [task: newTask])
    update 'task-count', "${Task.count()}"
    remove "task_${oldTask.id}"
}
```

## What's Next?

- Read the [full README](README.md) for detailed documentation
- Check out the [Developer Guide](DEVELOPER_GUIDE.md) for advanced patterns
- Explore the [Examples](EXAMPLES.md) for working code
- Visit [turbo.hotwired.dev](https://turbo.hotwired.dev/) for Turbo documentation

## Troubleshooting

### Links reload the full page
- Ensure Turbo JavaScript is loaded
- Check browser console for errors
- Verify the link doesn't have `data-turbo="false"`

### Frame not updating
- Ensure frame IDs match between source and target
- Check that the response includes a matching frame
- Verify no JavaScript errors in console

### Stream not working
- Ensure Accept header includes `text/vnd.turbo-stream.html`
- Check response Content-Type is correct
- Verify target element exists in DOM

## Need Help?

- [GitHub Issues](https://github.com/grails/grails-turbo/issues)
- [Grails Slack](https://slack.grails.org)
- [Turbo Documentation](https://turbo.hotwired.dev/)

Happy Turbo-charging your Grails app! 🚀

