package grails.turbo.example

import grails.turbo.TurboController

/**
 * Example controller demonstrating Turbo integration.
 * This controller shows how to use Turbo Frames and Turbo Streams.
 */
class ExampleController implements TurboController {

    def index() {
        // Simple index action
        [messages: Message.list()]
    }

    /**
     * List messages with support for both HTML and Turbo Stream responses.
     */
    def list() {
        def messages = Message.list(params)

        // Respond with different formats based on the request
        respondWithTurbo {
            html {
                render view: 'list', model: [messages: messages]
            }
            turboStream {
                update 'messages', render(template: 'messages', collection: messages, var: 'message')
            }
        }
    }

    /**
     * Show a single message. If requested from a Turbo Frame, only renders the frame content.
     */
    def show(Long id) {
        def message = Message.get(id)

        if (!message) {
            notFound()
            return
        }

        // If this is a Turbo Frame request, we can render just the frame
        if (isTurboFrameRequest()) {
            render template: 'message', model: [message: message]
            return
        }

        [message: message]
    }

    /**
     * Create a new message and respond with a Turbo Stream to append it to the list.
     */
    def create() {
        def message = new Message(params)

        if (message.save(flush: true)) {
            // Respond with different formats
            respondWithTurbo {
                html {
                    flash.message = "Message created successfully"
                    redirect action: 'list'
                }
                turboStream {
                    // Append the new message to the list
                    append 'messages', render(template: 'message', model: [message: message])
                    // Update the message count
                    update 'message-count', "${Message.count()}"
                    // Clear the form
                    update 'message-form', render(template: 'form', model: [message: new Message()])
                }
            }
        } else {
            respondWithTurbo {
                html {
                    render view: 'create', model: [message: message]
                }
                turboStream {
                    // Update the form with errors
                    update 'message-form', render(template: 'form', model: [message: message])
                }
            }
        }
    }

    /**
     * Update a message and respond with a Turbo Stream to replace it.
     */
    def update(Long id) {
        def message = Message.get(id)

        if (!message) {
            notFound()
            return
        }

        message.properties = params

        if (message.save(flush: true)) {
            respondWithTurbo {
                html {
                    flash.message = "Message updated successfully"
                    redirect action: 'show', id: message.id
                }
                turboStream {
                    // Replace the message in the list
                    replace "message_${message.id}", render(template: 'message', model: [message: message])
                }
            }
        } else {
            respondWithTurbo {
                html {
                    render view: 'edit', model: [message: message]
                }
                turboStream {
                    update "message_${message.id}", render(template: 'form', model: [message: message])
                }
            }
        }
    }

    /**
     * Delete a message and respond with a Turbo Stream to remove it.
     */
    def delete(Long id) {
        def message = Message.get(id)

        if (!message) {
            notFound()
            return
        }

        message.delete(flush: true)

        respondWithTurbo {
            html {
                flash.message = "Message deleted successfully"
                redirect action: 'list'
            }
            turboStream {
                // Remove the message from the list
                remove "message_${message.id}"
                // Update the message count
                update 'message-count', "${Message.count()}"
            }
        }
    }

    /**
     * Example of lazy-loading content in a Turbo Frame.
     */
    def lazyLoad(Long id) {
        sleep(1000) // Simulate slow loading

        def message = Message.get(id)
        render template: 'messageDetails', model: [message: message]
    }

    protected void notFound() {
        request.withFormat {
            html {
                flash.message = "Message not found"
                redirect action: 'list'
            }
            '*' { render status: 404 }
        }
    }
}


