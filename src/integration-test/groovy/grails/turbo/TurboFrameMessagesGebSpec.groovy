package grails.turbo

import grails.turbo.example.Message
import grails.turbo.pages.TurboFrameMessagesPage
import grails.testing.mixin.integration.Integration
import spock.lang.Timeout

import java.util.concurrent.TimeUnit

/**
 * Geb checks for Turbo Frame message CRUD and lazy-loaded frame on the demo page.
 */
@Timeout(value = 120, unit = TimeUnit.SECONDS)
@Integration
class TurboFrameMessagesGebSpec extends GebIntegrationSpec {

    void setup() {
        Message.withNewTransaction {
            Message.list()*.delete(flush: true)
        }
    }

    void 'add messages, delete one, refresh verifies lazy turbo frame loads'() {
        given:
        def suffix = UUID.randomUUID().toString().take(8)
        def firstTitle = "Geb first $suffix"
        def secondTitle = "Geb second $suffix"

        when: 'opening the turbo frame messages demo'
        to TurboFrameMessagesPage
        waitForTurboReady()

        then: 'list starts empty'
        waitForMessageCount(0)
        $('#empty-message').displayed

        when: 'creating the first message'
        addMessage(firstTitle, 'First body', 1)

        then: 'the message appears and the empty state is gone'
        waitFor { !$('#empty-message').displayed }

        when: 'creating a second message'
        addMessage(secondTitle, 'Second body', 2)

        then: 'both messages are visible'
        waitForMessageTitleVisible(secondTitle)
        messagesList.find('h5.card-title', text: firstTitle).displayed

        when: 'deleting the second message (by title)'
        deleteMessageWithTitle(secondTitle, 1)

        then: 'only the first message remains'
        messagesList.find('h5.card-title', text: firstTitle).displayed

        when: 'refreshing the index so the lazy frame src is bound to the surviving message'
        driver.navigate().refresh()
        waitFor { at TurboFrameMessagesPage }
        waitForTurboReady()
        def survivorId = Message.withNewTransaction { Message.findByTitle(firstTitle)?.id }

        then: 'turbo-frame points at lazyLoad with the surviving message id'
        survivorId != null
        waitForLazyFrameSrc(survivorId)

        when: 'the lazy frame finishes loading (eager in test env; server sleeps 1s)'
        scrollLazyFrameIntoView()

        then: 'lazy-loaded copy is visible in the frame'
        waitForLazyFrameLoaded(firstTitle)

        when: 'requesting the lazy-load URL directly (same response the frame receives)'
        go "/example/lazyLoad/${survivorId}"

        then: 'lazyLoad wraps content in a matching turbo-frame and renders the demo copy'
        waitFor(TurboFrameMessagesPage.DEFAULT_WAIT_SEC, TurboFrameMessagesPage.DEFAULT_RETRY_SEC) {
            $('turbo-frame#lazy-content').displayed
        }
        $('turbo-frame#lazy-content').text().contains('This content was lazy-loaded using Turbo Frames!')
        $('turbo-frame#lazy-content h5.card-title', text: firstTitle).displayed
    }
}
