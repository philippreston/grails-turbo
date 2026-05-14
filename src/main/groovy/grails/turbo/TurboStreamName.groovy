package grails.turbo

import java.nio.charset.StandardCharsets
import java.util.Base64

/**
 * Canonical Turbo stream names ({@code streamable:streamable:...}), mirroring
 * {@code Turbo::Streams::StreamName#stream_name_from} in turbo-rails.
 */
final class TurboStreamName {

    private TurboStreamName() {}

    /**
     * Flatten taglib/service broadcast arguments into a list (array, {@link Iterable} except {@link Map}, or single value).
     */
    static List<Object> normalizeStreamables(Object raw) {
        if (raw == null) {
            return []
        }
        if (raw instanceof Object[]) {
            return (raw as Object[]).toList()
        }
        if (raw instanceof Iterable && !(raw instanceof Map)) {
            List<Object> list = []
            for (Object o : (Iterable<?>) raw) {
                list << o
            }
            return list
        }
        [raw]
    }

    /**
     * Build a colon-separated stream name from a list/array of streamables (may be nested lists for composite segments).
     *
     * @param streamables flattened list (e.g. account + {@code :entries})
     * @param globalIdApp segment used in {@code gid://app/Model/id} for persisted objects (config {@code globalIdApp})
     */
    static String fromIterable(Iterable<?> streamables, String globalIdApp) {
        if (streamables == null) {
            throw new IllegalArgumentException('streamables must not be null')
        }
        List<String> parts = []
        for (Object item : streamables) {
            if (item != null && item != '') {
                parts << segment(item, globalIdApp ?: 'application')
            }
        }
        if (parts.isEmpty()) {
            throw new IllegalArgumentException('streamables cannot be blank')
        }
        parts.join(':')
    }

    private static String segment(Object streamable, String globalIdApp) {
        if (streamable instanceof CharSequence) {
            return streamable.toString().trim()
        }
        if (streamable instanceof Number || streamable instanceof Boolean) {
            return streamable.toString()
        }
        if (streamable instanceof Map) {
            throw new IllegalArgumentException('Map is not a valid streamable; use String or domain instance')
        }
        if (streamable.getClass().isArray()) {
            return fromIterable((streamable as Object[]).toList(), globalIdApp)
        }
        if (streamable instanceof Iterable) {
            return fromIterable((Iterable<?>) streamable, globalIdApp)
        }
        return toGidParam(streamable, globalIdApp)
    }

    /**
     * GlobalID-style {@code to_gid_param}: URL-safe Base64 of {@code gid://app/SimpleName/id}.
     */
    static String toGidParam(Object domainInstance, String globalIdApp) {
        if (domainInstance == null) {
            throw new IllegalArgumentException('domain streamable must not be null')
        }
        if (!domainInstance.hasProperty('id')) {
            throw new IllegalArgumentException("Streamable ${domainInstance.getClass().simpleName} has no id property")
        }
        Object id = domainInstance.getProperty('id')
        if (id == null) {
            throw new IllegalArgumentException("Streamable ${domainInstance.getClass().simpleName} must be persisted (id not null) for gid stream segment")
        }
        String model = domainInstance.getClass().simpleName
        String gid = "gid://${globalIdApp}/${model}/${id}"
        return Base64.getUrlEncoder().withoutPadding().encodeToString(gid.getBytes(StandardCharsets.UTF_8))
    }
}
