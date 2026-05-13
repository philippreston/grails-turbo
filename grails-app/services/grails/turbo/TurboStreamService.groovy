package grails.turbo

import grails.core.GrailsApplication
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.web.context.request.RequestContextHolder

/**
 * Service for creating and broadcasting Turbo Stream messages.
 */
class TurboStreamService {

    GrailsApplication grailsApplication

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
     */
    String refresh() {
        return builder().refresh().build()
    }
}

