<%@ page contentType="text/html;charset=UTF-8" %>
<div id="message_${message.id}" class="card mb-3">
    <div class="card-body">
        <h5 class="card-title">${message.title}</h5>
        <p class="card-text">${message.body}</p>
        <p class="card-text">
            <small class="text-muted">
                <g:formatDate date="${message.dateCreated}" format="yyyy-MM-dd HH:mm"/>
            </small>
        </p>
        <div class="btn-group" role="group">
            <g:link
                controller="example"
                action="show"
                id="${message.id}"
                class="btn btn-sm btn-primary"
                data-turbo-frame="message_${message.id}">
                View
            </g:link>
            <g:link
                controller="example"
                action="edit"
                id="${message.id}"
                class="btn btn-sm btn-secondary"
                data-turbo-frame="message_${message.id}">
                Edit
            </g:link>
            <g:form
                controller="example"
                action="delete"
                id="${message.id}"
                method="DELETE"
                style="display: inline">
                <button
                    type="submit"
                    class="btn btn-sm btn-danger"
                    data-turbo-confirm="Are you sure?">
                    Delete
                </button>
            </g:form>
        </div>
    </div>
</div>

