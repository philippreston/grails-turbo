package grails.turbo

import grails.testing.web.taglib.TagLibUnitTest
import grails.turbo.config.TurboConfig
import spock.lang.Specification

/**
 * Test specification for TurboTagLib.
 */
class TurboTagLibSpec extends Specification implements TagLibUnitTest<TurboTagLib> {

    def setup() {
        // Initialize turboConfig for the taglib
        tagLib.turboConfig = new TurboConfig()
    }

    void "test frame tag with required id"() {
        when:
        def output = applyTemplate('<turbo:frame id="test-frame">Content</turbo:frame>')

        then:
        output.contains('<turbo-frame id="test-frame">')
        output.contains('Content')
        output.contains('</turbo-frame>')
    }

    void "test frame tag with src attribute"() {
        when:
        def output = applyTemplate('<turbo:frame id="test-frame" src="/path/to/content">Loading...</turbo:frame>')

        then:
        output.contains('id="test-frame"')
        output.contains('src="/path/to/content"')
        output.contains('Loading...')
    }

    void "test frame tag with loading attribute"() {
        when:
        def output = applyTemplate('<turbo:frame id="test-frame" loading="lazy">Content</turbo:frame>')

        then:
        output.contains('loading="lazy"')
    }

    void "test frame tag with target attribute"() {
        when:
        def output = applyTemplate('<turbo:frame id="test-frame" target="_top">Content</turbo:frame>')

        then:
        output.contains('target="_top"')
    }

    void "test frame tag with disabled attribute"() {
        when:
        def output = applyTemplate('<turbo:frame id="test-frame" disabled="true">Content</turbo:frame>')

        then:
        output.contains('disabled')
    }

    void "test frame tag with autoscroll attribute"() {
        when:
        def output = applyTemplate('<turbo:frame id="test-frame" autoscroll="true">Content</turbo:frame>')

        then:
        output.contains('autoscroll')
    }

    void "test frame tag throws error without id"() {
        when:
        applyTemplate('<turbo:frame>Content</turbo:frame>')

        then:
        thrown(Exception)
    }

    void "test stream tag with append action"() {
        when:
        def output = applyTemplate('<turbo:stream action="append" target="messages">Content</turbo:stream>')

        then:
        output.contains('<turbo-stream action="append" target="messages">')
        output.contains('<template>')
        output.contains('Content')
        output.contains('</template>')
        output.contains('</turbo-stream>')
    }

    void "test stream tag with prepend action"() {
        when:
        def output = applyTemplate('<turbo:stream action="prepend" target="messages">Content</turbo:stream>')

        then:
        output.contains('action="prepend"')
        output.contains('target="messages"')
    }

    void "test stream tag with replace action"() {
        when:
        def output = applyTemplate('<turbo:stream action="replace" target="message_1">Content</turbo:stream>')

        then:
        output.contains('action="replace"')
        output.contains('target="message_1"')
    }

    void "test stream tag with update action"() {
        when:
        def output = applyTemplate('<turbo:stream action="update" target="message_1">Content</turbo:stream>')

        then:
        output.contains('action="update"')
        output.contains('target="message_1"')
    }

    void "test stream tag with remove action"() {
        when:
        def output = applyTemplate('<turbo:stream action="remove" target="message_1"></turbo:stream>')

        then:
        output.contains('action="remove"')
        output.contains('target="message_1"')
        !output.contains('<template>')
    }

    void "test stream tag with before action"() {
        when:
        def output = applyTemplate('<turbo:stream action="before" target="message_1">Content</turbo:stream>')

        then:
        output.contains('action="before"')
    }

    void "test stream tag with after action"() {
        when:
        def output = applyTemplate('<turbo:stream action="after" target="message_1">Content</turbo:stream>')

        then:
        output.contains('action="after"')
    }

    void "test stream tag with refresh action"() {
        when:
        def output = applyTemplate('<turbo:stream action="refresh" target="page"></turbo:stream>')

        then:
        output.contains('action="refresh"')
    }

    void "test stream tag with targets attribute"() {
        when:
        def output = applyTemplate('<turbo:stream action="update" targets=".message">Content</turbo:stream>')

        then:
        output.contains('targets=".message"')
    }

    void "test stream tag throws error without action"() {
        when:
        applyTemplate('<turbo:stream target="messages">Content</turbo:stream>')

        then:
        thrown(Exception)
    }

    void "test cableStreamSource tag"() {
        when:
        def output = applyTemplate('<turbo:cableStreamSource channel="MessagesChannel"/>')

        then:
        output.contains('<turbo-cable-stream-source channel="MessagesChannel"')
        output.contains('></turbo-cable-stream-source>')
    }

    void "test cableStreamSource tag with signedStreamName"() {
        when:
        def output = applyTemplate('<turbo:cableStreamSource channel="MessagesChannel" signedStreamName="abc123"/>')

        then:
        output.contains('channel="MessagesChannel"')
        output.contains('signed-stream-name="abc123"')
    }

    void "test cableStreamSource tag throws error without channel"() {
        when:
        applyTemplate('<turbo:cableStreamSource/>')

        then:
        thrown(Exception)
    }

    void "test pageRefresh tag with default method"() {
        when:
        def output = applyTemplate('<turbo:pageRefresh/>')

        then:
        output.contains('<meta name="turbo-refresh-method" content="replace">')
    }

    void "test pageRefresh tag with custom method"() {
        when:
        def output = applyTemplate('<turbo:pageRefresh method="morph"/>')

        then:
        output.contains('content="morph"')
    }

    void "test pageRefresh tag with scroll attribute"() {
        when:
        def output = applyTemplate('<turbo:pageRefresh scroll="preserve"/>')

        then:
        output.contains('<meta name="turbo-refresh-scroll" content="preserve">')
    }

    void "test includeTurbo tag with default version"() {
        when:
        def output = applyTemplate('<turbo:includeTurbo/>')

        then:
        output.contains('<script type="module"')
        output.contains('turbo@8.0.4')
        output.contains('turbo.es2017-esm.js')
    }

    void "test includeTurbo tag with custom version"() {
        when:
        def output = applyTemplate('<turbo:includeTurbo version="7.3.0"/>')

        then:
        output.contains('turbo@7.3.0')
    }
}

