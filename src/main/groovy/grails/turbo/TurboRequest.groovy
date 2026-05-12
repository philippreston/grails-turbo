package grails.turbo

import groovy.transform.CompileStatic

import javax.servlet.http.HttpServletRequest

/**
 * Utility methods for detecting Turbo requests and extracting Turbo-specific information.
 */
@CompileStatic
class TurboRequest {

    private final HttpServletRequest request

    TurboRequest(HttpServletRequest request) {
        this.request = request
    }

    /**
     * Check if the request is a Turbo request.
     * @return true if the request has the Turbo-Request header
     */
    boolean isTurboRequest() {
        return request.getHeader(TurboConstants.TURBO_REQUEST_HEADER) != null
    }

    /**
     * Check if the request is a Turbo Frame request.
     * @return true if the request has the Turbo-Frame header and frames are enabled in configuration
     */
    boolean isTurboFrameRequest() {
        return getTurboFrameId() != null
    }

    /**
     * Get the Turbo Frame ID from the request header.
     * @return the frame ID, or null if not a frame request
     */
    String getTurboFrameId() {
        if (isTurboFramesDisabled()) {
            return null
        }
        return request.getHeader(TurboConstants.TURBO_FRAME_HEADER)
    }

    /**
     * True when {@link TurboConstants#TURBO_FRAMES_DISABLED_ATTR} is set (Turbo Frames disabled in config).
     */
    boolean isTurboFramesDisabled() {
        return Boolean.TRUE == request.getAttribute(TurboConstants.TURBO_FRAMES_DISABLED_ATTR)
    }

    /**
     * Check if the request accepts Turbo Stream responses.
     * @return true if the request accepts turbo-stream MIME type
     */
    boolean acceptsTurboStream() {
        String accept = request.getHeader("Accept")
        return accept != null && accept.contains(TurboConstants.TURBO_STREAM_MIME_TYPE)
    }

    /**
     * Get the request format, checking for Turbo Stream first.
     * @return the format string
     */
    String getFormat() {
        if (acceptsTurboStream()) {
            return TurboConstants.TURBO_STREAM_FORMAT
        }
        return null
    }
}

