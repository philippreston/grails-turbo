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

    void "test default autoInclude is true"() {
        given:
        TurboConfig config = new TurboConfig()

        expect:
        config.autoInclude
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

    void "test default enableFrames is true"() {
        given:
        TurboConfig config = new TurboConfig()

        expect:
        config.enableFrames
    }

    void "test default enableStreams is true"() {
        given:
        TurboConfig config = new TurboConfig()

        expect:
        config.enableStreams
    }

    void "test can disable drive"() {
        given:
        TurboConfig config = new TurboConfig()

        when:
        config.enableDrive = false

        then:
        !config.enableDrive
    }

    void "test can disable frames"() {
        given:
        TurboConfig config = new TurboConfig()

        when:
        config.enableFrames = false

        then:
        !config.enableFrames
    }

    void "test can disable streams"() {
        given:
        TurboConfig config = new TurboConfig()

        when:
        config.enableStreams = false

        then:
        !config.enableStreams
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

    void "test can disable autoInclude"() {
        given:
        TurboConfig config = new TurboConfig()

        when:
        config.autoInclude = false

        then:
        !config.autoInclude
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
        config.enableFrames = false
        config.enableStreams = false
        config.turboVersion = '6.0.0'
        config.cdnUrl = 'https://new-cdn.com'
        config.autoInclude = false
        config.useCdn = false

        then:
        !config.enableDrive
        !config.enableFrames
        !config.enableStreams
        config.turboVersion == '6.0.0'
        config.cdnUrl == 'https://new-cdn.com'
        !config.autoInclude
        !config.useCdn
    }

    void "test config can represent minimal setup"() {
        given:
        TurboConfig config = new TurboConfig()

        when:
        config.enableDrive = true
        config.enableFrames = false
        config.enableStreams = false

        then:
        config.enableDrive
        !config.enableFrames
        !config.enableStreams
    }

    void "test config can represent streams-only setup"() {
        given:
        TurboConfig config = new TurboConfig()

        when:
        config.enableDrive = false
        config.enableFrames = false
        config.enableStreams = true

        then:
        !config.enableDrive
        !config.enableFrames
        config.enableStreams
    }
}

