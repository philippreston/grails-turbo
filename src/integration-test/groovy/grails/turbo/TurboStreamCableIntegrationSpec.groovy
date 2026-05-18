package grails.turbo

import grails.testing.mixin.integration.Integration
import grails.turbo.config.TurboConfig
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketHttpHeaders
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.handler.TextWebSocketHandler
import spock.lang.Specification

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Subscribes to Turbo::StreamsChannel over the Action Cable WebSocket and asserts a broadcast is delivered.
 * (Headless Geb cannot rely on the full browser Action Cable stack across environments.)
 */
@SuppressWarnings('SpringJavaInjectionPointsAutowiringInspection')
@Integration
class TurboStreamCableIntegrationSpec extends Specification {

    @Autowired
    Environment environment

    @Autowired
    TurboStreamService turboStreamService

    @Autowired
    TurboConfig turboConfig

    void 'Action Cable WebSocket subscriber receives a turbo-stream broadcast'() {
        given:
        int port = environment.getProperty('local.server.port', Integer)
        assert port > 0: 'local.server.port must be set for integration test server'

        String jobId = UUID.randomUUID().toString()
        String streamName = TurboStreamName.fromIterable(['streamDemo', jobId], turboConfig.globalIdApp ?: 'application')
        String signed = new TurboMessageVerifier(turboConfig.streamSigningSecret.trim()).generate(streamName)
        Map identifierMap = [channel: TurboConstants.DEFAULT_STREAMS_CHANNEL, signed_stream_name: signed]
        String identifierJson = JsonOutput.toJson(identifierMap)
        String subscribePayload = JsonOutput.toJson([command: 'subscribe', identifier: identifierJson])

        CountDownLatch welcomeLatch = new CountDownLatch(1)
        CountDownLatch confirmLatch = new CountDownLatch(1)
        CountDownLatch pushLatch = new CountDownLatch(1)

        TextWebSocketHandler handler = new TextWebSocketHandler() {
            @Override
            void handleTextMessage(WebSocketSession ws, TextMessage message) throws Exception {
                def p = new JsonSlurper().parseText(message.payload)
                if (p.type == 'welcome') {
                    welcomeLatch.countDown()
                }
                if (p.type == 'confirm_subscription') {
                    confirmLatch.countDown()
                }
                if (p.message && p.message.toString().contains('<turbo-stream')) {
                    pushLatch.countDown()
                }
            }
        }

        WebSocketHttpHeaders headers = new WebSocketHttpHeaders()
        headers.setSecWebSocketProtocol('actioncable-v1-json')

        StandardWebSocketClient client = new StandardWebSocketClient()
        URI uri = URI.create("ws://127.0.0.1:${port}/cable")
        WebSocketSession wsSession = client.doHandshake(handler, headers, uri).get(30, TimeUnit.SECONDS)

        expect:
        welcomeLatch.await(30, TimeUnit.SECONDS)

        when:
        wsSession.sendMessage(new TextMessage(subscribePayload))

        then:
        confirmLatch.await(30, TimeUnit.SECONDS)

        when:
        turboStreamService.broadcastUpdateTo(['streamDemo', jobId], 'job-status-panel',
            '<span id="job-status">Running</span>')

        then:
        pushLatch.await(30, TimeUnit.SECONDS)

        cleanup:
        wsSession?.close()
    }
}
