<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="turbo"/>
    <title>Turbo Stream job demo</title>
</head>
<body>

<div class="container mt-4">
    <h1>Turbo Stream job status</h1>
    <p class="text-muted">
        Status updates are pushed over Turbo Streams (Action Cable). Initial state is <strong>Pending</strong>;
        after 2 seconds the server broadcasts <strong>Running</strong> with a timestamp, then after 5 more seconds
        <strong>Complete</strong> with an updated time.
    </p>

    <div class="card mb-3" style="max-width: 32rem;">
        <div class="card-body">
            <h5 class="card-title">Job</h5>
            <div id="job-status-panel">
                <span id="job-status">Pending</span>
                <span id="job-time">N/A</span>
            </div>
        </div>
    </div>

    <p>
        <g:link controller="example" action="index" class="btn btn-outline-secondary">Back to messages demo</g:link>
    </p>

    <turbo:streamFrom streamables="${['streamDemo', jobId]}"/>
</div>

</body>
</html>
