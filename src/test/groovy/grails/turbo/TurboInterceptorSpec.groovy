package grails.turbo

import grails.testing.web.interceptor.InterceptorUnitTest
import grails.turbo.config.TurboConfig
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Test specification for TurboInterceptor.
 */
class TurboInterceptorSpec extends Specification implements InterceptorUnitTest<TurboInterceptor> {

    def setup() {
        interceptor.turboConfig = new TurboConfig()
    }

    void "test interceptor matches all controllers"() {
        when:
        withRequest(controller: "example")

        then:
        interceptor.doesMatch()
    }

    void "test interceptor adds isTurboRequest attribute when header present"() {
        given:
        request.addHeader(TurboConstants.TURBO_REQUEST_HEADER, "1")

        when:
        withRequest(controller: "test")
        interceptor.before()

        then:
        request.getAttribute('isTurboRequest') == true
    }

    void "test interceptor sets isTurboRequest to false when header absent"() {
        when:
        withRequest(controller: "test")
        interceptor.before()

        then:
        request.getAttribute('isTurboRequest') == false
    }

    void "test interceptor adds turboFrameId attribute when frame header present"() {
        given:
        request.addHeader(TurboConstants.TURBO_FRAME_HEADER, "my-frame")

        when:
        withRequest(controller: "test")
        interceptor.before()

        then:
        request.getAttribute('turboFrameId') == "my-frame"
    }

    void "test interceptor sets turboFrameId to null when header absent"() {
        when:
        withRequest(controller: "test")
        interceptor.before()

        then:
        request.getAttribute('turboFrameId') == null
    }

    void "test interceptor adds isTurboFrameRequest attribute when frame header present"() {
        given:
        request.addHeader(TurboConstants.TURBO_FRAME_HEADER, "my-frame")

        when:
        withRequest(controller: "test")
        interceptor.before()

        then:
        request.getAttribute('isTurboFrameRequest') == true
    }

    void "test interceptor sets isTurboFrameRequest to false when header absent"() {
        when:
        withRequest(controller: "test")
        interceptor.before()

        then:
        request.getAttribute('isTurboFrameRequest') == false
    }

    void "test interceptor adds turboRequest attribute"() {
        given:
        request.addHeader(TurboConstants.TURBO_REQUEST_HEADER, "1")

        when:
        withRequest(controller: "test")
        interceptor.before()

        then:
        request.getAttribute('turboRequest') != null
        request.getAttribute('turboRequest') instanceof TurboRequest
    }

    void "test interceptor handles multiple Turbo attributes together"() {
        given:
        request.addHeader(TurboConstants.TURBO_REQUEST_HEADER, "1")
        request.addHeader(TurboConstants.TURBO_FRAME_HEADER, "test-frame")

        when:
        withRequest(controller: "test")
        interceptor.before()

        then:
        request.getAttribute('isTurboRequest') == true
        request.getAttribute('turboFrameId') == "test-frame"
        request.getAttribute('isTurboFrameRequest') == true
    }

    void "test interceptor ignores frame headers when enableFrames is false"() {
        given:
        interceptor.turboConfig.enableFrames = false
        request.addHeader(TurboConstants.TURBO_FRAME_HEADER, "my-frame")

        when:
        withRequest(controller: "test")
        interceptor.before()

        then:
        request.getAttribute(TurboConstants.TURBO_FRAMES_DISABLED_ATTR) == true
        request.getAttribute('turboFrameId') == null
        request.getAttribute('isTurboFrameRequest') == false
    }

    void "test interceptor before always returns true to allow request to continue"() {
        when:
        withRequest(controller: "test")
        boolean result = interceptor.before()

        then:
        result
    }

    @Unroll
    void "test interceptor preserves turbo attributes for #method requests"() {
        given:
        request.addHeader(TurboConstants.TURBO_REQUEST_HEADER, "1")
        request.method = method

        when:
        withRequest(controller: "test", action: "create")
        interceptor.before()

        then:
        request.getAttribute('isTurboRequest') == true

        where:
        method << ['POST', 'PUT', 'DELETE', 'PATCH']
    }

    @Unroll
    void "request-scoped TurboRequest acceptsTurboStream=#expected when Accept (#scenario)"() {
        when:
        withRequest(controller: "test")
        request.addHeader('Accept', accept)
        interceptor.before()
        TurboRequest tr = (TurboRequest) request.getAttribute('turboRequest')

        then:
        tr.acceptsTurboStream() == expected

        where:
        scenario       | accept                                                | expected
        'turbo only'   | TurboConstants.TURBO_STREAM_MIME_TYPE                 | true
        'mixed accept' | "text/html, ${TurboConstants.TURBO_STREAM_MIME_TYPE}" | true
        'html only'    | 'text/html'                                           | false
    }

    void "interceptor leaves response format unchanged when already set (guards format block)"() {
        given:
        response.format = 'json'

        when:
        withRequest(controller: "test")
        request.addHeader('Accept', TurboConstants.TURBO_STREAM_MIME_TYPE)
        interceptor.before()

        then:
        response.format == 'json'
    }

    @Unroll
    void "when enableStreams=#enable, TurboRequest accepts turbo stream header=#accepts"() {
        given:
        interceptor.turboConfig.enableStreams = enable

        when:
        withRequest(controller: "test")
        request.addHeader('Accept', TurboConstants.TURBO_STREAM_MIME_TYPE)
        interceptor.before()
        TurboRequest tr = (TurboRequest) request.getAttribute('turboRequest')

        then:
        tr.acceptsTurboStream() == accepts
        (request.getAttribute(TurboConstants.TURBO_STREAMS_DISABLED_ATTR) == true) == streamsDisabledAttr

        where:
        enable | accepts | streamsDisabledAttr
        true   | true    | false
        false  | false   | true
    }

    void "test interceptor marks turboStreamsDisabled when enableStreams false"() {
        given:
        interceptor.turboConfig.enableStreams = false

        when:
        withRequest(controller: "test")
        interceptor.before()

        then:
        request.getAttribute(TurboConstants.TURBO_STREAMS_DISABLED_ATTR) == true
    }

    void "test interceptor handles null headers gracefully"() {
        when:
        withRequest(controller: "test")
        interceptor.before()

        then:
        noExceptionThrown()
        request.getAttribute('isTurboRequest') == false
        request.getAttribute('turboFrameId') == null
        request.getAttribute('isTurboFrameRequest') == false
    }

    void "test interceptor after returns true"() {
        when:
        boolean result = interceptor.after()

        then:
        result
    }

    void "test interceptor afterView does not throw exception"() {
        when:
        interceptor.afterView()

        then:
        noExceptionThrown()
    }
}

