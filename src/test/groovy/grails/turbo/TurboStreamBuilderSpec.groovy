package grails.turbo

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Test specification for TurboStreamBuilder class.
 */
class TurboStreamBuilderSpec extends Specification {

    @Shared
    private Map<String, Closure<TurboStreamBuilder>> templateSingleTarget = [
        append : { TurboStreamBuilder b -> b.append('id', '<i/>') },
        prepend: { TurboStreamBuilder b -> b.prepend('id', '<i/>') },
        replace: { TurboStreamBuilder b -> b.replace('id', '<i/>') },
        update : { TurboStreamBuilder b -> b.update('id', '<i/>') },
        before : { TurboStreamBuilder b -> b.before('id', '<i/>') },
        after  : { TurboStreamBuilder b -> b.after('id', '<i/>') },
    ]

    @Shared
    private Map<String, Closure<TurboStreamBuilder>> templateMultiTarget = [
        appendAll: { TurboStreamBuilder b -> b.appendAll('.r', '<tr/>') },
        prependAll: { TurboStreamBuilder b -> b.prependAll('.r', '<tr/>') },
        replaceAll: { TurboStreamBuilder b -> b.replaceAll('.r', '<tr/>') },
        updateAll: { TurboStreamBuilder b -> b.updateAll('.r', '<tr/>') },
        beforeAll: { TurboStreamBuilder b -> b.beforeAll('.r', '<tr/>') },
        afterAll: { TurboStreamBuilder b -> b.afterAll('.r', '<tr/>') },
    ]

    @Unroll
    void "template action #action uses target=\"id\" and wraps template"() {
        when:
        TurboStreamBuilder b = new TurboStreamBuilder()
        templateSingleTarget[action].call(b)
        String result = b.build()

        then:
        result.contains("action=\"${action}\"")
        result.contains('target="id"')
        result.contains('<template>')
        result.contains('<i/>')
        result.contains('</template>')

        where:
        action << templateSingleTarget.keySet()
    }

    @Unroll
    void "multi-target method #method uses targets and template"() {
        when:
        TurboStreamBuilder b = new TurboStreamBuilder()
        templateMultiTarget[method].call(b)
        String result = b.build()

        then:
        String turboAction = method.replaceAll('All$', '')
        result.contains("action=\"${turboAction}\"")
        result.contains('targets=".r"')
        result.contains('<tr/>')

        where:
        method << templateMultiTarget.keySet()
    }

    @Unroll
    void "replace and replaceAll respect morph=#morph"() {
        when:
        String single = new TurboStreamBuilder().replace('a', '<p/>', morph).build()
        String multi = new TurboStreamBuilder().replaceAll('.x', '<p/>', morph).build()

        then:
        single.contains('target="a"') && (single.contains('method="morph"') == morph)
        multi.contains('targets=".x"') && (multi.contains('method="morph"') == morph)

        where:
        morph << [false, true]
    }

    @Unroll
    void "update and updateAll respect morph=#morph"() {
        when:
        String single = new TurboStreamBuilder().update('a', 'c', morph).build()
        String multi = new TurboStreamBuilder().updateAll('.x', 'c', morph).build()

        then:
        single.contains('target="a"') && (single.contains('method="morph"') == morph)
        multi.contains('targets=".x"') && (multi.contains('method="morph"') == morph)

        where:
        morph << [false, true]
    }

    @Unroll
    void "remove #desc emits no template"() {
        when:
        TurboStreamBuilder b = new TurboStreamBuilder()
        if (useTargets) {
            b.removeAll('.gone')
        } else {
            b.remove('gone')
        }
        String result = b.build()

        then:
        result.contains('action="remove"')
        expectedSubstrings.every { result.contains(it) }
        !result.contains('<template>')

        where:
        desc           | useTargets | expectedSubstrings
        'single target' | false      | ['target="gone"']
        'multi targets' | true       | ['targets=".gone"']
    }

    @Unroll
    void "stream low-level delegates to target vs targets (#useTargetsAttribute)"() {
        when:
        String result = new TurboStreamBuilder()
            .stream('append', 'segment', '<b/>', useTargetsAttribute)
            .build()

        then:
        if (useTargetsAttribute) {
            assert result.contains('targets="segment"')
            assert !result.contains('target="segment"')
        } else {
            assert result.contains('target="segment"')
            assert !result.contains('targets="segment"')
        }

        where:
        useTargetsAttribute << [false, true]
    }

    @Unroll
    void "refresh #scenario"() {
        when:
        String result = new TurboStreamBuilder().refresh(opts).build()

        then:
        result.startsWith('<turbo-stream action="refresh"')
        result.endsWith('></turbo-stream>')
        !result.contains('<template>')
        fragments.every { result.contains(it) }

        where:
        scenario              | opts                                                       | fragments
        'bare'                | [:]                                                        | []
        'requestId camelCase' | [requestId: 'abc']                                         | ['request-id="abc"']
        'request-id hyphen'   | [('request-id'): 'z9']                                     | ['request-id="z9"']
        'scroll'              | [scroll: 'preserve']                                       | ['scroll="preserve"']
        'morph'               | [morph: true]                                              | ['method="morph"']
        'morph scroll rq'     | [requestId: 'r1', morph: true, scroll: 'reset']           | ['request-id="r1"', 'method="morph"', 'scroll="reset"']
        'raw method'          | [morph: false, method: 'advance']                          | ['method="advance"']
        'morph truthy string' | [morph: 'yes']                                             | ['method="morph"']
    }

    void "target id is escaped in attribute"() {
        when:
        String result = new TurboStreamBuilder().update('bad<id>', 'c').build()

        then:
        result.contains('&lt;') && result.contains('target=')
        !result.contains('target="bad<id>"')
    }

    void "chaining multiple actions"() {
        when:
        String result = new TurboStreamBuilder()
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

    void "toString returns built string"() {
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
