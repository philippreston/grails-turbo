package grails.turbo

import org.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.web.context.request.RequestContextHolder

/**
 * Trait to add Turbo support methods to Grails controllers.
 *
 * Usage: Add 'implements TurboController' to your controller class.
 */
trait TurboController {

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
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.call(builder)

        GrailsWebRequest webRequest = (GrailsWebRequest) RequestContextHolder.currentRequestAttributes()
        def response = webRequest.getCurrentResponse()
        response.setContentType(TurboConstants.TURBO_STREAM_MIME_TYPE)
        response.setCharacterEncoding("UTF-8")
        response.getWriter().write(builder.build())
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

        // Create a DSL for defining format handlers using a map
        def formatBuilder = [
            html: { Closure handler -> formats['html'] = handler },
            turboStream: { Closure handler -> formats['turbo_stream'] = handler },
            json: { Closure handler -> formats['json'] = handler },
            xml: { Closure handler -> formats['xml'] = handler }
        ]

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
                handler.call()
            }
        } else if (formats.containsKey('html')) {
            def handler = formats['html'] as Closure
            handler.call()
        }
    }
}



