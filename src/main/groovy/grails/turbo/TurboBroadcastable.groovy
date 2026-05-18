package grails.turbo

import grails.util.Holders

/**
 * Rails {@code Turbo::Broadcastable}-style helpers for broadcasting Turbo Streams from domain classes.
 * Broadcasts are scoped via {@link #turboBroadcastStreamables()} (defaults to {@code [this]} — turbo-rails
 * {@code streamables}). Override it when subscribers listen on composite names (for example {@code [post, 'comments']}).
 *
 * <p>Wire GORM lifecycle callbacks yourself and delegate to {@code turboBroadcast*} methods, for example:</p>
 * <pre>{@code
 * class Comment implements TurboBroadcastable {
 *     Post post
 *     static belongsTo = [post: Post]
 *
 *     List<Object> turboBroadcastStreamables() {
 *         [post, 'comments']
 *     }
 *
 *     def afterInsert() {
 *         turboBroadcastAppendTemplate('comments', '/comment/comment', [comment: this])
 *     }
 * }
 * }</pre>
 *
 * <p>How this relates to Rails’ declarative {@code broadcasts} DSL and suggested Grails patterns for centralizing rules are documented under
 * <strong>Declarative GORM broadcasts</strong> in AGENTS.md.</p>
 */
trait TurboBroadcastable {

    /**
     * Stream segment(s) passed to {@link TurboStreamService} {@code broadcast*} methods (Rails {@code streamables}).
     */
    List<Object> turboBroadcastStreamables() {
        [this]
    }

    void turboBroadcastAppend(String target, String html) {
        lookupTurboStreamService().broadcastAppendTo(turboBroadcastStreamables(), target, html)
    }

    void turboBroadcastAppendLater(String target, String html) {
        lookupTurboStreamService().broadcastAppendLater(turboBroadcastStreamables(), target, html)
    }

    void turboBroadcastPrepend(String target, String html) {
        lookupTurboStreamService().broadcastPrependTo(turboBroadcastStreamables(), target, html)
    }

    void turboBroadcastPrependLater(String target, String html) {
        lookupTurboStreamService().broadcastPrependLater(turboBroadcastStreamables(), target, html)
    }

    void turboBroadcastReplace(String target, String html, boolean morph = false) {
        lookupTurboStreamService().broadcastReplaceTo(turboBroadcastStreamables(), target, html, morph)
    }

    void turboBroadcastReplaceLater(String target, String html, boolean morph = false) {
        lookupTurboStreamService().broadcastReplaceLater(turboBroadcastStreamables(), target, html, morph)
    }

    void turboBroadcastUpdate(String target, String html, boolean morph = false) {
        lookupTurboStreamService().broadcastUpdateTo(turboBroadcastStreamables(), target, html, morph)
    }

    void turboBroadcastUpdateLater(String target, String html, boolean morph = false) {
        lookupTurboStreamService().broadcastUpdateLater(turboBroadcastStreamables(), target, html, morph)
    }

    void turboBroadcastRemove(String target) {
        lookupTurboStreamService().broadcastRemoveTo(turboBroadcastStreamables(), target)
    }

    void turboBroadcastRemoveLater(String target) {
        lookupTurboStreamService().broadcastRemoveLater(turboBroadcastStreamables(), target)
    }

    /**
     * Broadcast a Turbo {@code refresh} action to subscribers. Pass optional {@link TurboStreamBuilder#refresh(java.util.Map)} keys ({@code requestId}, {@code scroll}, {@code morph}, {@code method}).
     */
    void turboBroadcastRefresh(Map opts = [:]) {
        lookupTurboStreamService().broadcastRefreshTo(turboBroadcastStreamables(), opts ?: [:])
    }

    void turboBroadcastRefreshLater(Map opts = [:]) {
        lookupTurboStreamService().broadcastRefreshLater(turboBroadcastStreamables(), opts ?: [:])
    }

    void turboBroadcastRender(String turboStreamHtml) {
        lookupTurboStreamService().broadcastRenderTo(turboBroadcastStreamables(), turboStreamHtml)
    }

    void turboBroadcastRenderLater(String turboStreamHtml) {
        lookupTurboStreamService().broadcastRenderLater(turboBroadcastStreamables(), turboStreamHtml)
    }

    void turboBroadcastAppendTemplate(String target, String template, Map model = [:]) {
        lookupTurboStreamService().broadcastAppendTemplateTo(turboBroadcastStreamables(), target, template, model)
    }

    void turboBroadcastAppendTemplateLater(String target, String template, Map model = [:]) {
        lookupTurboStreamService().broadcastAppendTemplateLater(turboBroadcastStreamables(), target, template, model)
    }

