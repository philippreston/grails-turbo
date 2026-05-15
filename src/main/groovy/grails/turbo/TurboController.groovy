package grails.turbo

import org.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.web.context.request.RequestContextHolder

/**
 * Trait to add Turbo support methods to Grails controllers.
 *
 * Usage: Add 'implements TurboController' to your controller class.
 */
trait TurboController {

    // FIXME - make this work without requiring the controller to define a groovyPageRenderer property
    /**
     * Injected groovyPageRenderer service for rendering templates to strings.
     * This is automatically available to all controllers implementing this trait.
     */
    abstract def getGroovyPageRenderer()

    /**
     * Render a template to a string for use in Turbo Streams.
     * This method uses groovyPageRenderer which works reliably in all contexts.
     *
     * @param templatePath The template path (e.g., 'message' for _message.gsp)
     * @param model The model map to pass to the template
     * @return String containing the rendered HTML
     */
    String renderTemplate(String templatePath, Map model) {
        try {
            // Determine the controller path from the controller name
            def controllerPath = this.class.simpleName.replaceAll('Controller$', '').toLowerCase()

            // Use groovyPageRenderer to render template to string
            def result = groovyPageRenderer.render(
                template: "/${controllerPath}/${templatePath}",
                model: model
            )
            return result ?: ""
        } catch (Exception e) {
            // Log error but don't fail the request
            println "Error rendering template ${templatePath}: ${e.message}"
            return ""
        }
    }

    /**
     * Get the current Turbo request wrapper.
     */
    TurboRequest getTurboRequest() {
        GrailsWebRequest webRequest = (GrailsWebRequest) RequestContextHolder.currentRequestAttributes()
        return new TurboRequest(webRequest.getCurrentRequest())
    }

    /**
     * Check if the current request is a Turbo request.
     */
    boolean isTurboRequest() {
        return getTurboRequest().isTurboRequest()
    }

    /**
     * Check if the current request is a Turbo Frame request.
     */
    boolean isTurboFrameRequest() {
        return getTurboRequest().isTurboFrameRequest()
    }

    /**
     * Get the Turbo Frame ID from the current request.
     */
    String getTurboFrameId() {
        return getTurboRequest().getTurboFrameId()
    }

    /**
     * Check if the request accepts Turbo Stream responses.
     */
    boolean acceptsTurboStream() {
        return getTurboRequest().acceptsTurboStream()
    }

    /**
     * Render a Turbo Stream response.
     *
     * @param closure A closure that receives a TurboStreamBuilder
     */
    void renderTurboStream(@DelegatesTo(TurboStreamBuilder) Closure closure) {
        TurboStreamBuilder builder = new TurboStreamBuilder()
        closure.delegate = builder
        closure.resolveStrategy = Closure.OWNER_FIRST  // Changed from DELEGATE_FIRST to allow controller method access
        closure.call(builder)

        GrailsWebRequest webRequest = (GrailsWebRequest) RequestContextHolder.currentRequestAttributes()
        def response = webRequest.getCurrentResponse()
        response.setContentType(TurboConstants.TURBO_STREAM_MIME_TYPE)
        response.setCharacterEncoding("UTF-8")

        // Write the Turbo Stream response
        def writer = response.getWriter()
        writer.write(builder.build())
        writer.flush()

        // Mark the response as committed so Grails doesn't try to render a view
        response.flushBuffer()
        webRequest.renderView = false
    }

    /**
     * Respond with different formats, including Turbo Stream.
     *
     * Usage:
     * <pre>
     * respondWithTurbo {
     *     html { render view: 'index' }
     *     turboStream {
     *         update 'messages', render(template: 'message', model: [message: message])
     *     }
     * }
     * </pre>
     */
    void respondWithTurbo(Closure closure) {
        def formats = [:] as Map<String, Closure>

        // Create an Expando to handle format methods dynamically
        def formatBuilder = new Expando()
        formatBuilder.html = { Closure handler -> formats['html'] = handler }
        formatBuilder.turboStream = { Closure handler -> formats['turbo_stream'] = handler }
        formatBuilder.json = { Closure handler -> formats['json'] = handler }
        formatBuilder.xml = { Closure handler -> formats['xml'] = handler }

        closure.delegate = formatBuilder
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.call()

        // Determine which format to use
        String format = 'html'
        if (acceptsTurboStream()) {
            format = 'turbo_stream'
        } else {
            try {
                GrailsWebRequest webRequest = (GrailsWebRequest) RequestContextHolder.currentRequestAttributes()
                // Try to get format from request
                String requestFormat = webRequest.params.format ?: webRequest.request.getHeader('Accept')
                if (requestFormat && formats.containsKey(requestFormat)) {
                    format = requestFormat
                }
            } catch (Exception e) {
                // Default to html if we can't determine format
            }
        }

        // Execute the appropriate handler
        if (formats.containsKey(format)) {
            if (format == 'turbo_stream') {
                renderTurboStream(formats[format] as Closure)
            } else {
                def handler = formats[format] as Closure
                // Nested format closures were built while the outer closure's delegate was the
                // format Expando; re-bind so controller scopes (e.g. flash) and actions resolve.
                handler.delegate = this
                handler.resolveStrategy = Closure.DELEGATE_FIRST
                handler.call()
            }
        } else if (formats.containsKey('html')) {
            def handler = formats['html'] as Closure
            handler.delegate = this
            handler.resolveStrategy = Closure.DELEGATE_FIRST
            handler.call()
        }
    }
}



