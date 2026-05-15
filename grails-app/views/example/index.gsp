<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="turbo"/>
    <title>Turbo Examples - Messages</title>
</head>
<body>

<div class="container mt-4">
    <h1>Turbo Examples</h1>

    <p>
        <g:link controller="example" action="streamJobDemo" class="btn btn-outline-primary btn-sm">Turbo Stream job demo</g:link>
    </p>

    <div class="alert alert-info">
        <strong>Turbo Status:</strong>
        <g:if test="${isTurboRequest}">
            This is a Turbo request
        </g:if>
        <g:else>
            This is a regular request
        </g:else>

        <g:if test="${isTurboFrameRequest}">
            | Frame: ${turboFrameId}
        </g:if>
    </div>

    <div class="row">
        <div class="col-md-8">
            <h2>Messages <span id="message-count" class="badge badge-secondary">${messageCount ?: 0}</span></h2>

            <!-- Turbo Frame for messages list -->
            <turbo:frame id="messages">
                <g:if test="${!messages}">
                    <div id="empty-message" class="alert alert-warning">No messages yet. Create one using the form on the right.</div>
                </g:if>
                <div id="messages-list">
                    <g:each in="${messages}" var="message">
                        <g:render template="message" model="[message: message]"/>
                    </g:each>
                </div>
            </turbo:frame>
        </div>

        <div class="col-md-4">
            <h2>Add Message</h2>

            <!-- Turbo Frame for the form -->
            <turbo:frame id="message-form">
                <g:render template="form" model="[message: new grails.turbo.example.Message()]"/>
            </turbo:frame>
        </div>
    </div>

    <div class="row mt-4">
        <div class="col-md-12">
            <h2>Lazy-Loaded Frame Example</h2>
            <p class="text-muted">This frame loads its content asynchronously after a 1-second delay when it comes into view.</p>

            <!-- This frame will lazy-load its content -->
            <turbo:frame
                id="lazy-content"
                src="${createLink(controller: 'example', action: 'lazyLoad', params: messages ? [id: messages[0].id] : [:])}"
                loading="${lazyFrameLoading ?: 'lazy'}">
                <div class="text-center p-4">
                    <div class="spinner-border" role="status">
                        <span class="sr-only">Loading...</span>
                    </div>
                    <p class="mt-2">Loading content...</p>
                </div>
            </turbo:frame>
        </div>
    </div>
</div>

</body>
</html>

