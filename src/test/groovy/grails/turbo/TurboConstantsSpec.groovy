package grails.turbo

import spock.lang.Specification

/**
 * Test specification for TurboConstants.
 */
class TurboConstantsSpec extends Specification {

    void "test TURBO_REQUEST_HEADER constant"() {
        expect:
        TurboConstants.TURBO_REQUEST_HEADER == 'Turbo-Request'
    }

    void "test TURBO_FRAME_HEADER constant"() {
        expect:
        TurboConstants.TURBO_FRAME_HEADER == 'Turbo-Frame'
    }

    void "test TURBO_FRAMES_DISABLED_ATTR constant"() {
        expect:
        TurboConstants.TURBO_FRAMES_DISABLED_ATTR == 'turboFramesDisabled'
    }

    void "test TURBO_STREAM_MIME_TYPE constant"() {
        expect:
        TurboConstants.TURBO_STREAM_MIME_TYPE == 'text/vnd.turbo-stream.html'
    }

    void "test TURBO_STREAM_FORMAT constant"() {
        expect:
        TurboConstants.TURBO_STREAM_FORMAT == 'turbo_stream'
    }

    void "test ACTION_APPEND constant"() {
        expect:
        TurboConstants.ACTION_APPEND == 'append'
    }

    void "test ACTION_PREPEND constant"() {
        expect:
        TurboConstants.ACTION_PREPEND == 'prepend'
    }

    void "test ACTION_REPLACE constant"() {
        expect:
        TurboConstants.ACTION_REPLACE == 'replace'
    }

    void "test ACTION_UPDATE constant"() {
        expect:
        TurboConstants.ACTION_UPDATE == 'update'
    }

    void "test ACTION_REMOVE constant"() {
        expect:
        TurboConstants.ACTION_REMOVE == 'remove'
    }

    void "test ACTION_BEFORE constant"() {
        expect:
        TurboConstants.ACTION_BEFORE == 'before'
    }

    void "test ACTION_AFTER constant"() {
        expect:
        TurboConstants.ACTION_AFTER == 'after'
    }

    void "test ACTION_REFRESH constant"() {
        expect:
        TurboConstants.ACTION_REFRESH == 'refresh'
    }

    void "test all action constants are unique"() {
        given:
        def actions = [
            TurboConstants.ACTION_APPEND,
            TurboConstants.ACTION_PREPEND,
            TurboConstants.ACTION_REPLACE,
            TurboConstants.ACTION_UPDATE,
            TurboConstants.ACTION_REMOVE,
            TurboConstants.ACTION_BEFORE,
            TurboConstants.ACTION_AFTER,
            TurboConstants.ACTION_REFRESH
        ]

        expect:
        actions.size() == actions.unique().size()
    }

    void "test constants are not null"() {
        expect:
        TurboConstants.TURBO_REQUEST_HEADER != null
        TurboConstants.TURBO_FRAME_HEADER != null
        TurboConstants.TURBO_FRAMES_DISABLED_ATTR != null
        TurboConstants.TURBO_STREAM_MIME_TYPE != null
        TurboConstants.TURBO_STREAM_FORMAT != null
        TurboConstants.ACTION_APPEND != null
        TurboConstants.ACTION_PREPEND != null
        TurboConstants.ACTION_REPLACE != null
        TurboConstants.ACTION_UPDATE != null
        TurboConstants.ACTION_REMOVE != null
        TurboConstants.ACTION_BEFORE != null
        TurboConstants.ACTION_AFTER != null
        TurboConstants.ACTION_REFRESH != null
    }

    void "test constants are not empty strings"() {
        expect:
        !TurboConstants.TURBO_REQUEST_HEADER.isEmpty()
        !TurboConstants.TURBO_FRAME_HEADER.isEmpty()
        !TurboConstants.TURBO_FRAMES_DISABLED_ATTR.isEmpty()
        !TurboConstants.TURBO_STREAM_MIME_TYPE.isEmpty()
        !TurboConstants.TURBO_STREAM_FORMAT.isEmpty()
        !TurboConstants.ACTION_APPEND.isEmpty()
        !TurboConstants.ACTION_PREPEND.isEmpty()
        !TurboConstants.ACTION_REPLACE.isEmpty()
        !TurboConstants.ACTION_UPDATE.isEmpty()
        !TurboConstants.ACTION_REMOVE.isEmpty()
        !TurboConstants.ACTION_BEFORE.isEmpty()
        !TurboConstants.ACTION_AFTER.isEmpty()
        !TurboConstants.ACTION_REFRESH.isEmpty()
    }
}

