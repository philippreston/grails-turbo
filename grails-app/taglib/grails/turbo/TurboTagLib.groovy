package grails.turbo

import grails.artefact.TagLibrary
import grails.turbo.config.TurboConfig
import org.springframework.web.util.HtmlUtils

import java.util.Locale

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
     * @attr id - Frame id (optional if {@code bean} or {@code ids} is set; see Rails dom_id)
     * @attr bean - Domain object used to derive id as Rails-style dom_id (uncapitalizedSimpleName_id, or new_uncapitalizedSimpleName if no id)
     * @attr ids - List of parts joined by "_" for composite frame ids (e.g. [userId, "tray"])
     * @attr src - The source URL to load
     * @attr loading - Loading behavior: 'eager' or 'lazy' (default: 'eager')
     * @attr target - Target frame for navigation
     * @attr busy - Whether to show busy state
     * @attr disabled - Whether frame navigation is disabled
     * @attr autoscroll - Whether to autoscroll to the frame on update
     * Additional attributes (e.g. class, style, data-*) are passed through to the element.
     */
    Closure frame = { attrs, body ->
        Map m = attrs != null ? new LinkedHashMap(attrs as Map) : [:]

        String id = (String) m.remove('id')
        Object bean = m.remove('bean')
        Object idsAttr = m.remove('ids')

        if (!id && bean != null) {
            id = turboDomId(bean)
        }
        if (!id && idsAttr != null) {
            id = joinCompositeIds(idsAttr)
        }
        if (!id) {
            throwTagError("Tag [frame] requires [id], [bean], or [ids]")
        }

        if (turboConfig != null && !turboConfig.enableFrames) {
            out << "<!-- Turbo Frame disabled: using div id=\"${escapeAttr(id)}\" -->"
            out << "<div id=\"${escapeAttr(id)}\""
            writeHtmlAttributeMap(out, m)
            out << ">"
            out << body()
            out << "</div>"
            return
        }

        out << "<!-- Turbo Frame: ${escapeAttr(id)} -->"
        out << "<turbo-frame id=\"${escapeAttr(id)}\""

        ['src', 'loading', 'target', 'busy'].each { String key ->
            if (m.containsKey(key)) {
                def v = m.remove(key)
                if (v != null) {
                    out << " ${key}=\"" << escapeAttr(v.toString()) << "\""
                }
            }
        }

        if (m.containsKey('disabled')) {
            if (truthy(m.remove('disabled'))) {
                out << " disabled"
            }
        }
        if (m.containsKey('autoscroll')) {
            if (truthy(m.remove('autoscroll'))) {
                out << " autoscroll"
            }
        }

        writeHtmlAttributeMap(out, m)

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

        if (turboConfig?.useCdn) {
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

    /**
     * Rails-like dom_id for a single object (see ActionView::RecordIdentifier#dom_id).
     */
    static String turboDomId(Object bean) {
        if (bean == null) {
            return null
        }
        Class<?> clazz = bean.getClass()
        String simple = clazz.getSimpleName()
        String base = simple ?
            simple.substring(0, 1).toLowerCase(Locale.ENGLISH) + simple.substring(1) :
            'record'
        if (bean.hasProperty('id')) {
            Object idVal = bean.getProperty('id')
            if (idVal != null) {
                return "${base}_${idVal}"
            }
        }
        return "new_${base}"
    }

    private static String joinCompositeIds(Object idsAttr) {
        if (idsAttr == null) {
            return null
        }
        List<?> list
        if (idsAttr.getClass().isArray()) {
            list = (idsAttr as Object[]).toList()
        } else if (idsAttr instanceof Iterable) {
            list = (idsAttr as Iterable).collect { it }
        } else {
            return null
        }
        List<String> parts = []
        for (Object o : list) {
            if (o != null && o.toString()) {
                parts << o.toString()
            }
        }
        parts ? parts.join('_') : null
    }

    private static boolean truthy(Object v) {
        if (v == null) {
            return false
        }
        if (v instanceof Boolean) {
            return (Boolean) v
        }
        if (v instanceof Number) {
            return ((Number) v).intValue() != 0
        }
        String s = v.toString().trim()
        if (s.equalsIgnoreCase('false') || s == '0') {
            return false
        }
        return !s.isEmpty()
    }

    private static String escapeAttr(String s) {
        HtmlUtils.htmlEscape(s ?: '', 'UTF-8')
    }

    /**
     * Emit remaining attributes (class, data-*, aria-*, etc.). Boolean true emits minimized form.
     */
    private static void writeHtmlAttributeMap(Writer out, Map m) {
        m.each { Object k, Object v ->
            if (k == null || v == null) {
                return
            }
            String name = k.toString()
            if (!safeAttributeName(name)) {
                return
            }
            if (v instanceof Boolean) {
                if ((Boolean) v) {
                    out << ' ' << name
                }
            } else {
                out << ' ' << name << '="' << escapeAttr(v.toString()) << '"'
            }
        }
    }

    private static boolean safeAttributeName(String name) {
        name ==~ /[a-zA-Z_:][a-zA-Z0-9_:.-]*/
    }
}
