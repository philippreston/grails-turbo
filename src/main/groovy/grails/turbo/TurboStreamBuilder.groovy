package grails.turbo

import org.springframework.web.util.HtmlUtils

/**
 * Builder for Turbo Stream responses (Hotwired {@code turbo-stream} elements).
 */
class TurboStreamBuilder {

    private final StringBuilder output = new StringBuilder()

    private static String esc(String s) {
        HtmlUtils.htmlEscape(s ?: '', 'UTF-8')
    }

    TurboStreamBuilder append(String target, String content) {
        streamWithTarget(TurboConstants.ACTION_APPEND, target, content, false)
        this
    }

    TurboStreamBuilder appendAll(String targets, String content) {
        streamWithTargets(TurboConstants.ACTION_APPEND, targets, content, false)
        this
    }

    TurboStreamBuilder prepend(String target, String content) {
        streamWithTarget(TurboConstants.ACTION_PREPEND, target, content, false)
        this
    }

    TurboStreamBuilder prependAll(String targets, String content) {
        streamWithTargets(TurboConstants.ACTION_PREPEND, targets, content, false)
        this
    }

    TurboStreamBuilder replace(String target, String content) {
        replace(target, content, false)
    }

    TurboStreamBuilder replace(String target, String content, boolean morph) {
        streamWithTarget(TurboConstants.ACTION_REPLACE, target, content, morph)
        this
    }

    TurboStreamBuilder replaceAll(String targets, String content) {
        replaceAll(targets, content, false)
    }

    TurboStreamBuilder replaceAll(String targets, String content, boolean morph) {
        streamWithTargets(TurboConstants.ACTION_REPLACE, targets, content, morph)
        this
    }

    TurboStreamBuilder update(String target, String content) {
        update(target, content, false)
    }

    TurboStreamBuilder update(String target, String content, boolean morph) {
        streamWithTarget(TurboConstants.ACTION_UPDATE, target, content, morph)
        this
    }

    TurboStreamBuilder updateAll(String targets, String content) {
        updateAll(targets, content, false)
    }

    TurboStreamBuilder updateAll(String targets, String content, boolean morph) {
        streamWithTargets(TurboConstants.ACTION_UPDATE, targets, content, morph)
        this
    }

    TurboStreamBuilder remove(String target) {
        output.append('<turbo-stream action="')
        output.append(esc(TurboConstants.ACTION_REMOVE))
        output.append('" target="')
        output.append(esc(target))
        output.append('"></turbo-stream>')
        this
    }

    TurboStreamBuilder removeAll(String targets) {
        output.append('<turbo-stream action="')
        output.append(esc(TurboConstants.ACTION_REMOVE))
        output.append('" targets="')
        output.append(esc(targets))
        output.append('"></turbo-stream>')
        this
    }

    TurboStreamBuilder before(String target, String content) {
        streamWithTarget(TurboConstants.ACTION_BEFORE, target, content, false)
        this
    }

    TurboStreamBuilder beforeAll(String targets, String content) {
        streamWithTargets(TurboConstants.ACTION_BEFORE, targets, content, false)
        this
    }

    TurboStreamBuilder after(String target, String content) {
        streamWithTarget(TurboConstants.ACTION_AFTER, target, content, false)
        this
    }

    TurboStreamBuilder afterAll(String targets, String content) {
        streamWithTargets(TurboConstants.ACTION_AFTER, targets, content, false)
        this
    }

    /**
     * Page refresh stream (Turbo 8). No {@code template} wrapper.
     * <p>Optional {@code opts} keys (camelCase or {@code 'request-id'}): {@code requestId}, {@code scroll},
     * {@code morph} ({@code true} → {@code method="morph"}), {@code method} (raw stream {@code method} when {@code morph} is false).</p>
     */
    TurboStreamBuilder refresh(Map opts = [:]) {
        appendRefreshOpening(opts ?: [:])
        output.append('></turbo-stream>')
        this
    }

    private void appendRefreshOpening(Map opts) {
        output.append('<turbo-stream action="')
        output.append(esc(TurboConstants.ACTION_REFRESH))
        output.append('"')
        String requestId = (opts.requestId ?: opts.'request-id')?.toString()?.trim()
        if (requestId) {
            output.append(' request-id="')
            output.append(esc(requestId))
            output.append('"')
        }
        String scroll = opts.scroll?.toString()?.trim()
        if (scroll) {
            output.append(' scroll="')
            output.append(esc(scroll))
            output.append('"')
        }
        if (truthyOpt(opts.morph)) {
            output.append(' method="morph"')
        } else {
            String rawMethod = opts.method?.toString()?.trim()
            if (rawMethod) {
                output.append(' method="')
                output.append(esc(rawMethod))
                output.append('"')
            }
        }
    }

    private static boolean truthyOpt(Object v) {
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
        if (!s || s.equalsIgnoreCase('false') || s == '0') {
            return false
        }
        return true
    }

    /**
     * Low-level: single {@code target} id.
     */
    TurboStreamBuilder stream(String action, String target, String content, boolean useTargetsAttribute = false) {
        if (useTargetsAttribute) {
            streamWithTargets(action, target, content, false)
        } else {
            streamWithTarget(action, target, content, false)
        }
        this
    }

    String build() {
        output.toString()
    }

    @Override
    String toString() {
        build()
    }

    private void streamWithTarget(String action, String target, String content, boolean morph) {
        appendOpen(action, target, null, morph)
        appendTemplate(content)
        output.append('</turbo-stream>')
    }

    private void streamWithTargets(String action, String targets, String content, boolean morph) {
        appendOpen(action, null, targets, morph)
        appendTemplate(content)
        output.append('</turbo-stream>')
    }

    private void appendOpen(String action, String target, String targets, boolean morph) {
        output.append('<turbo-stream action="')
        output.append(esc(action))
        output.append('"')
        if (targets) {
            output.append(' targets="')
            output.append(esc(targets))
            output.append('"')
        } else if (target) {
            output.append(' target="')
            output.append(esc(target))
            output.append('"')
        }
        if (morph && (TurboConstants.ACTION_REPLACE == action || TurboConstants.ACTION_UPDATE == action)) {
            output.append(' method="morph"')
        }
        output.append('>')
    }

    private void appendTemplate(String content) {
        output.append('<template>')
        if (content) {
            output.append(content)
        }
        output.append('</template>')
    }
}
