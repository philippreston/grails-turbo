package grails.turbo

import grails.core.GrailsApplication
import grails.turbo.config.TurboConfig
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.core.task.TaskExecutor
import org.springframework.web.context.request.RequestContextHolder

/**
 * Service for creating and broadcasting Turbo Stream messages.
 * Broadcaster methods send HTML to {@link TurboStreamPublisher} (default: in-memory Action Cable; replace the bean for Redis-backed fan-out, etc.).
 */
class TurboStreamService {

    GrailsApplication grailsApplication
    TurboConfig turboConfig
    TurboStreamPublisher turboStreamPublisher
    TaskExecutor turboStreamTaskExecutor

    /**
     * Create a new Turbo Stream builder.
     */
    TurboStreamBuilder builder() {
        return new TurboStreamBuilder()
    }

    /**
     * Render a template with the given model.
     */
    String renderTemplate(String template, Map model = [:]) {
        try {
            GrailsWebRequest webRequest = (GrailsWebRequest) RequestContextHolder.currentRequestAttributes()
            def controller = webRequest.getCurrentController()

            // Use the controller's render method to render the template
            if (controller) {
                def output = new StringWriter()
                controller.render(template: template, model: model, writer: output)
                return output.toString()
            }
        } catch (Exception e) {
            // If we can't get the request context, return empty string
        }
        return ""
    }

    /**
     * Create a Turbo Stream to append content to a target.
     */
    String append(String target, String content) {
        return builder().append(target, content).build()
    }

    String appendAll(String targets, String content) {
        builder().appendAll(targets, content).build()
    }

    /**
     * Create a Turbo Stream to prepend content to a target.
     */
    String prepend(String target, String content) {
        return builder().prepend(target, content).build()
    }

    String prependAll(String targets, String content) {
        builder().prependAll(targets, content).build()
    }

    /**
     * Create a Turbo Stream to replace a target.
     */
    String replace(String target, String content) {
        return builder().replace(target, content).build()
    }

    String replace(String target, String content, boolean morph) {
        builder().replace(target, content, morph).build()
    }

    String replaceAll(String targets, String content) {
        builder().replaceAll(targets, content).build()
    }

    String replaceAll(String targets, String content, boolean morph) {
        builder().replaceAll(targets, content, morph).build()
    }

    /**
     * Create a Turbo Stream to update a target's inner HTML.
     */
    String update(String target, String content) {
        return builder().update(target, content).build()
    }

    String update(String target, String content, boolean morph) {
        builder().update(target, content, morph).build()
    }

    String updateAll(String targets, String content) {
        builder().updateAll(targets, content).build()
    }

    String updateAll(String targets, String content, boolean morph) {
        builder().updateAll(targets, content, morph).build()
    }

    /**
     * Create a Turbo Stream to remove a target.
     */
    String remove(String target) {
        return builder().remove(target).build()
    }

    String removeAll(String targets) {
        builder().removeAll(targets).build()
    }

    /**
     * Create a Turbo Stream to insert content before a target.
     */
    String before(String target, String content) {
        return builder().before(target, content).build()
    }

    String beforeAll(String targets, String content) {
        builder().beforeAll(targets, content).build()
    }

    /**
     * Create a Turbo Stream to insert content after a target.
     */
    String after(String target, String content) {
        return builder().after(target, content).build()
    }

    String afterAll(String targets, String content) {
        builder().afterAll(targets, content).build()
    }

    /**
     * Create a Turbo Stream to trigger a page refresh.
     *
     * @param opts Same keys as {@link TurboStreamBuilder#refresh(java.util.Map)} (optional {@code requestId}, {@code scroll}, {@code morph}, {@code method})
     */
    String refresh(Map opts = [:]) {
        return builder().refresh(opts ?: [:]).build()
    }

    // --- Broker-agnostic broadcasts (turbo-rails-style naming) ---

    void broadcastAppendTo(Object streamables, String target, String content) {
        publishStream(streamables, append(target, content))
    }

    void broadcastAppendLater(Object streamables, String target, String content) {
        runLater { broadcastAppendTo(streamables, target, content) }
    }

    void broadcastAppendAllTo(Object streamables, String targets, String content) {
        publishStream(streamables, appendAll(targets, content))
    }

    void broadcastAppendAllLater(Object streamables, String targets, String content) {
        runLater { broadcastAppendAllTo(streamables, targets, content) }
    }

    void broadcastPrependTo(Object streamables, String target, String content) {
        publishStream(streamables, prepend(target, content))
    }

    void broadcastPrependLater(Object streamables, String target, String content) {
        runLater { broadcastPrependTo(streamables, target, content) }
    }

    void broadcastPrependAllTo(Object streamables, String targets, String content) {
        publishStream(streamables, prependAll(targets, content))
    }

    void broadcastPrependAllLater(Object streamables, String targets, String content) {
        runLater { broadcastPrependAllTo(streamables, targets, content) }
    }

    void broadcastReplaceTo(Object streamables, String target, String content, boolean morph = false) {
        publishStream(streamables, replace(target, content, morph))
    }

    void broadcastReplaceLater(Object streamables, String target, String content, boolean morph = false) {
        runLater { broadcastReplaceTo(streamables, target, content, morph) }
    }

    void broadcastReplaceAllTo(Object streamables, String targets, String content, boolean morph = false) {
        publishStream(streamables, replaceAll(targets, content, morph))
    }

