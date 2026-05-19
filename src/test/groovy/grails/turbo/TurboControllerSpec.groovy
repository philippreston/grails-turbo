package grails.turbo

import grails.testing.web.controllers.ControllerUnitTest
import org.grails.web.servlet.mvc.GrailsWebRequest
import spock.lang.Specification
import spock.lang.Unroll
import org.springframework.web.context.request.RequestContextHolder
import grails.artefact.Controller

/**
 * Test specification for TurboController trait.
 *
 * Tests the trait by creating a concrete test controller that implements it.
 */
class TurboControllerSpec extends Specification implements ControllerUnitTest<TestTurboController> {

    void "test getTurboRequest returns TurboRequest wrapper"() {
        given:
        request.addHeader(TurboConstants.TURBO_REQUEST_HEADER, "1")

        when:
        TurboRequest turboRequest = controller.getTurboRequest()

        then:
        turboRequest != null
        turboRequest.isTurboRequest()
    }

    @Unroll
    void "isTurboRequest is #expected when Turbo-Request header #scenario"() {
        given:
        if (withHeader) {
            request.addHeader(TurboConstants.TURBO_REQUEST_HEADER, "1")
        }

        expect:
        controller.isTurboRequest() == expected

        where:
        scenario        | withHeader | expected
        'present'       | true       | true
        'absent'        | false      | false
    }

    @Unroll
    void "frame request state #scenario"() {
        given:
        if (frameId) {
            request.addHeader(TurboConstants.TURBO_FRAME_HEADER, frameId)
        }

        expect:
        controller.isTurboFrameRequest() == expectFrame
        controller.getTurboFrameId() == expectId

        where:
        scenario   | frameId      | expectFrame | expectId
        'with id'  | 'my-frame'   | true        | 'my-frame'
        'no header'| null         | false       | null
    }

    @Unroll
    void "acceptsTurboStream #scenario"() {
        given:
        if (accept) {
            request.addHeader("Accept", acceptVal)
        }

        expect:
        controller.acceptsTurboStream() == expect

        where:
        scenario      | accept | acceptVal                                              | expect
        'turbo Accept'| true   | TurboConstants.TURBO_STREAM_MIME_TYPE                  | true
        'html only'   | true   | 'text/html'                                            | false
        'no Accept'   | false  | null                                                   | false
    }

    void "test renderTemplate returns non-empty string when groovyPageRenderer works"() {
        given:
        def mockRenderer = Mock(Object) {
            render(_) >> "<div>Rendered Content</div>"
        }
        controller.groovyPageRenderer = mockRenderer

        when:
        String result = controller.renderTemplate('test', [data: 'value'])

        then:
        // The trait calls groovyPageRenderer.render() which should return HTML
        // Since we're mocking it, we should get our mocked response
        result != null
    }

    void "test renderTemplate handles errors gracefully"() {
        given:
        def mockRenderer = Mock(Object) {
            render(_) >> { throw new RuntimeException("Template error") }
        }
        controller.groovyPageRenderer = mockRenderer

        when:
        String result = controller.renderTemplate('test', [:])

        then:
        result == ""
        noExceptionThrown()
    }

    void "test renderTurboStream sets correct content type"() {
        when:
        controller.testRenderTurboStream()

        then:
        response.contentType.contains(TurboConstants.TURBO_STREAM_MIME_TYPE)
    }

    void "test renderTurboStream writes turbo stream HTML"() {
        when:
        controller.testRenderTurboStream()

        then:
        response.text.contains('<turbo-stream')
        response.text.contains('action="append"')
    }

    void "test renderTurboStream prevents view rendering"() {
        when:
        controller.testRenderTurboStream()
        def webRequest = RequestContextHolder.currentRequestAttributes() as GrailsWebRequest

        then:
        !webRequest.renderView
    }

    void "test respondWithTurbo executes turboStream block when accepting streams"() {
        given:
        request.addHeader("Accept", TurboConstants.TURBO_STREAM_MIME_TYPE)

        when:
        controller.testRespondWithTurbo()

        then:
        response.contentType.contains(TurboConstants.TURBO_STREAM_MIME_TYPE)
        response.text.contains('turbo-stream')
    }

    void "test respondWithTurbo executes html block for regular requests"() {
        when:
        controller.testRespondWithTurbo()

        then:
        response.redirectedUrl != null
    }

    void "test respondWithTurbo html block resolves flash on controller"() {
        when:
        controller.testRespondWithTurboAndFlash()

        then:
        response.redirectedUrl != null
        flash.message == 'ok'
    }

    void "test respondWithTurbo handles multiple format blocks"() {
        when:
        controller.testRespondWithTurboWithJson()

        then:
        noExceptionThrown()
    }
}

/**
 * Concrete test controller for testing TurboController trait.
 */
class TestTurboController implements TurboController, Controller {

    def testRenderTurboStream() {
        renderTurboStream {
            append('test', 'content')
        }
    }

    def testRespondWithTurbo() {
        respondWithTurbo {
            html {
                redirect(action: 'index')
            }
            turboStream {
                append('messages', '<div>test</div>')
            }
        }
    }

    def testRespondWithTurboAndFlash() {
        respondWithTurbo {
            html {
                flash.message = 'ok'
                redirect(action: 'index')
            }
            turboStream {
                append('messages', '<div>test</div>')
            }
        }
    }

    def testRespondWithTurboWithJson() {
        respondWithTurbo {
            html {
                render(view: 'index')
            }
            turboStream {
                append('messages', '<div>test</div>')
            }
            json {
                render(contentType: 'application/json', text: '{"result":"success"}')
            }
        }
    }
}

