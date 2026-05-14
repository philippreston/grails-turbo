package grails.turbo

import spock.lang.Specification

import java.nio.charset.StandardCharsets

class TurboStreamNameSpec extends Specification {

    void 'joins primitives with colons'() {
        expect:
        TurboStreamName.fromIterable(['5', 'entries'], 'application') == '5:entries'
        TurboStreamName.fromIterable(['a', 'b', 'c'], 'application') == 'a:b:c'
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

    void 'domain segment uses gid urlsafe base64'() {
        when:
        String seg = TurboStreamName.toGidParam(new PersistedDummy(), 'application')
        byte[] decoded = java.util.Base64.getUrlDecoder().decode(seg)

        then:
        new String(decoded, StandardCharsets.UTF_8) == 'gid://application/PersistedDummy/5'
    }

    void 'normalizeStreamables flattens array iterable and singleton'() {
        expect:
        TurboStreamName.normalizeStreamables(null) == []
        TurboStreamName.normalizeStreamables([1, 2]) == [1, 2]
        TurboStreamName.normalizeStreamables([1, 2] as Object[]) == [1, 2]
        TurboStreamName.normalizeStreamables('only') == ['only']
    }
}
