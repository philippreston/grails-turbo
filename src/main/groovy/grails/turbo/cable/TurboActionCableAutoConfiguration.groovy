package grails.turbo.cable

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.web.socket.config.annotation.EnableWebSocket

/**
 * Enables Spring WebSocket for Turbo Action Cable when the plugin is on the classpath.
 */
@AutoConfiguration
@EnableWebSocket
class TurboActionCableAutoConfiguration {
}
