package grails.turbo.cable

import groovy.json.JsonOutput
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession

import java.util.Objects
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet

/**
 * In-memory subscription store: canonical stream name -> WebSocket sessions with Action Cable identifiers.
 */
class TurboStreamSubscriptionRegistry {

    private final ConcurrentHashMap<String, Set<Subscription>> streamToSubs = new ConcurrentHashMap<>()

    void subscribe(String canonicalStream, WebSocketSession session, String identifier) {
        Objects.requireNonNull(canonicalStream, 'canonicalStream')
        Objects.requireNonNull(session, 'session')
        Objects.requireNonNull(identifier, 'identifier')
        Subscription sub = new Subscription(session: session, identifier: identifier)
        streamToSubs.computeIfAbsent(canonicalStream, { new CopyOnWriteArraySet<>() }).add(sub)
    }

    void unsubscribe(WebSocketSession session, String identifier) {
        if (session == null || identifier == null) {
            return
        }
        for (Set<Subscription> set : streamToSubs.values()) {
            set.removeIf { Subscription s ->
                s.session == session && s.identifier == identifier
            }
        }
        streamToSubs.entrySet().removeIf { it.getValue().isEmpty() }
    }

    void removeSession(WebSocketSession session) {
        if (session == null) {
            return
        }
        for (Set<Subscription> set : streamToSubs.values()) {
            set.removeIf { it.session == session }
        }
        streamToSubs.entrySet().removeIf { it.getValue().isEmpty() }
    }

    void broadcast(String canonicalStream, String turboStreamHtml) {
        if (!canonicalStream) {
            return
        }
        Set<Subscription> subs = streamToSubs.get(canonicalStream)
        if (subs == null || subs.isEmpty()) {
            return
        }
        String html = turboStreamHtml ?: ''
        for (Subscription sub : new ArrayList<>(subs)) {
            if (!sub.session.isOpen()) {
                subs.remove(sub)
                continue
            }
            String payload = JsonOutput.toJson(identifier: sub.identifier, message: html)
            sub.session.sendMessage(new TextMessage(payload))
        }
        streamToSubs.entrySet().removeIf { it.getValue().isEmpty() }
    }

    static class Subscription {
        WebSocketSession session
        String identifier
    }
}
