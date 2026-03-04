/**
 * Hotwired Turbo Integration for Grails
 * This file loads Turbo from CDN and provides initialization
 */

// Load Turbo from CDN
(function() {
    const script = document.createElement('script');
    script.type = 'module';
    script.src = 'https://cdn.jsdelivr.net/npm/@hotwired/turbo@8.0.4/dist/turbo.es2017-esm.js';
    document.head.appendChild(script);

    // Add event listeners for Turbo navigation
    document.addEventListener('turbo:load', function() {
        console.log('Turbo loaded');
    });

    document.addEventListener('turbo:before-fetch-request', function(event) {
        console.log('Turbo fetch:', event.detail.url.href);
    });

    document.addEventListener('turbo:frame-load', function(event) {
        console.log('Turbo frame loaded:', event.target.id);
    });
})();

