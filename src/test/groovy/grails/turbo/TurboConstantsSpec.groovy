package grails.turbo

import spock.lang.Specification
import spock.lang.Unroll

/**
 * Test specification for TurboConstants.
 */
class TurboConstantsSpec extends Specification {

    @Unroll
    void "constant #field == #value"() {
        expect:
        TurboConstants[field] == value

        where:
        field                      | value
        'TURBO_REQUEST_HEADER'     | 'Turbo-Request'
        'TURBO_FRAME_HEADER'       | 'Turbo-Frame'
        'TURBO_FRAMES_DISABLED_ATTR' | 'turboFramesDisabled'
        'TURBO_STREAMS_DISABLED_ATTR' | 'turboStreamsDisabled'
        'DEFAULT_STREAMS_CHANNEL'  | 'Turbo::StreamsChannel'
        'TURBO_STREAM_MIME_TYPE'   | 'text/vnd.turbo-stream.html'
        'TURBO_STREAM_FORMAT'      | 'turbo_stream'
        'ACTION_APPEND'            | 'append'
        'ACTION_PREPEND'           | 'prepend'
        'ACTION_REPLACE'           | 'replace'
        'ACTION_UPDATE'            | 'update'
        'ACTION_REMOVE'            | 'remove'
        'ACTION_BEFORE'            | 'before'
        'ACTION_AFTER'             | 'after'
        'ACTION_REFRESH'           | 'refresh'
    }

    void "stream action constants are unique"() {
        given:
        List<String> actions = [
            TurboConstants.ACTION_APPEND,
            TurboConstants.ACTION_PREPEND,
            TurboConstants.ACTION_REPLACE,
            TurboConstants.ACTION_UPDATE,
            TurboConstants.ACTION_REMOVE,
            TurboConstants.ACTION_BEFORE,
            TurboConstants.ACTION_AFTER,
            TurboConstants.ACTION_REFRESH,
        ]

        expect:
        actions.size() == actions.unique().size()
    }

    @Unroll
    void "non-action constant #field is non-blank"() {
        expect:
        TurboConstants[field]?.toString()?.trim()

        where:
        field << [
            'TURBO_REQUEST_HEADER',
            'TURBO_FRAME_HEADER',
            'TURBO_FRAMES_DISABLED_ATTR',
            'TURBO_STREAMS_DISABLED_ATTR',
            'DEFAULT_STREAMS_CHANNEL',
            'TURBO_STREAM_MIME_TYPE',
            'TURBO_STREAM_FORMAT',
        ]
    }
}
