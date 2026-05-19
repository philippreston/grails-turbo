package grails.turbo

import grails.testing.web.taglib.TagLibUnitTest
import grails.turbo.config.TurboConfig
import spock.lang.Specification
import spock.lang.Unroll

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

    void "test streamFrom produces signed turbo-cable-stream-source"() {
        when:
        def output = applyTemplate(
            '''<turbo:streamFrom streamables="${parts}"/>''',
            [parts: ['myaccount', 'entries']]
        )
        String signedName = extractSignedStreamName(output)
        String decoded = new TurboMessageVerifier('secret').verified(signedName)

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

    @Unroll
    void "stream tag action #action with target wraps template=#wraps"() {
        when:
        String tpl = action == 'remove'
            ? """<turbo:stream action="${action}" target="message_1"></turbo:stream>"""
            : """<turbo:stream action="${action}" target="message_1">Content</turbo:stream>"""
        def output = applyTemplate(tpl)

        then:
        output.contains("action=\"${action}\"")
        output.contains('target="message_1"')
        wraps == output.contains('<template>')
        extraChecks.every { output.contains(it) }

        where:
        action   | wraps | extraChecks
        'append' | true  | ['Content']
        'prepend'| true  | []
        'replace'| true  | []
        'update' | true  | []
        'remove' | false | []
        'before' | true  | []
        'after'  | true  | []
    }

    @Unroll
    void "stream refresh attribute variants (#scenario)"() {
        when:
        def output = applyTemplate(template)

        then:
        output.contains('action="refresh"')
        !output.contains('<template>')
        expected.every { output.contains(it) }

        where:
        scenario           | template | expected
        'minimal'          | '<turbo:stream action="refresh"></turbo:stream>' | []
        'requestId'        | '<turbo:stream action="refresh" requestId="u1"></turbo:stream>' | ['request-id="u1"']
        'request-id attr'  | '<turbo:stream action="refresh" request-id="u2"></turbo:stream>' | ['request-id="u2"']
        'morph+scroll'     | '<turbo:stream action="refresh" morph="true" scroll="preserve"></turbo:stream>' | ['method="morph"', 'scroll="preserve"']
        'raw method'       | '<turbo:stream action="refresh" method="advance"></turbo:stream>' | ['method="advance"']
    }

    @Unroll
    void "stream tag morph on replace/update (#action) morph=#morph"() {
        when:
        def output = applyTemplate(
            """<turbo:stream action="${action}" target="t" morph="${morph}">X</turbo:stream>"""
        )

        then:
        output.contains("action=\"${action}\"")
        output.contains('method="morph"') == morph

        where:
        action    | morph
        'replace' | false
        'replace' | true
        'update'  | false
        'update'  | true
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

    @Unroll
    void "pageRefresh emits metas (#scenario)"() {
        when:
        def output = applyTemplate(template)

        then:
        expected.every { output.contains(it) }

        where:
        scenario   | template | expected
        'defaults' | '<turbo:pageRefresh/>' | ['<meta name="turbo-refresh-method" content="replace">']
        'morph'    | '<turbo:pageRefresh method="morph"/>' | ['content="morph"']
        'scroll'   | '<turbo:pageRefresh scroll="preserve"/>' | ['turbo-refresh-scroll" content="preserve']
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

    void "turboDomId with prefix matches Rails dom_id(record, segment)"() {
        expect:
        TurboTagLib.turboDomId(new FrameIdBean(id: 5L), 'edit') == 'edit_frameIdBean_5'
        TurboTagLib.turboDomId(new FrameIdBean(), 'edit') == 'edit_new_frameIdBean'
    }

    void "frame tag applies prefix to bean dom id"() {
        when:
        def output = applyTemplate(
            '<turbo:frame bean="${b}" prefix="modal">X</turbo:frame>',
            [b: new FrameIdBean(id: 3L)]
        )

        then:
        output.contains('id="modal_frameIdBean_3"')
    }

    void "visitControl tag with content"() {
        when:
        def output = applyTemplate('<turbo:visitControl content="advance"/>')

        then:
        output.contains('<meta name="turbo-visit-control" content="advance">')
    }

    void "visitControl defaults to reload"() {
        when:
        def output = applyTemplate('<turbo:visitControl/>')

        then:
        output.contains('<meta name="turbo-visit-control" content="reload">')
    }

    void "cacheControl tag"() {
        when:
        def output = applyTemplate('<turbo:cacheControl content="no-preview"/>')

        then:
        output.contains('<meta name="turbo-cache-control" content="no-preview">')
    }

    void "cacheControl requires content"() {
        when:
        applyTemplate('<turbo:cacheControl/>')

        then:
        thrown(Exception)
    }

    static class FrameIdBean {
        Long id
    }
}