    void broadcastReplaceAllLater(Object streamables, String targets, String content, boolean morph = false) {
        runLater { broadcastReplaceAllTo(streamables, targets, content, morph) }
    }

    void broadcastUpdateTo(Object streamables, String target, String content, boolean morph = false) {
        publishStream(streamables, update(target, content, morph))
    }

    void broadcastUpdateLater(Object streamables, String target, String content, boolean morph = false) {
        runLater { broadcastUpdateTo(streamables, target, content, morph) }
    }

    void broadcastUpdateAllTo(Object streamables, String targets, String content, boolean morph = false) {
        publishStream(streamables, updateAll(targets, content, morph))
    }

    void broadcastUpdateAllLater(Object streamables, String targets, String content, boolean morph = false) {
        runLater { broadcastUpdateAllTo(streamables, targets, content, morph) }
    }

    void broadcastRemoveTo(Object streamables, String target) {
        publishStream(streamables, remove(target))
    }

    void broadcastRemoveLater(Object streamables, String target) {
        runLater { broadcastRemoveTo(streamables, target) }
    }

    void broadcastRemoveAllTo(Object streamables, String targets) {
        publishStream(streamables, removeAll(targets))
    }

    void broadcastRemoveAllLater(Object streamables, String targets) {
        runLater { broadcastRemoveAllTo(streamables, targets) }
    }

    void broadcastBeforeTo(Object streamables, String target, String content) {
        publishStream(streamables, before(target, content))
    }

    void broadcastBeforeLater(Object streamables, String target, String content) {
        runLater { broadcastBeforeTo(streamables, target, content) }
    }

    void broadcastBeforeAllTo(Object streamables, String targets, String content) {
        publishStream(streamables, beforeAll(targets, content))
    }

    void broadcastBeforeAllLater(Object streamables, String targets, String content) {
        runLater { broadcastBeforeAllTo(streamables, targets, content) }
    }

    void broadcastAfterTo(Object streamables, String target, String content) {
        publishStream(streamables, after(target, content))
    }

    void broadcastAfterLater(Object streamables, String target, String content) {
        runLater { broadcastAfterTo(streamables, target, content) }
    }

    void broadcastAfterAllTo(Object streamables, String targets, String content) {
        publishStream(streamables, afterAll(targets, content))
    }

    void broadcastAfterAllLater(Object streamables, String targets, String content) {
        runLater { broadcastAfterAllTo(streamables, targets, content) }
    }

    void broadcastRefreshTo(Object streamables, Map opts = [:]) {
        publishStream(streamables, refresh(opts ?: [:]))
    }

    void broadcastRefreshLater(Object streamables, Map opts = [:]) {
        runLater { broadcastRefreshTo(streamables, opts ?: [:]) }
    }

    /**
     * Publish a pre-built Turbo Stream fragment (one or more {@code turbo-stream} elements).
     */
    void broadcastRenderTo(Object streamables, String turboStreamHtml) {
        publishStream(streamables, turboStreamHtml ?: '')
    }

    void broadcastRenderLater(Object streamables, String turboStreamHtml) {
        runLater { broadcastRenderTo(streamables, turboStreamHtml) }
    }

    void broadcastAppendTemplateTo(Object streamables, String target, String template, Map model = [:]) {
        broadcastAppendTo(streamables, target, renderTemplate(template, model))
    }

    void broadcastAppendTemplateLater(Object streamables, String target, String template, Map model = [:]) {
        runLater { broadcastAppendTemplateTo(streamables, target, template, model) }
    }

    void broadcastPrependTemplateTo(Object streamables, String target, String template, Map model = [:]) {
        broadcastPrependTo(streamables, target, renderTemplate(template, model))
    }

    void broadcastPrependTemplateLater(Object streamables, String target, String template, Map model = [:]) {
        runLater { broadcastPrependTemplateTo(streamables, target, template, model) }
    }

    void broadcastReplaceTemplateTo(Object streamables, String target, String template, Map model = [:], boolean morph = false) {
        broadcastReplaceTo(streamables, target, renderTemplate(template, model), morph)
    }

    void broadcastReplaceTemplateLater(Object streamables, String target, String template, Map model = [:], boolean morph = false) {
        runLater { broadcastReplaceTemplateTo(streamables, target, template, model, morph) }
    }

    void broadcastUpdateTemplateTo(Object streamables, String target, String template, Map model = [:], boolean morph = false) {
        broadcastUpdateTo(streamables, target, renderTemplate(template, model), morph)
    }

    void broadcastUpdateTemplateLater(Object streamables, String target, String template, Map model = [:], boolean morph = false) {
        runLater { broadcastUpdateTemplateTo(streamables, target, template, model, morph) }
    }

    private void publishStream(Object streamables, String html) {
        turboStreamPublisher.publish(resolvePublishStreamName(streamables), html)
    }

    private String resolvePublishStreamName(Object streamablesArg) {
        List<Object> list = TurboStreamName.normalizeStreamables(streamablesArg)
        if (list.isEmpty()) {
            throw new IllegalArgumentException('streamables cannot be blank')
        }
        String app = turboConfig?.globalIdApp ?: 'application'
        // Same key as turbo-rails after verifying signed_stream_name (unsigned canonical name).
        TurboStreamName.fromIterable(list, app)
    }

    private void runLater(Runnable task) {
        if (turboStreamTaskExecutor != null) {
            turboStreamTaskExecutor.execute(task)
        } else {
            task.run()
        }
    }
}

