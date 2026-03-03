<%@ page contentType="text/html;charset=UTF-8" %>
<div class="card">
    <div class="card-body">
        <h5 class="card-title">${message.title}</h5>
        <p class="card-text">${message.body}</p>
        <hr>
        <p class="text-muted">
            <strong>Created:</strong> <g:formatDate date="${message.dateCreated}" format="yyyy-MM-dd HH:mm"/><br>
            <strong>Updated:</strong> <g:formatDate date="${message.lastUpdated}" format="yyyy-MM-dd HH:mm"/>
        </p>
        <p class="text-muted">
            <em>This content was lazy-loaded using Turbo Frames!</em>
        </p>
    </div>
</div>

