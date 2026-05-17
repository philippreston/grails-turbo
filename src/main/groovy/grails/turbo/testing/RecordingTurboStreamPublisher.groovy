package grails.turbo.testing

import grails.turbo.TurboStreamPublisher

/**
 * Captures {@link TurboStreamPublisher#publish} calls for assertions in tests (similar spirit to
 * turbo-rails broadcast test helpers).
 */
class RecordingTurboStreamPublisher implements TurboStreamPublisher {

    final List<Map> broadcasts = Collections.synchronizedList([])

    @Override
    void publish(String streamName, String turboStreamHtml) {
        broadcasts << [streamName: streamName, html: turboStreamHtml ?: '']
    }

    void clear() {
        broadcasts.clear()
    }

    List<Map> snapshot() {
        synchronized (broadcasts) {
            Collections.unmodifiableList(new ArrayList<>(broadcasts))
        }
    }
}
