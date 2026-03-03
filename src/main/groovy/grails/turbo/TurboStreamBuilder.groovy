package grails.turbo

import groovy.transform.CompileStatic

/**
 * Builder for creating Turbo Stream responses.
 */
@CompileStatic
class TurboStreamBuilder {

    private final StringBuilder output = new StringBuilder()

    /**
     * Append content to a target element.
     */
    TurboStreamBuilder append(String target, String content) {
        stream(TurboConstants.ACTION_APPEND, target, content)
        return this
    }

    /**
     * Prepend content to a target element.
     */
    TurboStreamBuilder prepend(String target, String content) {
        stream(TurboConstants.ACTION_PREPEND, target, content)
        return this
    }

    /**
     * Replace a target element.
     */
    TurboStreamBuilder replace(String target, String content) {
        stream(TurboConstants.ACTION_REPLACE, target, content)
        return this
    }

    /**
     * Update the content of a target element (replaces inner HTML).
     */
    TurboStreamBuilder update(String target, String content) {
        stream(TurboConstants.ACTION_UPDATE, target, content)
        return this
    }

    /**
     * Remove a target element.
     */
    TurboStreamBuilder remove(String target) {
        output.append("<turbo-stream action=\"${TurboConstants.ACTION_REMOVE}\" target=\"${target}\">")
        output.append("</turbo-stream>")
        return this
    }

    /**
     * Insert content before a target element.
     */
    TurboStreamBuilder before(String target, String content) {
        stream(TurboConstants.ACTION_BEFORE, target, content)
        return this
    }

    /**
     * Insert content after a target element.
     */
    TurboStreamBuilder after(String target, String content) {
        stream(TurboConstants.ACTION_AFTER, target, content)
        return this
    }

    /**
     * Trigger a page refresh.
     */
    TurboStreamBuilder refresh() {
        output.append("<turbo-stream action=\"${TurboConstants.ACTION_REFRESH}\">")
        output.append("</turbo-stream>")
        return this
    }

    /**
     * Add a custom stream action with multiple targets (CSS selector).
     */
    TurboStreamBuilder stream(String action, String targets, String content, boolean useTargetsAttribute = false) {
        output.append("<turbo-stream action=\"${action}\"")

        if (useTargetsAttribute) {
            output.append(" targets=\"${targets}\"")
        } else {
            output.append(" target=\"${targets}\"")
        }

        output.append(">")

        if (content && action != TurboConstants.ACTION_REMOVE) {
            output.append("<template>")
            output.append(content)
            output.append("</template>")
        }

        output.append("</turbo-stream>")
        return this
    }

    /**
     * Build the final Turbo Stream response.
     */
    String build() {
        return output.toString()
    }

    @Override
    String toString() {
        return build()
    }
}

