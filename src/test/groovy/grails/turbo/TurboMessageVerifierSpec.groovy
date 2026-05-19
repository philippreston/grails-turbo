package grails.turbo

import spock.lang.Specification
import spock.lang.Unroll

class TurboMessageVerifierSpec extends Specification {

    /** Byte-for-byte match for Ruby ActiveSupport::MessageVerifier.new(secret, digest: SHA256, serializer: JSON).generate(...) */
    private static final String RAILS_VECTOR_SIGNED =
        'ImFjY291bnQ6NTplbnRyaWVzIg==--0d2b973a988b4a72c1149397ca128c3f78a92322a7c869e40cccde480accac26'

    void 'generate matches Rails 7 SHA256 JSON MessageVerifier'() {
        given:
        TurboMessageVerifier v = new TurboMessageVerifier('secret')

        expect:
        v.generate('account:5:entries') == RAILS_VECTOR_SIGNED
    }

    void 'verified round-trips plaintext'() {
        given:
        TurboMessageVerifier v = new TurboMessageVerifier('secret')

        when:
        String signed = v.generate('hello:wired')
        String decoded = v.verified(signed)
        String tampered = v.verified(signed + 'x')

        then:
        decoded == 'hello:wired'
        v.validMessage(signed)
        tampered == null
    }

    @Unroll
    void 'generate + verified round-trip for #payload with secret=#secret'() {
        given:
        TurboMessageVerifier v = new TurboMessageVerifier(secret)

        when:
        String signed = v.generate(payload)

        then:
        v.verified(signed) == payload

        where:
        payload        | secret
        'a:b'          | 'x'
        '你好:wired'   | '🔑key'
    }
}
