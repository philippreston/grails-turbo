package grails.turbo

/**
 * Broker-agnostic hook for pushing Turbo Stream HTML to subscribers (Cable, Redis, STOMP, etc.).
 * Default bean is {@link grails.turbo.cable.ActionCableTurboStreamPublisher} (in-memory Action Cable fan-out).
 * Replace the {@code turboStreamPublisher} bean to use Redis, etc., or use {@link NoOpTurboStreamPublisher} for a stub.
 *
 * @param streamName canonical stream name (same as turbo-rails after verifying {@code signed_stream_name})
 * @param turboStreamHtml one or more {@code turbo-stream} elements (same wire format as HTTP {@code text/vnd.turbo-stream.html})
 */
interface TurboStreamPublisher {

    void publish(String streamName, String turboStreamHtml)
}
