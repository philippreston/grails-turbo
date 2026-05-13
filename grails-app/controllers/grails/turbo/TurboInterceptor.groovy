package grails.turbo

import grails.turbo.config.TurboConfig

/**
 * Interceptor that adds Turbo-related attributes to the request.
 * This makes Turbo request information available in GSP views and controllers.
 */
class TurboInterceptor {

    int order = 100

    /**
     * Injected by Spring; may be null in some unit-test setups (frames treated as enabled).
     */
    TurboConfig turboConfig

    TurboInterceptor() {
        matchAll()
    }

    boolean before() {
        if (turboConfig != null && !turboConfig.enableFrames) {
            request.setAttribute(TurboConstants.TURBO_FRAMES_DISABLED_ATTR, true)
        }
        if (turboConfig != null && !turboConfig.enableStreams) {
            request.setAttribute(TurboConstants.TURBO_STREAMS_DISABLED_ATTR, true)
        }
        // Create TurboRequest wrapper and add to request attributes
        TurboRequest turboRequest = new TurboRequest(request)

        request.setAttribute('turboRequest', turboRequest)
        request.setAttribute('isTurboRequest', turboRequest.isTurboRequest())
        request.setAttribute('isTurboFrameRequest', turboRequest.isTurboFrameRequest())
        request.setAttribute('turboFrameId', turboRequest.getTurboFrameId())

        // Add Turbo Stream MIME type if not already registered
        if (!response.format && (turboConfig == null || turboConfig.enableStreams)) {
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

