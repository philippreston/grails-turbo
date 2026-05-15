package grails.turbo.example

import grails.gorm.transactions.Transactional
import grails.turbo.TurboController
import grails.turbo.TurboStreamService
import grails.util.Environment
import org.springframework.core.task.TaskExecutor

import java.text.SimpleDateFormat
import java.util.TimeZone
import java.util.concurrent.TimeUnit

/**
 * Example controller demonstrating Turbo integration.
 * This controller shows how to use Turbo Frames and Turbo Streams.
 */
class ExampleController implements TurboController {

    def groovyPageRenderer
    TurboStreamService turboStreamService
    TaskExecutor turboStreamTaskExecutor


    def index() {
        // Simple index action with safe handling
        def messages = []
        def messageCount = 0

        try {
            messages = Message.list() ?: []
            messageCount = Message.count() ?: 0
        } catch (Exception e) {
            log.warn("Could not load messages: ${e.message}")
            // Return empty list if Message domain is not available
        }

        [messages: messages, messageCount: messageCount, lazyFrameLoading: Environment.current == Environment.TEST ? 'eager' : 'lazy']
    }

    /**
     * List messages with support for both HTML and Turbo Stream responses.
     */
    def list() {
        redirect action: 'index'
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
    @Transactional
    def create() {
        def message = new Message(params)

        if (message.save(flush: true)) {
            // Respond with different formats
            respondWithTurbo {
                html {
                    flash.message = "Message created successfully"
                    redirect action: 'index'
                }
                turboStream {
                    // Remove the empty state message if this is the first message
                    if (Message.count() == 1) {
                        remove 'empty-message'
                    }
                    // Append the new message to the list
                    def messageHtml = renderTemplate('message', [message: message])
                    append 'messages-list', messageHtml
                    // Update the message count
                    update 'message-count', "${Message.count() ?: 0}"
                    // Clear the form
                    def formHtml = renderTemplate('form', [message: new Message()])
                    update 'message-form', formHtml
                }
            }
            return  // ← Added this to prevent default view rendering
        } else {
            respondWithTurbo {
                html {
                    render view: 'create', model: [message: message]
                }
                turboStream {
                    // Update the form with errors
                    def formHtml = renderTemplate('form', [message: message])
                    update 'message-form', formHtml
                }
            }
            return  // ← Added this to prevent default view rendering
        }
    }

    /**
     * Update a message and respond with a Turbo Stream to replace it.
     */
    @Transactional
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
                    def messageHtml = renderTemplate('message', [message: message])
                    replace "message_${message.id}", messageHtml
                }
            }
        } else {
            respondWithTurbo {
                html {
                    render view: 'edit', model: [message: message]
                }
                turboStream {
                    def formHtml = renderTemplate('form', [message: message])
                    update "message_${message.id}", formHtml
                }
            }
        }
    }

    /**
     * Delete a message and respond with a Turbo Stream to remove it.
     */
    @Transactional
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
                    redirect action: 'index'
                }
            turboStream {
                // Remove the message from the list
                remove "message_${message.id}"
                // Update the message count
                update 'message-count', "${Message.count() ?: 0}"
            }
        }
    }

    /**
     * Demo page for Turbo Streams over Action Cable: async status transitions for a dummy job.
     */
    def streamJobDemo() {
        String jobId = UUID.randomUUID().toString()
        scheduleJobStreamUpdates(jobId)
        [jobId: jobId]
    }

    /**
     * Example of lazy-loading content in a Turbo Frame.
     */
    def lazyLoad(Long id) {
        sleep(1000) // Simulate slow loading

        def message = id ? Message.get(id) : Message.first()

        // Response must be wrapped in a turbo-frame tag with matching ID
        if (message) {
            def content = renderTemplate('messageDetails', [message: message])
            render(text: """<turbo-frame id="lazy-content">${content}</turbo-frame>""", contentType: 'text/html')
        } else {
            render(text: """
                <turbo-frame id="lazy-content">
                    <div class="alert alert-info">
                        <h5>Lazy-Loading Example</h5>
                        <p>This frame would normally load detailed content for a message after a 1-second delay.</p>
                        <p>Create a message first to see this in action!</p>
                    </div>
                </turbo-frame>
            """, contentType: 'text/html')
        }
    }

    protected void notFound() {
        request.withFormat {
            html {
                flash.message = "Message not found"
                redirect action: 'index'
            }
            '*' { render status: 404 }
        }
    }

    private void scheduleJobStreamUpdates(String jobId) {
        TaskExecutor exec = turboStreamTaskExecutor
        TurboStreamService svc = turboStreamService
        if (!exec || !svc) {
            log.warn('turboStreamTaskExecutor or turboStreamService not available; stream job updates skipped')
            return
        }
        List<?> streamables = ['streamDemo', jobId]
        exec.execute {
            sleep(TimeUnit.SECONDS.toMillis(2))
            svc.broadcastUpdateTo(streamables, 'job-status-panel', jobStatusPanelInner('Running', utcNow()))
        }
        exec.execute {
            sleep(TimeUnit.SECONDS.toMillis(7))
            svc.broadcastUpdateTo(streamables, 'job-status-panel', jobStatusPanelInner('Complete', utcNow()))
        }
    }

    private static String jobStatusPanelInner(String status, String timeLabel) {
        """<span id="job-status">${status}</span>
<span id="job-time">${timeLabel}</span>"""
    }

    private static String utcNow() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        sdf.setTimeZone(TimeZone.getTimeZone('UTC'))
        sdf.format(new Date())
    }
}


