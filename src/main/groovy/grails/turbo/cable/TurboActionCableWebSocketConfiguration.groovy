package grails.turbo.cable

import grails.turbo.config.TurboConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

/**
 * Registers the Action Cable compatible endpoint (Spring WebSocket stack, same foundation as
 * {@code grails-spring-websocket}). WebSocket support is enabled via {@link TurboActionCableAutoConfiguration}.
 */
class TurboActionCableWebSocketConfiguration implements WebSocketConfigurer {

    @Autowired
    Environment environment

    @Autowired
    TurboConfig turboConfig

    @Autowired
    ActionCableWebSocketHandler actionCableWebSocketHandler

    @Autowired
    ActionCableHandshakeHandler actionCableHandshakeHandler

    @Override
    void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        boolean enabled = environment.getProperty('grails.plugin.turbo.enableActionCable', Boolean, turboConfig.enableActionCable)
        if (!enabled) {
            return
        }
        String path = environment.getProperty('grails.plugin.turbo.actionCablePath', turboConfig.actionCablePath ?: '/cable')
        String origins = environment.getProperty('grails.plugin.turbo.actionCableAllowedOrigins', turboConfig.actionCableAllowedOrigins ?: '*')
        String[] allowed = parseOrigins(origins)

        registry.addHandler(actionCableWebSocketHandler, path)
            .setHandshakeHandler(actionCableHandshakeHandler)
            .setAllowedOrigins(allowed)
    }

    private static String[] parseOrigins(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return ['*'] as String[]
        }
        if (raw.trim() == '*') {
            return ['*'] as String[]
        }
        List<String> parts = raw.split(',').collect { it.trim() }.findAll { it }
        parts ? parts as String[] : ['*'] as String[]
    }
}
