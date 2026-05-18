package grails.turbo.cable

import grails.turbo.TurboConstants
import grails.turbo.TurboMessageVerifier
import grails.turbo.config.TurboConfig
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import spock.lang.Specification

class ActionCableWebSocketHandlerSpec extends Specification {

    void 'subscribe confirms when stream signature valid'() {
        given:
        def reg = new TurboStreamSubscriptionRegistry()
        def cfg = new TurboConfig(streamSigningSecret: 'secret', enableActionCable: true, actionCablePingIntervalSeconds: 3600)
        def v = new TurboMessageVerifier('secret')
        String signed = v.generate('chat:5')
        String identifier = JsonOutput.toJson(
            channel: TurboConstants.DEFAULT_STREAMS_CHANNEL,
            signed_stream_name: signed)

        def handler = new ActionCableWebSocketHandler()
        handler.turboConfig = cfg
        handler.turboStreamSubscriptionRegistry = reg

        List<TextMessage> out = []
        WebSocketSession session = Mock(WebSocketSession)
        session.isOpen() >> true
        session.sendMessage(_) >> { TextMessage m -> out << m }

        when:
        handler.handleTextMessage(session, new TextMessage(JsonOutput.toJson(command: 'subscribe', identifier: identifier)))

        then:
        out.size() == 1
        new JsonSlurper().parseText(out[0].payload as String).type == 'confirm_subscription'
    }

    void 'subscribe rejects bad signature'() {
        given:
        def reg = new TurboStreamSubscriptionRegistry()
        def cfg = new TurboConfig(streamSigningSecret: 'secret', enableActionCable: true, actionCablePingIntervalSeconds: 3600)
        String identifier = JsonOutput.toJson(
            channel: TurboConstants.DEFAULT_STREAMS_CHANNEL,
            signed_stream_name: 'nope--deadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeefdeadbeef')

        def handler = new ActionCableWebSocketHandler()
        handler.turboConfig = cfg
        handler.turboStreamSubscriptionRegistry = reg

        List<TextMessage> out = []
        WebSocketSession session = Mock(WebSocketSession)
        session.isOpen() >> true
        session.sendMessage(_) >> { TextMessage m -> out << m }

        when:
        handler.handleTextMessage(session, new TextMessage(JsonOutput.toJson(command: 'subscribe', identifier: identifier)))

        then:
        out.any { new JsonSlurper().parseText(it.payload as String).type == 'reject_subscription' }
    }
}
