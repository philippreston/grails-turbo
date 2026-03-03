package grails.turbo

/**
 * Interceptor that adds Turbo-related attributes to the request.
 * This makes Turbo request information available in GSP views and controllers.
 */
class TurboInterceptor {

    int order = 100

    TurboInterceptor() {
        matchAll()
    }

    boolean before() {
        // Create TurboRequest wrapper and add to request attributes
        TurboRequest turboRequest = new TurboRequest(request)

        request.setAttribute('turboRequest', turboRequest)
        request.setAttribute('isTurboRequest', turboRequest.isTurboRequest())
        request.setAttribute('isTurboFrameRequest', turboRequest.isTurboFrameRequest())
        request.setAttribute('turboFrameId', turboRequest.getTurboFrameId())

        // Add Turbo Stream MIME type if not already registered
        if (!response.format) {
            if (turboRequest.acceptsTurboStream()) {
                response.format = TurboConstants.TURBO_STREAM_FORMAT
                response.contentType = TurboConstants.TURBO_STREAM_MIME_TYPE
            }
        }

        true
    }

    boolean after() { true }

    void afterView() {
        // Clean up
    }
}

