package grails.turbo

import geb.spock.GebSpec
import grails.testing.mixin.integration.Integration
import grails.testing.spock.OnceBefore

/**
 * Base class for Geb integration tests.
 * Provides common setup and utility methods for testing with Geb.
 *
 * Usage:
 * <pre>
 * {@literal @}Integration
 * class MyPageSpec extends GebIntegrationSpec {
 *     void "test page"() {
 *         when:
 *         go '/path'
 *
 *         then:
 *         $('h1').text() == 'Expected Title'
 *     }
 * }
 * </pre>
 */
abstract class GebIntegrationSpec extends GebSpec {

    @OnceBefore
    void setupBaseUrl() {
        // This will be injected by the integration test framework
        // The baseUrl is configured in GebConfig.groovy
    }
}

