package grails.turbo

import spock.lang.Specification
import spock.lang.Unroll

import java.nio.charset.StandardCharsets

class TurboStreamNameSpec extends Specification {

    @Unroll
    void 'fromIterable joins streamables (#scenario)'() {
        expect:
        TurboStreamName.fromIterable(parts, 'application') == expected

        where:
        scenario   | parts              | expected
        'strings'  | ['5', 'entries']   | '5:entries'
        'three'    | ['a', 'b', 'c']    | 'a:b:c'
    }

    void 'nested iterables recurse'() {
        when:
        String actual = TurboStreamName.fromIterable([[81, 'tray'], 'suffix'], 'application')
        String expected = TurboStreamName.fromIterable(['81', 'tray'], 'application') + ':suffix'

        then:
        actual == expected
        actual == '81:tray:suffix'
    }

    static class PersistedDummy {
        Long id = 5L
    }

    @Unroll
    void 'primitive segments stringify (#value -> #expectedSeg)'() {
        expect:
        TurboStreamName.fromIterable([value], 'application') == expectedSeg

        where:
        value           | expectedSeg
        42L             | '42'
        true            | 'true'
        '  spaced  '    | 'spaced'
    }

    void 'domain segment uses gid urlsafe base64'() {
        when:
        String seg = TurboStreamName.toGidParam(new PersistedDummy(), 'application')
        byte[] decoded = java.util.Base64.getUrlDecoder().decode(seg)

        then:
        new String(decoded, StandardCharsets.UTF_8) == 'gid://application/PersistedDummy/5'
    }

    @Unroll
    void 'normalizeStreamables #scenario'() {
        expect:
        TurboStreamName.normalizeStreamables(raw) == expected

        where:
        scenario    | raw              | expected
        'null'      | null             | []
        'list'      | [1, 2]           | [1, 2]
        'array'     | [1, 2] as Object[] | [1, 2]
        'singleton' | 'only'           | ['only']
    }

    @Unroll
    void 'fromIterable rejects #scenario'() {
        when:
        TurboStreamName.fromIterable(input, 'application')

        then:
        thrown(IllegalArgumentException)

        where:
        scenario | input
        'null'   | null
        'empty'  | []
    }

    void 'Map streamable throws'() {
        when:
        TurboStreamName.fromIterable([[:]], 'application')

        then:
        thrown(IllegalArgumentException)
    }

    @Unroll
    void 'toGidParam rejects #scenario'() {
        when:
        TurboStreamName.toGidParam(instance, 'application')

        then:
        thrown(IllegalArgumentException)

        where:
        scenario      | instance
        'null'        | null
        'no id prop'  | new Object()
        'null id'     | new UnsavedDummy()
    }

    static class UnsavedDummy {
        Long id
    }
}
