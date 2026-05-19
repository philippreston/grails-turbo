package grails.turbo

import spock.lang.Specification
import spock.lang.Unroll

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

    @Unroll
    void "isTurboFrameRequest when frame header=#frameHeader and frames disabled=#disabled"() {
        given:
        HttpServletRequest request = Mock(HttpServletRequest)
        request.getAttribute(TurboConstants.TURBO_FRAMES_DISABLED_ATTR) >> (disabled ? true : null)
        request.getHeader(TurboConstants.TURBO_FRAME_HEADER) >> frameHeader

        when:
        TurboRequest turboRequest = new TurboRequest(request)

        then:
        turboRequest.isTurboFrameRequest() == expectFrame
        turboRequest.getTurboFrameId() == expectId
        turboRequest.isTurboFramesDisabled() == disabled

        where:
        frameHeader | disabled | expectFrame | expectId
        'my-frame'  | false    | true        | 'my-frame'
        'my-frame'  | true     | false       | null
        null        | false    | false       | null
    }

    @Unroll
    void "acceptsTurboStream is #accepted for Accept: #acceptHeader"() {
        given:
        HttpServletRequest request = Mock(HttpServletRequest)
        request.getHeader("Accept") >> acceptHeader
        request.getAttribute(TurboConstants.TURBO_STREAMS_DISABLED_ATTR) >> null

        when:
        TurboRequest turboRequest = new TurboRequest(request)

        then:
        turboRequest.acceptsTurboStream() == accepted

        where:
        acceptHeader                                                               | accepted
        TurboConstants.TURBO_STREAM_MIME_TYPE                                     | true
        "text/html, ${TurboConstants.TURBO_STREAM_MIME_TYPE}"                     | true
        "text/html, ${TurboConstants.TURBO_STREAM_MIME_TYPE};q=0.9, */*;q=0.1"      | true
        'text/html, application/json'                                             | false
        null                                                                       | false
        'text/html'                                                                | false
    }

    @Unroll
    void "getFormat is #expect when streams disabled=#disabled and Accept streams=#accepts"() {
        given:
        HttpServletRequest request = Mock(HttpServletRequest)
        request.getHeader("Accept") >> (accepts ? TurboConstants.TURBO_STREAM_MIME_TYPE : 'text/html')
        request.getAttribute(TurboConstants.TURBO_STREAMS_DISABLED_ATTR) >> (disabled ? true : null)

        when:
        TurboRequest turboRequest = new TurboRequest(request)

        then:
        turboRequest.getFormat() == expect
        turboRequest.isTurboStreamsDisabled() == disabled

        where:
        disabled | accepts | expect
        false    | true    | TurboConstants.TURBO_STREAM_FORMAT
        false    | false   | null
        true     | true    | null
    }
}
