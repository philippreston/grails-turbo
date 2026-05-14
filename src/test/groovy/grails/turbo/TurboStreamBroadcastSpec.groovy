package grails.turbo

import grails.testing.services.ServiceUnitTest
import grails.turbo.config.TurboConfig
import org.springframework.core.task.SyncTaskExecutor
import spock.lang.Specification

class TurboStreamBroadcastSpec extends Specification implements ServiceUnitTest<TurboStreamService> {

    CapturingPublisher publisher

    void setup() {
        publisher = new CapturingPublisher()
        service.turboStreamPublisher = publisher
        service.turboStreamTaskExecutor = new SyncTaskExecutor()
    }

    void 'broadcastAppendTo publishes unsigned stream name when no signing secret'() {
        given:
        service.turboConfig = new TurboConfig(globalIdApp: 'myapp')

        when:
        service.broadcastAppendTo(['room', '7'], 'messages', '<div>x</div>')

        then:
        publisher.calls.size() == 1
        publisher.calls[0].streamName == 'room:7'
        publisher.calls[0].html.contains('action="append"')
        publisher.calls[0].html.contains('target="messages"')
    }

    void 'broadcastAppendTo publishes canonical stream name when streamSigningSecret is set (Rails parity)'() {
        given:
        service.turboConfig = new TurboConfig(globalIdApp: 'myapp', streamSigningSecret: 's3cr3t')

        when:
        service.broadcastAppendTo('solo', 't', '<p/>')

        then:
        publisher.calls.size() == 1
        publisher.calls[0].streamName == 'solo'
    }

    void 'broadcastAppendLater invokes publisher asynchronously via executor'() {
        given:
        service.turboConfig = new TurboConfig()

        when:
        service.broadcastAppendLater([1, 'two'], 'x', 'y')

        then:
        publisher.calls.size() == 1
    }

    void 'broadcastRenderTo sends raw turbo stream html'() {
        given:
        service.turboConfig = new TurboConfig()

        when:
        service.broadcastRenderTo(['a'], '<turbo-stream action="refresh"></turbo-stream>')

        then:
        publisher.calls[0].html == '<turbo-stream action="refresh"></turbo-stream>'
    }

    void 'broadcastRemoveAllTo uses targets attribute'() {
        given:
        service.turboConfig = new TurboConfig()

        when:
        service.broadcastRemoveAllTo('x', '.item')

        then:
        publisher.calls[0].html.contains('targets=".item"')
        !publisher.calls[0].html.contains('target=')
    }

    void 'blank streamables throw'() {
        given:
        service.turboConfig = new TurboConfig()

        when:
        service.broadcastRefreshTo([])

        then:
        thrown(IllegalArgumentException)
    }

    static class CapturingPublisher implements TurboStreamPublisher {
        List<Map> calls = []

        @Override
        void publish(String streamName, String turboStreamHtml) {
            calls << [streamName: streamName, html: turboStreamHtml]
        }
    }
}
