package grails.turbo

/**
 * Broker-agnostic hook for pushing Turbo Stream HTML to subscribers (Cable, Redis, STOMP, etc.).
 * Default bean is {@link NoOpTurboStreamPublisher}. Replace the {@code turboStreamPublisher} bean
 * in your application to integrate with your real-time backend.
 *
 * @param streamName signed or unsigned name matching {@code turbo:streamFrom} / Rails {@code Turbo::StreamsChannel}
 * @param turboStreamHtml one or more {@code turbo-stream} elements (same wire format as HTTP {@code text/vnd.turbo-stream.html})
 */
interface TurboStreamPublisher {

    void publish(String streamName, String turboStreamHtml)
}
