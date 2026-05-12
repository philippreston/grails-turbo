package grails.turbo.config

import groovy.transform.CompileStatic

/**
 * Configuration class for Turbo plugin settings.
 */
@CompileStatic
class TurboConfig {

    /**
     * Version of the Turbo JavaScript library to use.
     * Default: 8.0.4
     */
    String turboVersion = '8.0.4'

    /**
     * Whether to use CDN for Turbo JavaScript.
     * Default: true
     */
    boolean useCdn = true

    /**
     * CDN URL for Turbo JavaScript.
     */
    String cdnUrl = 'https://cdn.jsdelivr.net/npm/@hotwired/turbo'

    /**
     * Whether to enable Turbo Drive (automatic page navigation).
     * When false, adds meta tag to disable Drive globally.
     * Default: true
     */
    boolean enableDrive = true

    /**
     * Whether to enable Turbo Frames (scoped updates, lazy src frames).
     * When false, {@code turbo:frame} renders a plain {@code div} with the same id and
     * {@link TurboRequest#isTurboFrameRequest()} is forced false for the request.
     * Default: true
     */
    boolean enableFrames = true


    /**
     * Custom Turbo configuration options to be added as meta tags.
     */
    Map<String, String> metaOptions = [:]
}

