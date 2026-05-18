package grails.turbo

import spock.lang.Specification

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
}
