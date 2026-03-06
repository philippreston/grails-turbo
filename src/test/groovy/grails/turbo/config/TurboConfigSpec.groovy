package grails.turbo.config

import spock.lang.Specification

/**
 * Test specification for TurboConfig.
 */
class TurboConfigSpec extends Specification {

    void "test default turboVersion is 8.0.4"() {
        given:
        TurboConfig config = new TurboConfig()

        expect:
        config.turboVersion == '8.0.4'
    }


    void "test default useCdn is true"() {
        given:
        TurboConfig config = new TurboConfig()

        expect:
        config.useCdn
    }

    void "test default CDN URL is jsdelivr"() {
        given:
        TurboConfig config = new TurboConfig()

        expect:
        config.cdnUrl == 'https://cdn.jsdelivr.net/npm/@hotwired/turbo'
    }

    void "test default enableDrive is true"() {
        given:
        TurboConfig config = new TurboConfig()

        expect:
        config.enableDrive
    }

    void "test can disable drive"() {
        given:
        TurboConfig config = new TurboConfig()

        when:
        config.enableDrive = false

        then:
        !config.enableDrive
    }


    void "test can set custom version"() {
        given:
        TurboConfig config = new TurboConfig()

        when:
        config.turboVersion = '7.3.0'

        then:
        config.turboVersion == '7.3.0'
    }

    void "test can set custom CDN URL"() {
        given:
        TurboConfig config = new TurboConfig()

        when:
        config.cdnUrl = 'https://custom-cdn.com/turbo'

        then:
        config.cdnUrl == 'https://custom-cdn.com/turbo'
    }


    void "test can disable CDN usage"() {
        given:
        TurboConfig config = new TurboConfig()

        when:
        config.useCdn = false

        then:
        !config.useCdn
    }

    void "test config properties are mutable"() {
        given:
        TurboConfig config = new TurboConfig()

        when:
        config.enableDrive = false
        config.turboVersion = '6.0.0'
        config.cdnUrl = 'https://new-cdn.com'
        config.useCdn = false

        then:
        !config.enableDrive
        config.turboVersion == '6.0.0'
        config.cdnUrl == 'https://new-cdn.com'
        !config.useCdn
    }

    void "test config can disable drive"() {
        given:
        TurboConfig config = new TurboConfig()

        when:
        config.enableDrive = false

        then:
        !config.enableDrive
    }
}

