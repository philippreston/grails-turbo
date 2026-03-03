# Grails Turbo Plugin Examples

This directory contains example code demonstrating how to use the Grails Turbo plugin.

## Running the Examples

1. Start the application:
   ```bash
   ./gradlew bootRun
   ```

2. Navigate to the examples:
   ```
   http://localhost:8080/example/index
   ```

## What's Included

### ExampleController
Demonstrates:
- Using the `TurboController` trait
- Handling Turbo Frame requests
- Responding with Turbo Streams
- CRUD operations with Turbo
- Lazy-loading with Turbo Frames

### Example Views
- `index.gsp` - Main example page with Turbo Frames
- `_message.gsp` - Message card template
- `_form.gsp` - Form for creating/editing messages
- `_messageDetails.gsp` - Lazy-loaded message details

### Features Demonstrated

1. **Turbo Frames**: 
   - Scoped updates without full page reload
   - Lazy-loading content
   - Nested frames

2. **Turbo Streams**:
   - Append new items to lists
   - Update existing items
   - Remove items
   - Update multiple elements in one response

3. **Form Handling**:
   - Submit forms with Turbo
   - Handle validation errors
   - Update form after submission

4. **Request Detection**:
   - Check if request is from Turbo
   - Get Turbo Frame ID
   - Conditional rendering based on request type

## Try These Features

### Creating a Message
1. Fill out the form on the right side
2. Submit - the new message appears instantly without page reload
3. The form is cleared automatically

### Editing a Message
1. Click "Edit" on any message
2. The message card is replaced with an edit form
3. Submit to update - the card is replaced with updated content

### Deleting a Message
1. Click "Delete" on any message
2. Confirm the deletion
3. The message is removed from the list instantly

### Lazy Loading
1. Scroll down to see the lazy-loaded frame
2. Content loads automatically when it enters the viewport

## Code Structure

```
grails-turbo/
├── src/main/groovy/grails/turbo/
│   ├── TurboController.groovy          # Trait for controllers
│   ├── TurboRequest.groovy             # Request detection
│   ├── TurboStreamBuilder.groovy       # Build stream responses
│   ├── TurboTagLib.groovy              # GSP tags
│   └── TurboConstants.groovy           # Constants
├── grails-app/
│   ├── controllers/grails/turbo/
│   │   ├── example/ExampleController.groovy
│   │   └── TurboInterceptor.groovy
│   ├── services/grails/turbo/
│   │   └── TurboStreamService.groovy
│   └── views/example/
│       ├── index.gsp
│       ├── _message.gsp
│       ├── _form.gsp
│       └── _messageDetails.gsp
└── src/test/groovy/grails/turbo/
    ├── TurboRequestSpec.groovy
    └── TurboStreamBuilderSpec.groovy
```

