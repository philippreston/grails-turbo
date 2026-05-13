package grails.turbo

import spock.lang.Specification

/**
 * Test specification for TurboStreamBuilder class.
 */
class TurboStreamBuilderSpec extends Specification {

    void "test appendAll uses targets"() {
        given:
        TurboStreamBuilder builder = new TurboStreamBuilder()

        when:
        String result = builder.appendAll('.row', '<div>x</div>').build()

        then:
        result.contains('action="append"')
        result.contains('targets=".row"')
    }

    void "test replace morph"() {
        when:
        String result = new TurboStreamBuilder().replace('x', '<p>y</p>', true).build()

        then:
        result.contains('method="morph"')
        result.contains('target="x"')
    }

    void "test target id is escaped in attribute"() {
        when:
        String result = new TurboStreamBuilder().update('bad<id>', 'c').build()

        then:
        result.contains('&lt;') && result.contains('target=')
        !result.contains('target="bad<id>"')
    }

    void "test append action"() {
        given:
        TurboStreamBuilder builder = new TurboStreamBuilder()

        when:
        String result = builder.append("messages", "<div>New message</div>").build()

        then:
        result.contains('action="append"')
        result.contains('target="messages"')
        result.contains('<template>')
        result.contains('<div>New message</div>')
        result.contains('</template>')
    }

    void "test prepend action"() {
        given:
        TurboStreamBuilder builder = new TurboStreamBuilder()

        when:
        String result = builder.prepend("messages", "<div>New message</div>").build()

        then:
        result.contains('action="prepend"')
        result.contains('target="messages"')
        result.contains('<div>New message</div>')
    }

    void "test replace action"() {
        given:
        TurboStreamBuilder builder = new TurboStreamBuilder()

        when:
        String result = builder.replace("message_1", "<div>Updated message</div>").build()

        then:
        result.contains('action="replace"')
        result.contains('target="message_1"')
        result.contains('<div>Updated message</div>')
    }

    void "test update action"() {
        given:
        TurboStreamBuilder builder = new TurboStreamBuilder()

        when:
        String result = builder.update("message_1", "Updated content").build()

        then:
        result.contains('action="update"')
        result.contains('target="message_1"')
        result.contains('Updated content')
    }

    void "test remove action"() {
        given:
        TurboStreamBuilder builder = new TurboStreamBuilder()

        when:
        String result = builder.remove("message_1").build()

        then:
        result.contains('action="remove"')
        result.contains('target="message_1"')
        !result.contains('<template>')
    }

    void "test before action"() {
        given:
        TurboStreamBuilder builder = new TurboStreamBuilder()

        when:
        String result = builder.before("message_1", "<div>Before content</div>").build()

        then:
        result.contains('action="before"')
        result.contains('target="message_1"')
        result.contains('<div>Before content</div>')
    }

    void "test after action"() {
        given:
        TurboStreamBuilder builder = new TurboStreamBuilder()

        when:
        String result = builder.after("message_1", "<div>After content</div>").build()

        then:
        result.contains('action="after"')
        result.contains('target="message_1"')
        result.contains('<div>After content</div>')
    }

    void "test refresh action"() {
        given:
        TurboStreamBuilder builder = new TurboStreamBuilder()

        when:
        String result = builder.refresh().build()

        then:
        result.contains('action="refresh"')
        !result.contains('target=')
    }

    void "test chaining multiple actions"() {
        given:
        TurboStreamBuilder builder = new TurboStreamBuilder()

        when:
        String result = builder
            .append("messages", "<div>New message</div>")
            .update("count", "42")
            .remove("old_message")
            .build()

        then:
        result.contains('action="append"')
        result.contains('action="update"')
        result.contains('action="remove"')
        result.count("<turbo-stream") == 3
    }

    void "test toString returns built string"() {
        given:
        TurboStreamBuilder builder = new TurboStreamBuilder()
        builder.append("test", "content")

        when:
        String result = builder.toString()

        then:
        result.contains("turbo-stream")
        result.contains("content")
    }
}

