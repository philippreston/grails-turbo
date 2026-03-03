<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="turbo"/>
    <title>Turbo Examples - Messages</title>
</head>
<body>

<div class="container mt-4">
    <h1>Turbo Examples</h1>

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
            <h2>Messages <span id="message-count" class="badge badge-secondary">${Message.count()}</span></h2>

            <!-- Turbo Frame for messages list -->
            <turbo:frame id="messages">
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

            <!-- This frame will lazy-load its content -->
            <turbo:frame
                id="lazy-content"
                src="${createLink(controller: 'example', action: 'lazyLoad', params: [id: 1])}"
                loading="lazy">
                <div class="text-center p-4">
                    <div class="spinner-border" role="status">
                        <span class="sr-only">Loading...</span>
                    </div>
                </div>
            </turbo:frame>
        </div>
    </div>
</div>

</body>
</html>

