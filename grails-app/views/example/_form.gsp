<%@ page contentType="text/html;charset=UTF-8" %>
<g:form controller="example" action="create" method="POST">
    <div class="form-group">
        <label for="title">Title</label>
        <g:textField
            name="title"
            value="${message?.title}"
            class="form-control ${hasErrors(bean: message, field: 'title', 'is-invalid')}"
            required="required"/>
        <g:hasErrors bean="${message}" field="title">
            <div class="invalid-feedback">
                <g:renderErrors bean="${message}" field="title"/>
            </div>
        </g:hasErrors>
    </div>

    <div class="form-group">
        <label for="body">Message</label>
        <g:textArea
            name="body"
            value="${message?.body}"
            class="form-control ${hasErrors(bean: message, field: 'body', 'is-invalid')}"
            rows="4"
            required="required"/>
        <g:hasErrors bean="${message}" field="body">
            <div class="invalid-feedback">
                <g:renderErrors bean="${message}" field="body"/>
            </div>
        </g:hasErrors>
    </div>

    <button type="submit" class="btn btn-primary">
        <g:if test="${message?.id}">Update</g:if>
        <g:else>Create</g:else>
        Message
    </button>

    <g:if test="${message?.id}">
        <g:link controller="example" action="list" class="btn btn-secondary">Cancel</g:link>
    </g:if>
</g:form>

