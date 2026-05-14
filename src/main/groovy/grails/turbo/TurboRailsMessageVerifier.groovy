package grails.turbo

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.nio.charset.StandardCharsets
import java.util.Base64

/**
 * Rails-compatible signed message for stream names (ActiveSupport::MessageVerifier with
 * digest SHA256, JSON serializer, non-URL-safe Base64, no metadata envelope).
 */
final class TurboRailsMessageVerifier {

    private static final String HMAC_SHA256 = 'HmacSHA256'
    private static final String SEPARATOR = '--'

    private final byte[] secret

    TurboRailsMessageVerifier(String secretKey) {
        if (!secretKey) {
            throw new IllegalArgumentException('stream signing secret must not be null or empty')
        }
        this.secret = secretKey.getBytes(StandardCharsets.UTF_8)
    }

    /**
     * Sign a canonical stream name string (e.g. {@code "account:5:entries"}).
     */
    String generate(String streamName) {
        if (streamName == null) {
            throw new IllegalArgumentException('streamName must not be null')
        }
        String serialized = JsonOutput.toJson(streamName)
        byte[] serializedBytes = serialized.getBytes(StandardCharsets.UTF_8)
        String encoded = Base64.encoder.encodeToString(serializedBytes)
        return encoded + SEPARATOR + computeDigest(encoded)
    }

    boolean validMessage(String signedMessage) {
        return verified(signedMessage) != null
    }

    /**
     * @return decoded canonical stream name, or {@code null} if invalid/tampered
     */
    String verified(String signedMessage) {
        if (!signedMessage || !signedMessage.contains(SEPARATOR)) {
            return null
        }
        int idx = signedMessage.lastIndexOf(SEPARATOR)
        if (idx < 1) {
            return null
        }
        String encoded = signedMessage.substring(0, idx)
        String digestHex = signedMessage.substring(idx + SEPARATOR.length())
        if (!encoded || digestHex.length() != 64) {
            return null
        }
        if (!constantTimeEquals(computeDigest(encoded), digestHex)) {
            return null
        }
        try {
            byte[] jsonBytes = Base64.decoder.decode(encoded)
            String json = new String(jsonBytes, StandardCharsets.UTF_8)
            return new JsonSlurper().parseText(json) as String
        } catch (ignored) {
            return null
        }
    }

    private String computeDigest(String encoded) {
        Mac mac = Mac.getInstance(HMAC_SHA256)
        mac.init(new SecretKeySpec(secret, HMAC_SHA256))
        byte[] sig = mac.doFinal(encoded.getBytes(StandardCharsets.US_ASCII))
        StringBuilder hex = new StringBuilder(sig.length * 2)
        for (byte b : sig) {
            hex.append(String.format('%02x', b & 0xff))
        }
        hex.toString()
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) {
            return false
        }
        int result = 0
        for (int i = 0; i < a.length(); i++) {
            result |= (((int) a.charAt(i)) ^ ((int) b.charAt(i)))
        }
        result == 0
    }
}
