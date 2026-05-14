package grails.turbo

/**
 * Default {@link TurboStreamPublisher} that performs no I/O (suitable for dev or HTTP-only apps).
 */
class NoOpTurboStreamPublisher implements TurboStreamPublisher {

    @Override
    void publish(String streamName, String turboStreamHtml) {
        // intentionally empty
    }
}
