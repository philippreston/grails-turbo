package grails.turbo

import grails.testing.services.ServiceUnitTest
import grails.turbo.config.TurboConfig
import grails.turbo.testing.RecordingTurboStreamPublisher
import org.springframework.core.task.SyncTaskExecutor
import spock.lang.Specification

class TurboStreamBroadcastSpec extends Specification implements ServiceUnitTest<TurboStreamService> {

    RecordingTurboStreamPublisher publisher

    void setup() {
        publisher = new RecordingTurboStreamPublisher()
        service.turboStreamPublisher = publisher
        service.turboStreamTaskExecutor = new SyncTaskExecutor()
    }

    void 'broadcastAppendTo publishes unsigned stream name when no signing secret'() {
        given:
        service.turboConfig = new TurboConfig(globalIdApp: 'myapp')

        when:
        service.broadcastAppendTo(['room', '7'], 'messages', '<div>x</div>')

        then:
        publisher.broadcasts.size() == 1
        publisher.broadcasts[0].streamName == 'room:7'
        publisher.broadcasts[0].html.contains('action="append"')
        publisher.broadcasts[0].html.contains('target="messages"')
    }

    void 'broadcastAppendTo publishes canonical stream name when streamSigningSecret is set (Rails parity)'() {
        given:
        service.turboConfig = new TurboConfig(globalIdApp: 'myapp', streamSigningSecret: 's3cr3t')

        when:
        service.broadcastAppendTo('solo', 't', '<p/>')

        then:
        publisher.broadcasts.size() == 1
        publisher.broadcasts[0].streamName == 'solo'
    }

    void 'broadcastAppendLater invokes publisher asynchronously via executor'() {
        given:
        service.turboConfig = new TurboConfig()

        when:
        service.broadcastAppendLater([1, 'two'], 'x', 'y')

        then:
        publisher.broadcasts.size() == 1
    }

    void 'broadcastRenderTo sends raw turbo stream html'() {
        given:
        service.turboConfig = new TurboConfig()

        when:
        service.broadcastRenderTo(['a'], '<turbo-stream action="refresh"></turbo-stream>')

        then:
        publisher.broadcasts[0].html == '<turbo-stream action="refresh"></turbo-stream>'
    }

    void 'broadcastRefreshTo passes refresh options'() {
        given:
        service.turboConfig = new TurboConfig()

        when:
        service.broadcastRefreshTo('room', [requestId: 'x', morph: true, scroll: 'preserve'])

        then:
        publisher.broadcasts[0].html.contains('request-id="x"')
        publisher.broadcasts[0].html.contains('method="morph"')
        publisher.broadcasts[0].html.contains('scroll="preserve"')
    }

    void 'broadcastRemoveAllTo uses targets attribute'() {
        given:
        service.turboConfig = new TurboConfig()

        when:
        service.broadcastRemoveAllTo('x', '.item')

        then:
        publisher.broadcasts[0].html.contains('targets=".item"')
        !publisher.broadcasts[0].html.contains('target=')
    }

    void 'blank streamables throw'() {
        given:
        service.turboConfig = new TurboConfig()

        when:
        service.broadcastRefreshTo([])

        then:
        thrown(IllegalArgumentException)
    }

    void 'RecordingTurboStreamPublisher snapshot survives clear'() {
        given:
        RecordingTurboStreamPublisher rec = new RecordingTurboStreamPublisher()

        when:
        rec.publish('stream-a', '<turbo-stream/>')
        List<Map> snap = rec.snapshot()
        rec.clear()

        then:
        snap.size() == 1
        snap[0].streamName == 'stream-a'
        rec.broadcasts.empty
    }

}

