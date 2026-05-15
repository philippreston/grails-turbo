package grails.turbo

import grails.turbo.example.Message
import grails.turbo.pages.TurboFrameMessagesPage
import grails.testing.mixin.integration.Integration
import spock.lang.Timeout

/**
 * Geb checks for Turbo Frame message CRUD and lazy-loaded frame on the demo page.
 */
@Timeout(120)
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

        then: 'list starts empty'
        waitFor { messageCount.text() == '0' }
        $('#empty-message').displayed

        when: 'creating the first message'
        addMessage(firstTitle, 'First body')

        then: 'the message appears and the empty state is gone'
        waitFor { $('h5.card-title', text: firstTitle).displayed }
        waitFor { !$('#empty-message').displayed }
        messageCount.text() == '1'

        when: 'creating a second message'
        addMessage(secondTitle, 'Second body')

        then: 'both messages are visible'
        waitFor { $('h5.card-title', text: secondTitle).displayed }
        $('h5.card-title', text: firstTitle).displayed
        messageCount.text() == '2'

        when: 'deleting the second message (by title)'
        deleteMessageWithTitle(secondTitle)

        then: 'only the first message remains'
        waitFor { $('h5.card-title', text: secondTitle).size() == 0 }
        $('h5.card-title', text: firstTitle).displayed
        messageCount.text() == '1'

        when: 'refreshing the index so the lazy frame src is bound to the surviving message'
        driver.navigate().refresh()
        waitFor { at TurboFrameMessagesPage }

        then: 'turbo-frame points at lazyLoad with the surviving message id (Turbo will fetch this in the browser)'
        def survivorId = Message.withNewTransaction { Message.findByTitle(firstTitle)?.id }
        survivorId != null
        waitFor(15, 0.25) {
            def src = lazyContentFrame.getAttribute('src')
            src && src.contains('lazyLoad') &&
                (src.contains("id=${survivorId}") || src.contains("/${survivorId}"))
        }

        when: 'requesting the lazy-load URL directly (same response the frame receives)'
        go "/example/lazyLoad/${survivorId}"

        then: 'lazyLoad wraps content in a matching turbo-frame and renders the demo copy'
        waitFor(15, 0.25) { $('turbo-frame#lazy-content').displayed }
        $('turbo-frame#lazy-content').text().contains('This content was lazy-loaded using Turbo Frames!')
        $('h5.card-title', text: firstTitle).displayed
    }
}
