package grails.turbo

import spock.lang.Specification
import spock.lang.Unroll

class TurboBroadcastableSpec extends Specification {

    static class DummyBean implements TurboBroadcastable {
        Long id
        List<Object> streams
        TurboStreamService svc

        List<Object> turboBroadcastStreamables() {
            streams ?: [this]
        }

        TurboStreamService lookupTurboStreamService() {
            svc
        }
    }

    void 'turboBroadcastAppend uses turboBroadcastStreamables'() {
        given:
        TurboStreamService svc = Mock()
        DummyBean bean = new DummyBean(streams: ['parent', 'feed'], svc: svc)

        when:
        bean.turboBroadcastAppend('slot', '<a/>')

        then:
        1 * svc.broadcastAppendTo(['parent', 'feed'], 'slot', '<a/>')
    }

    void 'default streamables is composed instance'() {
        given:
        TurboStreamService svc = Mock()
        DummyBean bean = new DummyBean(id: 9L, svc: svc)

        when:
        bean.turboBroadcastRefresh()

        then:
        1 * svc.broadcastRefreshTo([bean], [:])
    }

    @Unroll
    void 'turboBroadcastReplace morph=#morph delegates'() {
        given:
        TurboStreamService svc = Mock()
        DummyBean bean = new DummyBean(svc: svc)

        when:
        bean.turboBroadcastReplace('t', '<p/>', morph)

        then:
        1 * svc.broadcastReplaceTo([bean], 't', '<p/>', morph)

        where:
        morph << [false, true]
    }

    @Unroll
    void 'turboBroadcastAll targets (#method) delegates'() {
        given:
        TurboStreamService svc = Mock()
        DummyBean bean = new DummyBean(svc: svc)

        when:
        bean."$method"('.x', '<i/>')

        then:
        1 * svc."$svcMethod"([bean], '.x', '<i/>')

        where:
        method                    | svcMethod
        'turboBroadcastAppendAll' | 'broadcastAppendAllTo'
        'turboBroadcastBeforeAll' | 'broadcastBeforeAllTo'
    }

    void 'turboBroadcastRemoveAll delegates'() {
        given:
        TurboStreamService svc = Mock()
        DummyBean bean = new DummyBean(svc: svc)

        when:
        bean.turboBroadcastRemoveAll('.x')

        then:
        1 * svc.broadcastRemoveAllTo([bean], '.x')
    }
}
