package grails.turbo

import grails.testing.mixin.integration.Integration
import grails.turbo.pages.TurboStreamJobDemoPage
import spock.lang.Timeout

import java.util.concurrent.TimeUnit

/**
 * Geb end-to-end check for {@code /example/streamJobDemo}: Turbo + Action Cable must load in the browser
 * and DOM updates must reflect server-pushed turbo-stream messages (Running then Complete).
 */
@Timeout(value = 120, unit = TimeUnit.SECONDS)
@Integration
class TurboStreamJobGebSpec extends GebIntegrationSpec {

    void 'streamJobDemo page receives Running then Complete via turbo streams'() {
        when: 'opening the stream demo page'
        to TurboStreamJobDemoPage

        then: 'initial state and cable source present'
        waitFor(20, 0.25) { streamSource.size() > 0 }
        jobStatus.text().trim() == 'Pending'
        jobTime.text().trim() == 'N/A'

        and: 'server pushes Running with a UTC timestamp (~2s after load)'
        waitFor(45, 0.25) {
            jobStatus.text().trim() == 'Running' &&
                jobTime.text().trim() != 'N/A' &&
                jobTime.text().trim().matches(/^\d{4}-\d{2}-\d{2}T.*/)
        }

        and: 'server pushes Complete with a new timestamp (~7s after load)'
        String runningTime = jobTime.text().trim()
        waitFor(45, 0.25) {
            jobStatus.text().trim() == 'Complete' &&
                jobTime.text().trim() != runningTime
        }
    }
}
