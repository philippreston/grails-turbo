package grails.turbo

import grails.testing.services.ServiceUnitTest
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Test specification for TurboStreamService.
 */
class TurboStreamServiceSpec extends Specification implements ServiceUnitTest<TurboStreamService> {

    @Unroll
    void "convenience #method builds a single turbo-stream"() {
        when:
        String result = service."$method"(*args)

        then:
        result.contains("action=\"${action}\"")
        snippets.every { result.contains(it) }

        where:
        method        | args                              | action   | snippets
        'append'      | ['messages', '<div>n</div>']      | 'append' | ['target="messages"', '<div>n</div>']
        'prepend'     | ['messages', '<div>n</div>']      | 'prepend'| ['target="messages"']
        'replace'     | ['m1', '<div>u</div>']            | 'replace'| ['target="m1"']
        'update'      | ['m1', 'inner']                   | 'update' | ['target="m1"', 'inner']
        'remove'      | ['m1']                            | 'remove' | ['target="m1"']
        'before'      | ['m1', '<hr/>']                   | 'before' | ['target="m1"', '<hr/>']
        'after'       | ['m1', '<hr/>']                   | 'after'  | ['target="m1"']
    }

    @Unroll
    void "#method uses targets for multi-select"() {
        when:
        String result = service."$method"(*args)

        then:
        result.contains('targets=".row"')
        actionFragments.every { result.contains(it) }

        where:
        method       | args                         | actionFragments
        'appendAll'  | ['.row', '<tr/>']           | ['action="append"', '<tr/>']
        'prependAll' | ['.row', '<tr/>']           | ['action="prepend"']
        'replaceAll' | ['.row', '<p/>']            | ['action="replace"']
        'updateAll'  | ['.row', 'x']                | ['action="update"']
        'beforeAll'  | ['.row', '<i/>']            | ['action="before"']
        'afterAll'   | ['.row', '<i/>']            | ['action="after"']
        'removeAll'  | ['.row']                     | ['action="remove"']
    }

    @Unroll
    void "replace and update morph overload morph=#morph"() {
        when:
        String rSingle = morph ? service.replace('a', '<p/>', true) : service.replace('a', '<p/>')
        String uSingle = morph ? service.update('a', 'c', true) : service.update('a', 'c')
        String rMulti = morph ? service.replaceAll('.x', '<p/>', true) : service.replaceAll('.x', '<p/>')
        String uMulti = morph ? service.updateAll('.x', 'c', true) : service.updateAll('.x', 'c')

        then:
        [rSingle, uSingle, rMulti, uMulti].every { (it.contains('method="morph"')) == morph }

        where:
        morph << [false, true]
    }

    @Unroll
    void "refresh #scenario"() {
        when:
        String result = service.refresh(opts)

        then:
        result.contains('action="refresh"')
        !result.contains('<template>')
        expected.every { result.contains(it) }

        where:
        scenario    | opts                                    | expected
        'default'   | [:]                                     | []
        'with opts' | [requestId: 'x', morph: true, scroll: 'reset'] | ['request-id="x"', 'method="morph"', 'scroll="reset"']
    }

    void "builder returns new TurboStreamBuilder"() {
        expect:
        service.builder() instanceof TurboStreamBuilder
    }

    void "builder allows chaining multiple actions"() {
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

    void "multiple stream actions can be combined"() {
        when:
        String result = service.builder()
            .append('list', '<li>Item 1</li>')
            .append('list', '<li>Item 2</li>')
            .update('total', '2')
            .build()

        then:
        result.count('<turbo-stream') == 3
    }

    @Unroll
    void "empty or null content still renders update (#desc)"() {
        when:
        String result = service.update('target', content)

        then:
        result.contains('action="update"')
        result.contains('target="target"')
        noExceptionThrown()

        where:
        desc         | content
        'empty'      | ''
        'null'       | null
    }

    void "HTML content inside template is preserved"() {
        when:
        String result = service.append('messages', '<div class="message">Text & More</div>')

        then:
        result.contains('<div class="message">Text & More</div>')
    }

    void "target IDs with hyphens and underscores"() {
        when:
        String result = service.update('message-id_123', 'content')

        then:
        result.contains('target="message-id_123"')
    }
}
