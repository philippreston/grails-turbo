package grails.turbo.config

import spock.lang.Specification
import spock.lang.Unroll

/**
 * Test specification for TurboConfig.
 */
class TurboConfigSpec extends Specification {

    @Unroll
    void "default #property is #expected"() {
        given:
        TurboConfig config = new TurboConfig()

        expect:
        config."$property" == expected

        where:
        property                     | expected
        'turboVersion'               | '8.0.4'
        'cdnUrl'                     | 'https://cdn.jsdelivr.net/npm/@hotwired/turbo'
        'actionCablePath'            | '/cable'
        'actionCableAllowedOrigins'  | '*'
        'actionCablePingIntervalSeconds' | 3
        'globalIdApp'                | 'application'
    }

    @Unroll
    void "default boolean #property is #expected"() {
        given:
        TurboConfig config = new TurboConfig()

        expect:
        config."$property" == expected

        where:
        property         | expected
        'useCdn'         | true
        'enableDrive'    | true
        'enableFrames'   | true
        'enableStreams'  | true
        'enableActionCable' | true
    }

    @Unroll
    void "can assign #property"() {
        given:
        TurboConfig config = new TurboConfig()

        when:
        config."$property" = value

        then:
        config."$property" == value

        where:
        property        | value
        'turboVersion'  | '7.3.0'
        'cdnUrl'        | 'https://custom-cdn.com/turbo'
        'useCdn'        | false
        'enableDrive'   | false
        'enableFrames'  | false
        'enableStreams' | false
        'streamSigningSecret' | 'k'
        'globalIdApp'   | 'myapp'
    }

    void "config accepts multiple independent mutations"() {
        given:
        TurboConfig config = new TurboConfig()

        when:
        config.enableDrive = false
        config.enableFrames = false
        config.enableStreams = false
        config.turboVersion = '6.0.0'
        config.cdnUrl = 'https://new-cdn.com'
        config.useCdn = false

        then:
        !config.enableDrive
        !config.enableFrames
        !config.enableStreams
        config.turboVersion == '6.0.0'
        config.cdnUrl == 'https://new-cdn.com'
        !config.useCdn
    }
}
