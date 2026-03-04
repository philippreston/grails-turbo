package grails.turbo

import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

/**
 * Test specification for TurboStreamService.
 */
class TurboStreamServiceSpec extends Specification implements ServiceUnitTest<TurboStreamService> {

    void "test builder returns new TurboStreamBuilder"() {
        when:
        TurboStreamBuilder builder = service.builder()

        then:
        builder != null
        builder instanceof TurboStreamBuilder
    }

    void "test append creates append turbo stream"() {
        when:
        String result = service.append('messages', '<div>New message</div>')

        then:
        result.contains('<turbo-stream action="append" target="messages">')
        result.contains('<template>')
        result.contains('<div>New message</div>')
        result.contains('</template>')
        result.contains('</turbo-stream>')
    }

    void "test prepend creates prepend turbo stream"() {
        when:
        String result = service.prepend('messages', '<div>New message</div>')

        then:
        result.contains('action="prepend"')
        result.contains('target="messages"')
        result.contains('<div>New message</div>')
    }

    void "test replace creates replace turbo stream"() {
        when:
        String result = service.replace('message_1', '<div>Updated message</div>')

        then:
        result.contains('action="replace"')
        result.contains('target="message_1"')
        result.contains('<div>Updated message</div>')
    }

    void "test update creates update turbo stream"() {
        when:
        String result = service.update('message_1', 'Updated content')

        then:
        result.contains('action="update"')
        result.contains('target="message_1"')
        result.contains('Updated content')
    }

    void "test remove creates remove turbo stream"() {
        when:
        String result = service.remove('message_1')

        then:
        result.contains('action="remove"')
        result.contains('target="message_1"')
        !result.contains('<template>')
    }

    void "test before creates before turbo stream"() {
        when:
        String result = service.before('message_1', '<div>Before content</div>')

        then:
        result.contains('action="before"')
        result.contains('target="message_1"')
        result.contains('<div>Before content</div>')
    }

    void "test after creates after turbo stream"() {
        when:
        String result = service.after('message_1', '<div>After content</div>')

        then:
        result.contains('action="after"')
        result.contains('target="message_1"')
        result.contains('<div>After content</div>')
    }

    void "test refresh creates refresh turbo stream"() {
        when:
        String result = service.refresh()

        then:
        result.contains('action="refresh"')
        !result.contains('target=')
    }

    void "test builder allows chaining multiple actions"() {
        when:
        String result = service.builder()
            .append('messages', '<div>New</div>')
            .update('count', '10')
            .remove('old')
            .build()

        then:
        result.contains('action="append"')
        result.contains('action="update"')
        result.contains('action="remove"')
    }

    void "test service methods return valid turbo stream HTML"() {
        when:
        String result = service.append('test', 'content')

        then:
        result.startsWith('<turbo-stream')
        result.endsWith('</turbo-stream>')
    }

    void "test multiple stream actions can be combined"() {
        given:
        def builder = service.builder()

        when:
        String result = builder
            .append('list', '<li>Item 1</li>')
            .append('list', '<li>Item 2</li>')
            .update('total', '2')
            .build()

        then:
        result.count('<turbo-stream') == 3
        result.contains('<li>Item 1</li>')
        result.contains('<li>Item 2</li>')
    }

    void "test empty content is handled correctly"() {
        when:
        String result = service.update('target', '')

        then:
        result.contains('action="update"')
        result.contains('target="target"')
    }

    void "test null content is handled gracefully"() {
        when:
        String result = service.update('target', null)

        then:
        result.contains('action="update"')
        result.contains('target="target"')
        noExceptionThrown()
    }

    void "test HTML content is not escaped"() {
        when:
        String result = service.append('messages', '<div class="message">Text & More</div>')

        then:
        result.contains('<div class="message">Text & More</div>')
    }

    void "test target IDs with special characters"() {
        when:
        String result = service.update('message-id_123', 'content')

        then:
        result.contains('target="message-id_123"')
    }
}

