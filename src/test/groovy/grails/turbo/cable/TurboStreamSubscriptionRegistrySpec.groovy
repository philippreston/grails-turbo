package grails.turbo.cable

import groovy.json.JsonSlurper
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import spock.lang.Specification

class TurboStreamSubscriptionRegistrySpec extends Specification {

    void 'broadcast sends transmit envelope to subscribers'() {
        given:
        TurboStreamSubscriptionRegistry reg = new TurboStreamSubscriptionRegistry()
        List<TextMessage> sent = []
        WebSocketSession s = Mock(WebSocketSession)
        s.isOpen() >> true
        s.sendMessage(_) >> { TextMessage m -> sent << m }

        String identifier = '{"channel":"Turbo::StreamsChannel","signed_stream_name":"x"}'
        reg.subscribe('room:1', s, identifier)

        when:
        reg.broadcast('room:1', '<turbo-stream></turbo-stream>')

        then:
        sent.size() == 1
        def body = new JsonSlurper().parseText(sent[0].payload as String)
        body.identifier == identifier
        body.message == '<turbo-stream></turbo-stream>'
    }

    void 'removeSession drops all subs for that socket'() {
        given:
        TurboStreamSubscriptionRegistry reg = new TurboStreamSubscriptionRegistry()
        WebSocketSession s = Mock(WebSocketSession) { isOpen() >> true }
        reg.subscribe('a', s, '{"channel":"Turbo::StreamsChannel","signed_stream_name":"x"}')

        when:
        reg.removeSession(s)

        then:
        reg.broadcast('a', 'x')
        0 * s.sendMessage(_)
    }
}
