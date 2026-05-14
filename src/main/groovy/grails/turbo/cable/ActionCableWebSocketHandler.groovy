package grails.turbo.cable

import grails.turbo.TurboConstants
import grails.turbo.TurboRailsMessageVerifier
import grails.turbo.config.TurboConfig
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Minimal Action Cable server for {@link TurboConstants#DEFAULT_STREAMS_CHANNEL} subscriptions.
 */
class ActionCableWebSocketHandler extends TextWebSocketHandler {

    private static final String TYPE_WELCOME = 'welcome'
    private static final String TYPE_CONFIRM = 'confirm_subscription'
    private static final String TYPE_REJECT = 'reject_subscription'
    private static final String TYPE_PING = 'ping'

    private static final ScheduledExecutorService PING = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
        @Override
        Thread newThread(Runnable r) {
            Thread t = new Thread(r, 'turbo-action-cable-ping')
            t.setDaemon(true)
            return t
        }
    })
    private static final AtomicBoolean PING_SCHEDULED = new AtomicBoolean(false)

    private final Set<WebSocketSession> allSessions = ConcurrentHashMap.newKeySet()

    @Autowired
    TurboConfig turboConfig

    @Autowired
    TurboStreamSubscriptionRegistry turboStreamSubscriptionRegistry

    @Override
    void afterConnectionEstablished(WebSocketSession session) throws Exception {
        allSessions.add(session)
        schedulePingIfNeeded()
        String welcome = JsonOutput.toJson(type: TYPE_WELCOME)
        session.sendMessage(new TextMessage(welcome))
    }

    @Override
    void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        allSessions.remove(session)
        turboStreamSubscriptionRegistry.removeSession(session)
    }

    @Override
    void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        if (!turboConfig.enableActionCable) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason('Action Cable disabled'))
            return
        }
        Object parsed = new JsonSlurper().parseText(message.payload)
        if (!(parsed instanceof Map)) {
            return
        }
        Map map = (Map) parsed
        String command = map.command as String
        if (command == 'subscribe') {
            handleSubscribe(session, map.identifier as String)
        } else if (command == 'unsubscribe') {
            turboStreamSubscriptionRegistry.unsubscribe(session, map.identifier as String)
        }
    }

    private void handleSubscribe(WebSocketSession session, String identifier) {
        if (!identifier) {
            return
        }
        Object idObj = new JsonSlurper().parseText(identifier)
        if (!(idObj instanceof Map)) {
            sendReject(session, identifier)
            return
        }
        Map idMap = (Map) idObj
        String channel = idMap.channel as String
        String signed = idMap.signed_stream_name as String
        if (channel != TurboConstants.DEFAULT_STREAMS_CHANNEL || !signed) {
            sendReject(session, identifier)
            return
        }
        String secret = turboConfig.streamSigningSecret?.trim()
        if (!secret) {
            sendReject(session, identifier)
            return
        }
        String canonical = new TurboRailsMessageVerifier(secret).verified(signed)
        if (!canonical) {
            sendReject(session, identifier)
            return
        }
        turboStreamSubscriptionRegistry.subscribe(canonical, session, identifier)
        session.sendMessage(new TextMessage(JsonOutput.toJson(type: TYPE_CONFIRM, identifier: identifier)))
    }

    private static void sendReject(WebSocketSession session, String identifier) {
        if (identifier && session.isOpen()) {
            session.sendMessage(new TextMessage(JsonOutput.toJson(type: TYPE_REJECT, identifier: identifier)))
        }
    }

    private void schedulePingIfNeeded() {
        if (!PING_SCHEDULED.compareAndSet(false, true)) {
            return
        }
        int secs = turboConfig.actionCablePingIntervalSeconds > 0 ? turboConfig.actionCablePingIntervalSeconds : 3
        PING.scheduleAtFixedRate(this::broadcastPing, secs, secs, TimeUnit.SECONDS)
    }

    private void broadcastPing() {
        long epoch = System.currentTimeMillis() / 1000L
        String msg = JsonOutput.toJson(type: TYPE_PING, message: epoch)
        for (WebSocketSession s : new ArrayList<>(allSessions)) {
            if (s.isOpen()) {
                try {
                    s.sendMessage(new TextMessage(msg))
                } catch (ignored) {
                    // session may be closing
                }
            }
        }
    }
}
