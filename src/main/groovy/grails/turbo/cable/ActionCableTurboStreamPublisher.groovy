package grails.turbo.cable

import grails.turbo.TurboStreamPublisher
import grails.turbo.config.TurboConfig
import org.springframework.beans.factory.annotation.Autowired

/**
 * Fans out turbo-stream HTML to Action Cable subscribers (in-memory, single-node).
 */
class ActionCableTurboStreamPublisher implements TurboStreamPublisher {

    @Autowired
    TurboConfig turboConfig

    @Autowired
    TurboStreamSubscriptionRegistry turboStreamSubscriptionRegistry

    @Override
    void publish(String streamName, String turboStreamHtml) {
        if (!turboConfig.enableActionCable || !streamName) {
            return
        }
        turboStreamSubscriptionRegistry.broadcast(streamName, turboStreamHtml)
    }
}
