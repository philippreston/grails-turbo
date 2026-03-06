package grails.turbo

import grails.artefact.TagLibrary
import grails.turbo.config.TurboConfig

/**
 * Tag library for Hotwired Turbo support.
 * Provides tags for creating Turbo Frames and Streams.
 */
class TurboTagLib implements TagLibrary {

    static namespace = "turbo"

    TurboConfig turboConfig

    /**
     * Creates a turbo-frame tag.
     *
     * @attr id REQUIRED - The unique identifier for the frame
     * @attr src - The source URL to load
     * @attr loading - Loading behavior: 'eager' or 'lazy' (default: 'eager')
     * @attr target - Target frame for navigation
     * @attr busy - Whether to show busy state
     * @attr disabled - Whether frame navigation is disabled
     * @attr autoscroll - Whether to autoscroll to the frame on update
     */
    Closure frame = { attrs, body ->
        String id = attrs.id
        if (!id) {
            throwTagError("Tag [frame] is missing required attribute [id]")
        }

        out << "<!-- Turbo Frame: ${id} -->"
        out << "<turbo-frame id=\"${id}\""

        if (attrs.src) {
            out << " src=\"${attrs.src}\""
        }
        if (attrs.loading) {
            out << " loading=\"${attrs.loading}\""
        }
        if (attrs.target) {
            out << " target=\"${attrs.target}\""
        }
        if (attrs.busy) {
            out << " busy=\"${attrs.busy}\""
        }
        if (attrs.disabled) {
            out << " disabled"
        }
        if (attrs.autoscroll) {
            out << " autoscroll"
        }

        out << ">"
        out << body()
        out << "</turbo-frame>"
    }

    /**
     * Creates a turbo-stream tag for server-generated updates.
     *
     * @attr action REQUIRED - The action to perform: append, prepend, replace, update, remove, before, after
     * @attr target REQUIRED - The target element ID (required for all actions except remove)
     * @attr targets - CSS selector for multiple targets
     */
    Closure stream = { attrs, body ->
        String action = attrs.action
        if (!action) {
            throwTagError("Tag [stream] is missing required attribute [action]")
        }

        String target = attrs.target
        String targets = attrs.targets

        if (!target && !targets && action != 'remove') {
            throwTagError("Tag [stream] requires either [target] or [targets] attribute")
        }

        out << "<turbo-stream action=\"${action}\""

        if (target) {
            out << " target=\"${target}\""
        }
        if (targets) {
            out << " targets=\"${targets}\""
        }

        out << ">"

        if (action != 'remove') {
            out << "<template>"
            out << body()
            out << "</template>"
        }

        out << "</turbo-stream>"
    }

    /**
     * Creates a turbo-cable-stream-source tag for streaming over WebSocket.
     *
     * @attr channel REQUIRED - The cable channel to subscribe to
     * @attr signedStreamName - The signed stream name for authentication
     */
    Closure cableStreamSource = { attrs ->
        String channel = attrs.channel
        if (!channel) {
            throwTagError("Tag [cableStreamSource] is missing required attribute [channel]")
        }

        out << "<turbo-cable-stream-source channel=\"${channel}\""

        if (attrs.signedStreamName) {
            out << " signed-stream-name=\"${attrs.signedStreamName}\""
        }

        out << "></turbo-cable-stream-source>"
    }

    /**
     * Refreshes the page using Turbo morphing.
     *
     * @attr method - HTTP method to use for refresh (default: 'replace')
     * @attr scroll - Scroll behavior: 'preserve' or 'reset'
     */
    Closure pageRefresh = { attrs ->
        out << "<meta name=\"turbo-refresh-method\" content=\"${attrs.method ?: 'replace'}\">"
        if (attrs.scroll) {
            out << "<meta name=\"turbo-refresh-scroll\" content=\"${attrs.scroll}\">"
        }
    }

    /**
     * Include Turbo JavaScript library.
     * Uses configuration from TurboConfig if available.
     *
     * @attr version - Turbo version to use (overrides config)
     * @attr cdnUrl - CDN URL to use (overrides config)
     */
    Closure includeTurbo = { attrs ->
        String version = attrs.version ?: turboConfig?.turboVersion
        String cdnUrl = attrs.cdnUrl ?: turboConfig?.cdnUrl

        if(turboConfig.useCdn) {
            out << "<script type=\"module\" src=\"${cdnUrl}@${version}/dist/turbo.es2017-esm.js\"></script>"
        }

        // Add meta tags from configuration
        if (turboConfig?.metaOptions) {
            turboConfig.metaOptions.each { key, value ->
                out << "<meta name=\"turbo-${key}\" content=\"${value}\">"
            }
        }

        // Add Drive configuration if disabled
        if (turboConfig && !turboConfig.enableDrive) {
            out << "<meta name=\"turbo-visit-control\" content=\"reload\">"
        }
    }
}

