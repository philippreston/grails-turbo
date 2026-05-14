package grails.turbo

import grails.util.Holders

/**
 * Optional GORM-friendly helpers: broadcasts scoped to {@code this} as the sole streamable
 * (Rails {@code Broadcastable}-style). For composite stream names (e.g. {@code [this, 'comments']}),
 * call {@link TurboStreamService} directly.
 */
trait TurboBroadcasts {

    void turboBroadcastAppend(String target, String html) {
        lookupTurboStreamService().broadcastAppendTo([this], target, html)
    }

    void turboBroadcastAppendLater(String target, String html) {
        lookupTurboStreamService().broadcastAppendLater([this], target, html)
    }

    void turboBroadcastPrepend(String target, String html) {
        lookupTurboStreamService().broadcastPrependTo([this], target, html)
    }

    void turboBroadcastPrependLater(String target, String html) {
        lookupTurboStreamService().broadcastPrependLater([this], target, html)
    }

    void turboBroadcastReplace(String target, String html, boolean morph = false) {
        lookupTurboStreamService().broadcastReplaceTo([this], target, html, morph)
    }

    void turboBroadcastReplaceLater(String target, String html, boolean morph = false) {
        lookupTurboStreamService().broadcastReplaceLater([this], target, html, morph)
    }

    void turboBroadcastUpdate(String target, String html, boolean morph = false) {
        lookupTurboStreamService().broadcastUpdateTo([this], target, html, morph)
    }

    void turboBroadcastUpdateLater(String target, String html, boolean morph = false) {
        lookupTurboStreamService().broadcastUpdateLater([this], target, html, morph)
    }

    void turboBroadcastRemove(String target) {
        lookupTurboStreamService().broadcastRemoveTo([this], target)
    }

    void turboBroadcastRemoveLater(String target) {
        lookupTurboStreamService().broadcastRemoveLater([this], target)
    }

    void turboBroadcastRefresh() {
        lookupTurboStreamService().broadcastRefreshTo([this])
    }

    void turboBroadcastRefreshLater() {
        lookupTurboStreamService().broadcastRefreshLater([this])
    }

    void turboBroadcastRender(String turboStreamHtml) {
        lookupTurboStreamService().broadcastRenderTo([this], turboStreamHtml)
    }

    void turboBroadcastRenderLater(String turboStreamHtml) {
        lookupTurboStreamService().broadcastRenderLater([this], turboStreamHtml)
    }

    void turboBroadcastAppendTemplate(String target, String template, Map model = [:]) {
        lookupTurboStreamService().broadcastAppendTemplateTo([this], target, template, model)
    }

    void turboBroadcastAppendTemplateLater(String target, String template, Map model = [:]) {
        lookupTurboStreamService().broadcastAppendTemplateLater([this], target, template, model)
    }

    private TurboStreamService lookupTurboStreamService() {
        (TurboStreamService) Holders.applicationContext.getBean('turboStreamService')
    }
}
