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
        tagLib.turboConfig = new TurboConfig(streamSigningSecret: 'secret', enableActionCable: true, actionCablePath: '/cable')
    }

    void "test frame tag with required id"() {
        when:
        def output = applyTemplate('<turbo:frame id="test-frame">Content</turbo:frame>')

        then:
        output.contains('<turbo-frame id="test-frame"')
        output.contains('Content')
        output.contains('</turbo-frame>')
    }

    void "test frame tag passes through extra html attributes"() {
        when:
        def output = applyTemplate('<turbo:frame id="x" class="my-card" data-test="1">Body</turbo:frame>')

        then:
        output.contains('class="my-card"')
        output.contains('data-test="1"')
        output.contains('<turbo-frame id="x"')
    }

    void "test frame tag with bean derives dom id"() {
        when:
        def output = applyTemplate('<turbo:frame bean="${b}">X</turbo:frame>', [b: new FrameIdBean(id: 42L)])

        then:
        output.contains('id="frameIdBean_42"')
    }

    void "test frame tag with ids builds composite id"() {
        when:
        def output = applyTemplate('<turbo:frame ids="${parts}">Z</turbo:frame>', [parts: [7, 'tray']])

        then:
        output.contains('id="7_tray"')
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

    void "test frame tag renders div when enableFrames is false"() {
        given:
        tagLib.turboConfig.enableFrames = false

        when:
        def output = applyTemplate('<turbo:frame id="no-frames">Content</turbo:frame>')

        then:
        output.contains('<div id="no-frames">')
        output.contains('Content')
        output.contains('</div>')
        !output.contains('<turbo-frame')
    }

    void "test frame tag throws error without id"() {
        when:
        applyTemplate('<turbo:frame>Content</turbo:frame>')

        then:
        thrown(Exception)
    }

    void "test stream tag with morph on replace"() {
        when:
        def output = applyTemplate('<turbo:stream action="replace" target="a" morph="true">X</turbo:stream>')

        then:
        output.contains('method="morph"')
        output.contains('action="replace"')
    }

    void "test streamFrom produces signed turbo-cable-stream-source"() {
        when:
        def output = applyTemplate(
            '''<turbo:streamFrom streamables="${parts}"/>''',
            [parts: ['myaccount', 'entries']]
        )
        String signedName = extractSignedStreamName(output)
        String decoded = new TurboRailsMessageVerifier('secret').verified(signedName)

        then:
        output.contains('turbo-cable-stream-source channel="Turbo::StreamsChannel"')
        output.contains('signed-stream-name=')
        decoded.startsWith('myaccount')
    }

    void "test streamFrom skipped when enableStreams false"() {
        given:
        tagLib.turboConfig.enableStreams = false

        when:
        def output = applyTemplate('<turbo:streamFrom streamables="${[\'a\']}"/>')

        then:
        output.contains('skipped')
        !output.contains('signed-stream-name')
    }

    private static String extractSignedStreamName(String html) {
        def m = (html =~ /signed-stream-name="([^"]+)"/)
        return m.find() ? m.group(1) : ''
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
        output.contains('<script type="module" src="https://cdn.jsdelivr.net/npm/@hotwired/turbo-rails@8.0.4/app/assets/javascripts/turbo.min.js"></script>')
        output.contains('action-cable-url')
        output.contains('/cable')
        !output.contains('turbo.es2017-esm.js')
    }

    void "test includeTurbo tag with custom version"() {
        when:
        def output = applyTemplate('<turbo:includeTurbo turboRailsVersion="7.3.0" version="7.3.0"/>')

        then:
        output.contains('@hotwired/turbo-rails@7.3.0/')
        output.contains('turbo.min.js')
    }

    void "includeTurbo omits action-cable-url when enableActionCable false"() {
        given:
        tagLib.turboConfig = new TurboConfig(streamSigningSecret: 'secret', enableActionCable: false)

        when:
        def output = applyTemplate('<turbo:includeTurbo/>')

        then:
        !output.contains('action-cable-url')
        output.contains('turbo.es2017-esm.js')
    }

    void "includeTurbo metasOnly emits cable meta without script"() {
        when:
        def output = applyTemplate('<turbo:includeTurbo metasOnly="true"/>')

        then:
        output.contains('action-cable-url')
        !output.contains('turbo.min.js')
        !output.contains('turbo.es2017-esm.js')
    }

    void "includeTurbo metasOnly builds ws cable URL from request for relative path"() {
        given:
        request.serverName = 'localhost'
        request.serverPort = 5555
        request.scheme = 'http'

        when:
        def output = applyTemplate('<turbo:includeTurbo metasOnly="true"/>')

        then:
        output.contains('content="ws://localhost:5555/cable"')
    }

    void "includeTurbo scriptsOnly emits turbo-rails script without cable meta"() {
        when:
        def output = applyTemplate('<turbo:includeTurbo scriptsOnly="true"/>')

        then:
        output.contains('<script type="module"')
        output.contains('@hotwired/turbo-rails@8.0.4/')
        output.contains('turbo.min.js')
        !output.contains('action-cable-url')
    }

    void "includeTurbo rejects metasOnly and scriptsOnly together"() {
        when:
        applyTemplate('<turbo:includeTurbo metasOnly="true" scriptsOnly="true"/>')

        then:
        thrown(Exception)
    }

    void "turboDomId persisted id"() {
        expect:
        TurboTagLib.turboDomId(new FrameIdBean(id: 1L)) == 'frameIdBean_1'
    }

    void "turboDomId new record"() {
        expect:
        TurboTagLib.turboDomId(new FrameIdBean()) == 'new_frameIdBean'
    }

    static class FrameIdBean {
        Long id
    }
}

