package grails.turbo.pages

import geb.Page

/**
 * Page object for the Turbo Stream (Action Cable) job status demo.
 */
class TurboStreamJobDemoPage extends Page {

    static url = '/example/streamJobDemo'

    static at = { $('h1').text()?.contains('Turbo Stream job status') }

    static content = {
        jobStatus { $('#job-status') }
        jobTime { $('#job-time') }
        streamSource { $('turbo-cable-stream-source') }
    }
}
