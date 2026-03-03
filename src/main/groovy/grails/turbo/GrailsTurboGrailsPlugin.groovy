package grails.turbo

import grails.plugins.*
import grails.turbo.config.TurboConfig

/**
 * Grails Turbo Plugin
 *
 * Provides integration with Hotwired Turbo (https://turbo.hotwired.dev/) for building
 * single-page application behavior in Grails applications.
 *
 * This plugin is inspired by turbo-rails and provides similar functionality for Grails,
 * including Turbo Drive, Turbo Frames, and Turbo Streams.
 */
class GrailsTurboGrailsPlugin extends Plugin {

    def grailsVersion = "6.0.0 > *"

    def pluginExcludes = [
        "grails-app/views/error.gsp",
        "grails-app/views/index.gsp",
        "grails-app/views/notFound.gsp"
    ]

    def title = "Grails Turbo"
    def author = "Grails Community"
    def authorEmail = ""
    def description = '''\
Hotwired Turbo integration for Grails. Provides Turbo Drive for fast page navigation,
Turbo Frames for lazy-loading and scoped updates, and Turbo Streams for real-time updates
over HTTP and WebSocket connections.
'''
    def profiles = ['web']
    def documentation = "https://github.com/grails/grails-turbo"

    def license = "APACHE"

    def issueManagement = [system: "GitHub", url: "https://github.com/grails/grails-turbo/issues"]
    def scm = [url: "https://github.com/grails/grails-turbo"]

    // Load order - should load after core plugins
    def loadAfter = ['controllers', 'services', 'urlMappings']

    // Watch for changes in these artefacts
    def watchedResources = [
        "file:./grails-app/controllers/**/*Controller.groovy",
        "file:./grails-app/services/**/*Service.groovy",
        "file:./grails-app/views/**/*.gsp"
    ]

    Closure doWithSpring() { {->
        // Register TurboStreamService
        turboStreamService(TurboStreamService) { bean ->
            bean.autowire = true
        }

        // Register configuration
        turboConfig(TurboConfig)
    }}

    void doWithDynamicMethods() {
        // Add turbo-related methods to controllers
        grailsApplication.controllerClasses?.each { controllerClass ->
            addTurboMethods(controllerClass.clazz)
        }
    }

    void doWithApplicationContext() {
        // Register Turbo Stream MIME type
        try {
            def mimeTypes = grailsApplication.config.getProperty('grails.mime.types', Map)
            if (mimeTypes && !mimeTypes.containsKey(TurboConstants.TURBO_STREAM_FORMAT)) {
                mimeTypes[TurboConstants.TURBO_STREAM_FORMAT] = [TurboConstants.TURBO_STREAM_MIME_TYPE]
            }
        } catch (Exception e) {
            log.warn("Could not register Turbo Stream MIME type: ${e.message}")
        }
    }

    void onChange(Map<String, Object> event) {
        if (event.source) {
            try {
                if (grailsApplication.isControllerClass(event.source)) {
                    addTurboMethods(event.source as Class)
                }
            } catch (Exception e) {
                log.debug("Could not add Turbo methods to changed class: ${e.message}")
            }
        }
    }

    void onConfigChange(Map<String, Object> event) {
        // Reload configuration if needed
    }

    void onShutdown(Map<String, Object> event) {
        // Cleanup if needed
    }

    /**
     * Add Turbo helper methods to controller classes.
     */
    private void addTurboMethods(Class controllerClass) {
        // Add getTurboRequest method
        controllerClass.metaClass.getTurboRequest = {->
            return new TurboRequest(delegate.request)
        }

        // Add isTurboRequest method
        controllerClass.metaClass.isTurboRequest = {->
            return delegate.getTurboRequest().isTurboRequest()
        }

        // Add isTurboFrameRequest method
        controllerClass.metaClass.isTurboFrameRequest = {->
            return delegate.getTurboRequest().isTurboFrameRequest()
        }

        // Add getTurboFrameId method
        controllerClass.metaClass.getTurboFrameId = {->
            return delegate.getTurboRequest().getTurboFrameId()
        }

        // Add acceptsTurboStream method
        controllerClass.metaClass.acceptsTurboStream = {->
            return delegate.getTurboRequest().acceptsTurboStream()
        }
    }
}