    void turboBroadcastAppendAll(String targets, String html) {
        lookupTurboStreamService().broadcastAppendAllTo(turboBroadcastStreamables(), targets, html)
    }

    void turboBroadcastAppendAllLater(String targets, String html) {
        lookupTurboStreamService().broadcastAppendAllLater(turboBroadcastStreamables(), targets, html)
    }

    void turboBroadcastPrependAll(String targets, String html) {
        lookupTurboStreamService().broadcastPrependAllTo(turboBroadcastStreamables(), targets, html)
    }

    void turboBroadcastPrependAllLater(String targets, String html) {
        lookupTurboStreamService().broadcastPrependAllLater(turboBroadcastStreamables(), targets, html)
    }

    void turboBroadcastReplaceAll(String targets, String html, boolean morph = false) {
        lookupTurboStreamService().broadcastReplaceAllTo(turboBroadcastStreamables(), targets, html, morph)
    }

    void turboBroadcastReplaceAllLater(String targets, String html, boolean morph = false) {
        lookupTurboStreamService().broadcastReplaceAllLater(turboBroadcastStreamables(), targets, html, morph)
    }

    void turboBroadcastUpdateAll(String targets, String html, boolean morph = false) {
        lookupTurboStreamService().broadcastUpdateAllTo(turboBroadcastStreamables(), targets, html, morph)
    }

    void turboBroadcastUpdateAllLater(String targets, String html, boolean morph = false) {
        lookupTurboStreamService().broadcastUpdateAllLater(turboBroadcastStreamables(), targets, html, morph)
    }

    void turboBroadcastRemoveAll(String targets) {
        lookupTurboStreamService().broadcastRemoveAllTo(turboBroadcastStreamables(), targets)
    }

    void turboBroadcastRemoveAllLater(String targets) {
        lookupTurboStreamService().broadcastRemoveAllLater(turboBroadcastStreamables(), targets)
    }

    void turboBroadcastBefore(String target, String html) {
        lookupTurboStreamService().broadcastBeforeTo(turboBroadcastStreamables(), target, html)
    }

    void turboBroadcastBeforeLater(String target, String html) {
        lookupTurboStreamService().broadcastBeforeLater(turboBroadcastStreamables(), target, html)
    }

    void turboBroadcastBeforeAll(String targets, String html) {
        lookupTurboStreamService().broadcastBeforeAllTo(turboBroadcastStreamables(), targets, html)
    }

    void turboBroadcastBeforeAllLater(String targets, String html) {
        lookupTurboStreamService().broadcastBeforeAllLater(turboBroadcastStreamables(), targets, html)
    }

    void turboBroadcastAfter(String target, String html) {
        lookupTurboStreamService().broadcastAfterTo(turboBroadcastStreamables(), target, html)
    }

    void turboBroadcastAfterLater(String target, String html) {
        lookupTurboStreamService().broadcastAfterLater(turboBroadcastStreamables(), target, html)
    }

    void turboBroadcastAfterAll(String targets, String html) {
        lookupTurboStreamService().broadcastAfterAllTo(turboBroadcastStreamables(), targets, html)
    }

    void turboBroadcastAfterAllLater(String targets, String html) {
        lookupTurboStreamService().broadcastAfterAllLater(turboBroadcastStreamables(), targets, html)
    }

    void turboBroadcastReplaceTemplate(String target, String template, Map model = [:], boolean morph = false) {
        lookupTurboStreamService().broadcastReplaceTemplateTo(turboBroadcastStreamables(), target, template, model, morph)
    }

    void turboBroadcastReplaceTemplateLater(String target, String template, Map model = [:], boolean morph = false) {
        lookupTurboStreamService().broadcastReplaceTemplateLater(turboBroadcastStreamables(), target, template, model, morph)
    }

    void turboBroadcastUpdateTemplate(String target, String template, Map model = [:], boolean morph = false) {
        lookupTurboStreamService().broadcastUpdateTemplateTo(turboBroadcastStreamables(), target, template, model, morph)
    }

    void turboBroadcastUpdateTemplateLater(String target, String template, Map model = [:], boolean morph = false) {
        lookupTurboStreamService().broadcastUpdateTemplateLater(turboBroadcastStreamables(), target, template, model, morph)
    }

    void turboBroadcastPrependTemplate(String target, String template, Map model = [:]) {
        lookupTurboStreamService().broadcastPrependTemplateTo(turboBroadcastStreamables(), target, template, model)
    }

    void turboBroadcastPrependTemplateLater(String target, String template, Map model = [:]) {
        lookupTurboStreamService().broadcastPrependTemplateLater(turboBroadcastStreamables(), target, template, model)
    }

    TurboStreamService lookupTurboStreamService() {
        (TurboStreamService) Holders.applicationContext.getBean('turboStreamService')
    }
}
