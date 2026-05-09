package grails.turbo

import grails.testing.mixin.integration.Integration

/**
 * Example Geb integration test.
 * This test demonstrates the basic setup and can be used as a template.
 *
 * To run:
 * ./gradlew integrationTest
 *
 * To run with visible browser for debugging:
 * ./gradlew integrationTest -Dgeb.env=chromeHeadful
 */
@Integration
class ExampleGebSpec extends GebIntegrationSpec {

    void "test application home page is accessible"() {
        when: "we navigate to the home page"
        go '/'

        then: "the page loads successfully"
        title != null
    }

    void "example of using Geb selectors"() {
        when: "navigating to a page"
        go '/'

        then: "we can select elements"
        // Example selectors - update based on your actual pages
        // $('h1').text() == 'Expected Title'
        // $('div.class-name').size() > 0
        // $('a', text: 'Link Text').click()

        // For now, just verify we can access the page
        driver != null
    }
}

