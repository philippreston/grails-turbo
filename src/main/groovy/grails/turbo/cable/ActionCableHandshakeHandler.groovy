package grails.turbo.cable

import org.springframework.web.socket.server.support.DefaultHandshakeHandler

/**
 * Negotiates Action Cable WebSocket subprotocols expected by {@code @rails/actioncable}.
 */
class ActionCableHandshakeHandler extends DefaultHandshakeHandler {

    ActionCableHandshakeHandler() {
        setSupportedProtocols('actioncable-v1-json', 'actioncable-unsupported')
    }
}
