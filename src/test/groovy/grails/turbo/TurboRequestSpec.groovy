package grails.turbo

import spock.lang.Specification

import javax.servlet.http.HttpServletRequest

/**
 * Test specification for TurboRequest class.
 */
class TurboRequestSpec extends Specification {

    void "test isTurboRequest returns true when header is present"() {
        given:
        HttpServletRequest request = Mock(HttpServletRequest)
        request.getHeader(TurboConstants.TURBO_REQUEST_HEADER) >> "1"

        when:
        TurboRequest turboRequest = new TurboRequest(request)

        then:
        turboRequest.isTurboRequest()
    }

    void "test isTurboRequest returns false when header is absent"() {
        given:
        HttpServletRequest request = Mock(HttpServletRequest)
        request.getHeader(TurboConstants.TURBO_REQUEST_HEADER) >> null

        when:
        TurboRequest turboRequest = new TurboRequest(request)

        then:
        !turboRequest.isTurboRequest()
    }

    void "test isTurboFrameRequest returns true when frame header is present"() {
        given:
        HttpServletRequest request = Mock(HttpServletRequest)
        request.getHeader(TurboConstants.TURBO_FRAME_HEADER) >> "my-frame"

        when:
        TurboRequest turboRequest = new TurboRequest(request)

        then:
        turboRequest.isTurboFrameRequest()
        turboRequest.getTurboFrameId() == "my-frame"
    }

    void "test acceptsTurboStream returns true when MIME type is in Accept header"() {
        given:
        HttpServletRequest request = Mock(HttpServletRequest)
        request.getHeader("Accept") >> "text/html, ${TurboConstants.TURBO_STREAM_MIME_TYPE}"

        when:
        TurboRequest turboRequest = new TurboRequest(request)

        then:
        turboRequest.acceptsTurboStream()
    }

    void "test acceptsTurboStream returns false when MIME type is not in Accept header"() {
        given:
        HttpServletRequest request = Mock(HttpServletRequest)
        request.getHeader("Accept") >> "text/html"

        when:
        TurboRequest turboRequest = new TurboRequest(request)

        then:
        !turboRequest.acceptsTurboStream()
    }

    void "test getFormat returns turbo_stream when accepts turbo stream"() {
        given:
        HttpServletRequest request = Mock(HttpServletRequest)
        request.getHeader("Accept") >> TurboConstants.TURBO_STREAM_MIME_TYPE

        when:
        TurboRequest turboRequest = new TurboRequest(request)

        then:
        turboRequest.getFormat() == TurboConstants.TURBO_STREAM_FORMAT
    }
}

