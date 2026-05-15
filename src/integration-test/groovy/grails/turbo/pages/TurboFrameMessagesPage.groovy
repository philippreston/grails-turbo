package grails.turbo.pages

import geb.Page
import org.openqa.selenium.JavascriptExecutor

/**
 * Page object for the Turbo Frame messages demo ({@code /example/index}).
 */
class TurboFrameMessagesPage extends Page {

    static url = '/example/index'

    static at = { $('h1').text()?.contains('Turbo Examples') }

    static content = {
        messageCount { $('#message-count') }
        messagesList { $('#messages-list') }
        lazyContentFrame { $('turbo-frame#lazy-content') }
        titleField { $('turbo-frame#message-form input[name=title]') }
        bodyField { $('turbo-frame#message-form textarea[name=body]') }
        submitButton { $('turbo-frame#message-form button[type=submit]') }
    }

    void addMessage(String title, String body) {
        waitFor { titleField.displayed && submitButton.displayed }
        titleField.value(title)
        bodyField.value(body)
        submitButton.click()
    }

    /**
     * Deletes the message card whose heading matches {@code title}.
     * Turbo {@code data-turbo-confirm} uses {@code window.confirm}; headless Chrome often does not
     * expose that as a WebDriver {@code alert}, so we stub {@code confirm} to accept.
     */
    void deleteMessageWithTitle(String title) {
        def card = $('[id^=message_]').has('h5.card-title', text: title)
        waitFor { card.displayed }
        browser.js.exec('window.confirm = function() { return true; }')
        card.find('button.btn-danger').click()
    }

    void scrollLazyFrameIntoView() {
        waitFor { lazyContentFrame.displayed }
        def element = lazyContentFrame.firstElement()
        ((JavascriptExecutor) browser.driver).executeScript(
                'arguments[0].scrollIntoView({block: "center"})', element)
    }
}
