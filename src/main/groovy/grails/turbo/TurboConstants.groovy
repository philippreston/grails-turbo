package grails.turbo

/**
 * Constants for Turbo HTTP headers and MIME types.
 */
class TurboConstants {

    /**
     * Request attribute set by {@code TurboInterceptor} when
     * {@link grails.turbo.config.TurboConfig#enableFrames} is false, so
     * {@link TurboRequest} and GSPs treat the request as a non-frame request.
     */
    static final String TURBO_FRAMES_DISABLED_ATTR = "turboFramesDisabled"

    /**
     * Request attribute set when {@link grails.turbo.config.TurboConfig#enableStreams} is false.
     */
    static final String TURBO_STREAMS_DISABLED_ATTR = "turboStreamsDisabled"

    /**
     * Default Action Cable channel id for Hotwired Turbo Streams (matches turbo-rails).
     */
    static final String DEFAULT_STREAMS_CHANNEL = "Turbo::StreamsChannel"

    /**
     * Request header that indicates the request is from a Turbo Frame
     */
    static final String TURBO_FRAME_HEADER = "Turbo-Frame"

    /**
     * Request header that indicates the request is from Turbo
     */
    static final String TURBO_REQUEST_HEADER = "Turbo-Request"

    /**
     * MIME type for Turbo Stream responses
     */
    static final String TURBO_STREAM_MIME_TYPE = "text/vnd.turbo-stream.html"

    /**
     * MIME type for Turbo Stream format
     */
    static final String TURBO_STREAM_FORMAT = "turbo_stream"

    /**
     * Stream actions
     */
    static final String ACTION_APPEND = "append"
    static final String ACTION_PREPEND = "prepend"
    static final String ACTION_REPLACE = "replace"
    static final String ACTION_UPDATE = "update"
    static final String ACTION_REMOVE = "remove"
    static final String ACTION_BEFORE = "before"
    static final String ACTION_AFTER = "after"
    static final String ACTION_REFRESH = "refresh"
}

