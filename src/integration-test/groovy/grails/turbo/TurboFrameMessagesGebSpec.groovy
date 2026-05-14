package grails.turbo

import grails.turbo.example.Message
import grails.turbo.pages.TurboFrameMessagesPage
import grails.testing.mixin.integration.Integration

/**
 * Geb checks for Turbo Frame message CRUD and lazy-loaded frame on the demo page.
 */
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

        when: 'refreshing and scrolling the lazy frame into view'
        driver.navigate().refresh()
        waitFor { at TurboFrameMessagesPage }
        scrollLazyFrameIntoView()

        then: 'lazy-loaded frame content replaces the placeholder (controller uses ~1s delay)'
        waitFor(30, 0.25) {
            lazyContentFrame.text().contains('This content was lazy-loaded using Turbo Frames!')
        }
        lazyContentFrame.text().contains(firstTitle)
    }
}
