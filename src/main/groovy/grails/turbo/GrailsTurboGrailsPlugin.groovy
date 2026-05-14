package grails.turbo

import grails.plugins.*
import grails.turbo.config.TurboConfig

import org.springframework.core.task.SimpleAsyncTaskExecutor

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
        // Register configuration
        turboConfig(TurboConfig)

        turboStreamPublisher(NoOpTurboStreamPublisher)

        turboStreamTaskExecutor(SimpleAsyncTaskExecutor)

        // Register TurboStreamService
        turboStreamService(TurboStreamService) { bean ->
            bean.autowire = true
        }
    }}

    void doWithApplicationContext() {
        // Apply configuration from application.yml
        def config = grailsApplication.config.getProperty('grails.plugin.turbo', Map)
        if (config) {
            def turboConfig = applicationContext.getBean('turboConfig', TurboConfig)

            if (config.containsKey('turboVersion')) {
                turboConfig.turboVersion = config.turboVersion
            }
            if (config.containsKey('useCdn')) {
                turboConfig.useCdn = config.useCdn as boolean
            }
            if (config.containsKey('cdnUrl')) {
                turboConfig.cdnUrl = config.cdnUrl
            }
            if (config.containsKey('enableDrive')) {
                turboConfig.enableDrive = config.enableDrive as boolean
            }
            if (config.containsKey('enableFrames')) {
                turboConfig.enableFrames = config.enableFrames as boolean
            }
            if (config.containsKey('enableStreams')) {
                turboConfig.enableStreams = config.enableStreams as boolean
            }
            if (config.containsKey('streamSigningSecret')) {
                turboConfig.streamSigningSecret = config.streamSigningSecret as String
            }
            if (config.containsKey('globalIdApp')) {
                turboConfig.globalIdApp = config.globalIdApp as String
            }
            if (config.containsKey('metaOptions')) {
                turboConfig.metaOptions = config.metaOptions as Map<String, String>
            }
        }

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
        // Controller reloading is handled by Grails
        // TurboController trait methods are available automatically
    }

    void onConfigChange(Map<String, Object> event) {
        // Reload configuration if needed
    }

    void onShutdown(Map<String, Object> event) {
        // Cleanup if needed
    }
}