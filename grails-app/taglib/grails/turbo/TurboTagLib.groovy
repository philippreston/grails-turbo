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
     * @attr prefix - Optional segment prefixed before dom_id(bean), Rails {@code dom_id(record, :detail)} style (e.g. {@code edit} → {@code edit_message_1})
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
        Object prefixAttr = m.remove('prefix')

        if (!id && bean != null) {
            id = prefixAttr != null ? turboDomId(bean, prefixAttr) : turboDomId(bean)
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
     * @attr action REQUIRED - The action to perform: append, prepend, replace, update, remove, before, after, refresh
     * @attr target - Single element id
     * @attr targets - CSS selector for multiple targets (append_all style)
     * @attr morph - If true on replace/update, emit {@code method="morph"} (Turbo 8)
     */
    Closure stream = { attrs, body ->
        Map m = attrs != null ? new LinkedHashMap(attrs as Map) : [:]
        String action = (String) m.remove('action')
        if (!action) {
            throwTagError("Tag [stream] is missing required attribute [action]")
        }

        boolean morph = truthy(m.remove('morph'))
        String target = (String) m.remove('target')
        String targets = (String) m.remove('targets')

        if (!target && !targets && action != 'remove') {
            throwTagError("Tag [stream] requires either [target] or [targets] attribute")
        }

        out << '<turbo-stream action="' << escapeAttr(action) << '"'
        if (target) {
            out << ' target="' << escapeAttr(target) << '"'
        }
        if (targets) {
            out << ' targets="' << escapeAttr(targets) << '"'
        }
        if (morph && (action == TurboConstants.ACTION_REPLACE || action == TurboConstants.ACTION_UPDATE)) {
            out << ' method="morph"'
        }
        out << '>'

        if (action != TurboConstants.ACTION_REMOVE) {
            out << '<template>'
            out << body()
            out << '</template>'
        }

        out << '</turbo-stream>'
    }

    /**
     * Rails {@code turbo_stream_from}: emits {@code turbo-cable-stream-source} with signed stream identifier.
     *
     * @attr streamables REQUIRED - Iterable, array, or comma-separated equivalents (e.g. {@code "${[account,'entries']}"})
     * @attr channel - Channel class/name (default {@code Turbo::StreamsChannel})
     *
     * Requires {@link grails.turbo.config.TurboConfig#streamSigningSecret} (Rails verifier-compatible).
     * The legacy {@link #cableStreamSource} tag remains for low-level use but is deprecated.
     */
    Closure streamFrom = { attrs ->
        if (turboConfig != null && !turboConfig.enableStreams) {
            out << '<!-- turbo:streamFrom skipped (enableStreams=false) -->'
            return
        }

        Map m = attrs != null ? new LinkedHashMap(attrs as Map) : [:]
        Object raw = m.remove('streamables')
        if (raw == null) {
            throwTagError("Tag [streamFrom] is missing required attribute [streamables]")
        }

        List<?> streamables = TurboStreamName.normalizeStreamables(raw)
        if (streamables.isEmpty()) {
            throwTagError('Tag [streamFrom] requires non-blank streamables')
        }

        String app = turboConfig?.globalIdApp ?: 'application'
        String canonical = TurboStreamName.fromIterable(streamables, app)

        String channel = (m.remove('channel') ?: TurboConstants.DEFAULT_STREAMS_CHANNEL).toString()
        String signingSecret = turboConfig?.streamSigningSecret
        if (!signingSecret?.trim()) {
            throwTagError('Tag [streamFrom] requires grails.plugin.turbo.streamSigningSecret (Rails Turbo.signed_stream_verifier key)')
        }

        String signed = new TurboRailsMessageVerifier(signingSecret.trim()).generate(canonical)

        out << '<turbo-cable-stream-source channel="' << escapeAttr(channel) << '"'
        out << ' signed-stream-name="' << escapeAttr(signed) << '"'
        writeHtmlAttributeMap(out, m)
        out << '></turbo-cable-stream-source>'
    }

    /**
     * @deprecated Use {@link #streamFrom}.
     */
    @Deprecated
    Closure cableStreamSource = { attrs ->
        String channel = attrs.channel
        if (!channel) {
            throwTagError("Tag [cableStreamSource] is missing required attribute [channel]")
        }

        out << "<turbo-cable-stream-source channel=\"${escapeAttr(channel)}\""

        if (attrs.signedStreamName) {
            out << " signed-stream-name=\"${escapeAttr(attrs.signedStreamName)}\""
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
     * Turbo Drive {@code turbo-visit-control} meta (reload, advance, etc.).
     *
     * @attr content - Meta content value (default {@code reload})
     */
    Closure visitControl = { attrs ->
        Map m = attrs != null ? new LinkedHashMap(attrs as Map) : [:]
        String content = (m.remove('content') ?: 'reload').toString()
        out << "<meta name=\"turbo-visit-control\" content=\"${escapeAttr(content)}\">"
    }

    /**
     * Turbo {@code turbo-cache-control} meta ({@code no-cache}, {@code no-preview}, …).
     *
     * @attr content REQUIRED
     */
    Closure cacheControl = { attrs ->
        Map m = attrs != null ? new LinkedHashMap(attrs as Map) : [:]
        String content = (String) m.remove('content')
        if (!content?.trim()) {
            throwTagError('Tag [cacheControl] is missing required attribute [content]')
        }
        String escaped = escapeAttr(content.trim())
        out << "<meta name=\"turbo-cache-control\" content=\"${escaped}\">"
    }

    /**
     * Include Turbo JavaScript library.
     * Uses configuration from TurboConfig if available.
     *
     * When {@link TurboConfig#enableStreams} and {@link TurboConfig#enableActionCable} are both true,
     * loads the {@code @hotwired/turbo-rails} browser bundle (Turbo + Action Cable + {@code turbo-cable-stream-source}).
     * Otherwise loads the {@code @hotwired/turbo} ESM build only.
     *
     * For correct load order, use {@code metasOnly="true"} in {@code layout head} and {@code scriptsOnly="true"}
     * before other scripts at the end of {@code body}. Omit both for a single combined include (metas then script).
     *
     * @attr version - Turbo / turbo-rails version when using CDN (overrides config)
     * @attr cdnUrl - CDN base for Turbo ESM only (overrides config); not used for turbo-rails bundle
     * @attr turboRailsVersion - optional override for {@code @hotwired/turbo-rails} version (defaults to {@code version} / config)
     * @attr metasOnly - if true, emit only meta tags (for {@code head})
     * @attr scriptsOnly - if true, emit only script tag(s) (for end of {@code body})
     */
    Closure includeTurbo = { attrs ->
        Map m = attrs != null ? new LinkedHashMap(attrs as Map) : [:]
        boolean metasOnly = truthy(m.remove('metasOnly'))
        boolean scriptsOnly = truthy(m.remove('scriptsOnly'))

        String version = (m.remove('version') ?: turboConfig?.turboVersion)?.toString()
        String cdnUrl = (m.remove('cdnUrl') ?: turboConfig?.cdnUrl)?.toString()
        String turboRailsVersion = (m.remove('turboRailsVersion') ?: version)?.toString()

        if (metasOnly && scriptsOnly) {
            throwTagError('Tag [includeTurbo] cannot set both [metasOnly] and [scriptsOnly]')
        }

        if (!scriptsOnly) {
            writeIncludeTurboMetas(out, cableStreams())
        }
        if (!metasOnly) {
            writeIncludeTurboScript(out, version, cdnUrl, turboRailsVersion, cableStreams())
        }
    }

    private boolean cableStreams() {
        turboConfig?.enableStreams && turboConfig?.enableActionCable
    }

    private void writeIncludeTurboMetas(Writer out, boolean cableStreamsEnabled) {
        if (cableStreamsEnabled) {
            String configured = turboConfig.actionCableUrl ?: turboConfig.actionCablePath ?: '/cable'
            String cableUrl = resolveActionCableWebSocketUrl(configured)
            out << "<meta name=\"action-cable-url\" content=\"${escapeAttr(cableUrl)}\">"
        }

        if (turboConfig?.metaOptions) {
            turboConfig.metaOptions.each { key, value ->
                out << "<meta name=\"turbo-${key}\" content=\"${value}\">"
            }
        }

        if (turboConfig && !turboConfig.enableDrive) {
            out << "<meta name=\"turbo-visit-control\" content=\"reload\">"
        }
    }

    /**
     * Rails-style Action Cable URL for {@code <meta name="action-cable-url">}: when the configured value is
     * a path (no {@code ://}), build {@code ws(s)://host:port/path} from the current request so browser clients
     * (e.g. @rails/actioncable) connect reliably on non-default ports and in headless integration tests.
     */
    private String resolveActionCableWebSocketUrl(String configured) {
        if (!configured) {
            configured = '/cable'
        }
        if (configured.contains('://')) {
            return configured
        }
        String path = configured.startsWith('/') ? configured : "/${configured}"
        try {
            def req = request
            if (req != null) {
                String httpScheme = req.scheme?.toLowerCase(Locale.ROOT) ?: 'http'
                String wsScheme = 'https' == httpScheme ? 'wss' : 'ws'
                String host = req.serverName ?: 'localhost'
                int port = req.serverPort
                boolean defaultPort = port <= 0 ||
                    ('http' == httpScheme && port == 80) ||
                    ('https' == httpScheme && port == 443)
                String portPart = defaultPort ? '' : ":${port}"
                return "${wsScheme}://${host}${portPart}${path}"
            }
        } catch (Exception ignored) {
            // No request in context (e.g. some unit tests): fall back to path-only
        }
        return path
    }

    private void writeIncludeTurboScript(Writer out, String version, String cdnUrl, String turboRailsVersion, boolean cableStreamsEnabled) {
        if (!turboConfig?.useCdn) {
            return
        }
        if (cableStreamsEnabled) {
            // npm package "main" is an ES module (ends with export { Turbo, cable }); must use type="module".
            out << '<script type="module" src="https://cdn.jsdelivr.net/npm/@hotwired/turbo-rails@' << escapeAttr(turboRailsVersion)
            out << '/app/assets/javascripts/turbo.min.js"></script>'
        } else {
            out << "<script type=\"module\" src=\"${escapeAttr(cdnUrl)}@${escapeAttr(version)}/dist/turbo.es2017-esm.js\"></script>"
        }
    }

    /**
     * Rails-like dom_id for a single object (see ActionView::RecordIdentifier#dom_id).
     *
     * @param prefixSegment Optional prefix placed before the record key ({@code dom_id(record, :edit)} → {@code edit_message_1})
     */
    static String turboDomId(Object bean, Object prefixSegment = null) {
        if (bean == null) {
            return null
        }
        Class<?> clazz = bean.getClass()
        String simple = clazz.getSimpleName()
        String baseName = simple ?
            simple.substring(0, 1).toLowerCase(Locale.ENGLISH) + simple.substring(1) :
            'record'
        String recordKey
        if (bean.hasProperty('id')) {
            Object idVal = bean.getProperty('id')
            if (idVal != null) {
                recordKey = "${baseName}_${idVal}"
            } else {
                recordKey = "new_${baseName}"
            }
        } else {
            recordKey = "new_${baseName}"
        }
        if (prefixSegment == null) {
            return recordKey
        }
        String p = prefixSegment.toString().trim()
        if (!p) {
            return recordKey
        }
        return "${p}_${recordKey}"
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
