<!doctype html>
<html lang="en" class="no-js">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
    <title>
        <g:layoutTitle default="Grails Turbo"/>
    </title>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <asset:link rel="icon" href="favicon.ico" type="image/x-ico"/>

    <asset:stylesheet src="application.css"/>

    <!-- Turbo JavaScript - loaded via tag library with configuration support -->
    <turbo:includeTurbo/>

    <g:layoutHead/>
</head>

<body>

<nav class="navbar navbar-expand-lg navbar-dark navbar-static-top" role="navigation">
    <div class="container-fluid">
        <a class="navbar-brand" href="/#" data-turbo-frame="_top">
            <asset:image src="grails.svg" alt="Grails Logo"/>
        </a>
        <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarContent" aria-controls="navbarContent" aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>

        <div class="collapse navbar-collapse" aria-expanded="false" style="height: 0.8px;" id="navbarContent">
            <ul class="nav navbar-nav ml-auto">
                <g:pageProperty name="page.nav"/>
            </ul>
        </div>
    </div>
</nav>

<g:layoutBody/>

<div class="footer" role="contentinfo">
    <div class="container-fluid">
        <div class="row">
            <div class="col">
                <a href="https://turbo.hotwired.dev" target="_blank">
                    <strong class="centered">Hotwired Turbo</strong>
                </a>
                <p>This application is powered by Hotwired Turbo for fast, modern page navigation.</p>
            </div>
            <div class="col">
                <a href="https://guides.grails.org" target="_blank">
                    <asset:image src="advancedgrails.svg" alt="Grails Guides" class="float-left"/>
                </a>
                <strong class="centered"><a href="https://guides.grails.org" target="_blank">Grails Guides</a></strong>
                <p>Building your first Grails app? Check out the <a href="https://guides.grails.org" target="_blank">Grails Guides</a>.</p>
            </div>
            <div class="col">
                <a href="https://docs.grails.org" target="_blank">
                    <asset:image src="documentation.svg" alt="Grails Documentation" class="float-left"/>
                </a>
                <strong class="centered"><a href="https://docs.grails.org" target="_blank">Documentation</a></strong>
                <p>In-depth documentation for all Grails features in the <a href="https://docs.grails.org" target="_blank">User Guide</a>.</p>
            </div>
        </div>
    </div>
</div>

<div id="spinner" class="spinner" style="display:none;">
    <g:message code="spinner.alt" default="Loading&hellip;"/>
</div>

<asset:javascript src="application.js"/>

</body>
</html>

