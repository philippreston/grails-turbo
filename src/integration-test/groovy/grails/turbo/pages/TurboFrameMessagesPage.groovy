package grails.turbo.pages

import geb.Page
import org.openqa.selenium.JavascriptExecutor

/**
 * Page object for the Turbo Frame messages demo ({@code /example/index}).
 */
class TurboFrameMessagesPage extends Page {

    static final int DEFAULT_WAIT_SEC = 45
    static final double DEFAULT_RETRY_SEC = 0.25

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

    boolean waitForTurboReady(int timeoutSec = DEFAULT_WAIT_SEC) {
        waitFor(timeoutSec, DEFAULT_RETRY_SEC) {
            browser.js.exec('return typeof Turbo !== "undefined"')
        }
    }

    boolean waitForMessageCount(int expected) {
        waitFor(DEFAULT_WAIT_SEC, DEFAULT_RETRY_SEC) {
            messageCount.text()?.trim() == expected.toString()
        }
    }

    boolean waitForMessageTitleVisible(String title) {
        waitFor(DEFAULT_WAIT_SEC, DEFAULT_RETRY_SEC) {
            messagesList.find('h5.card-title', text: title).displayed
        }
    }

    boolean waitForMessageTitleAbsent(String title) {
        waitFor(DEFAULT_WAIT_SEC, DEFAULT_RETRY_SEC) {
            messagesList.find('h5.card-title', text: title).size() == 0
        }
    }

    void addMessage(String title, String body, int expectedCount) {
        waitForTurboReady()
        waitFor { titleField.displayed && submitButton.displayed }
        titleField.value(title)
        bodyField.value(body)
        submitButton.click()
        waitForMessageCount(expectedCount)
        waitForMessageTitleVisible(title)
    }

    /**
     * Deletes the message card whose heading matches {@code title}.
     * Turbo {@code data-turbo-confirm} uses {@code window.confirm}; headless Chrome often does not
     * expose that as a WebDriver {@code alert}, so we stub {@code confirm} to accept.
     */
    void deleteMessageWithTitle(String title, int expectedCountAfter) {
        def card = messagesList.find('[id^=message_]').has('h5.card-title', text: title)
        waitFor { card.displayed }
        browser.js.exec('window.confirm = function() { return true; }')
        card.find('button.btn-danger').click()
        waitForMessageCount(expectedCountAfter)
        waitForMessageTitleAbsent(title)
    }

    boolean waitForLazyFrameSrc(Long messageId) {
        waitFor(DEFAULT_WAIT_SEC, DEFAULT_RETRY_SEC) {
            def src = lazyContentFrame.getAttribute('src')
            src && src.contains('lazyLoad') &&
                (src.contains("id=${messageId}") || src.contains("/${messageId}"))
        }
    }

    boolean waitForLazyFrameLoaded(String expectedTitle = null) {
        waitFor(DEFAULT_WAIT_SEC, DEFAULT_RETRY_SEC) {
            def frameText = lazyContentFrame.text()
            frameText?.contains('This content was lazy-loaded using Turbo Frames!') &&
                (!expectedTitle || lazyContentFrame.find('h5.card-title', text: expectedTitle).displayed)
        }
    }

    boolean scrollLazyFrameIntoView() {
        waitFor { lazyContentFrame.displayed }
        def element = lazyContentFrame.firstElement()
        ((JavascriptExecutor) browser.driver).executeScript(
                'arguments[0].scrollIntoView({block: "center"})', element)
        true
    }
}
