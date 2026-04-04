package com.github.phoswald.fitbit.viewer.login;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import jakarta.json.Json;

class JwtUtil {

    private static final String JWT_HEADER = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
    private static final Pattern JWT_PATTERN = Pattern.compile("([A-Za-z0-9_-]+)\\.([A-Za-z0-9_-]+)\\.([A-Za-z0-9_-]+)");

    static String createAndSign(String payload, String secret) {
        String encodedHeader = encodeBase64Url(JWT_HEADER);
        String encodedPayload = encodeBase64Url(payload);
        String encodedSignature = encodeBase64Url(signWithHMAC(
                encodedHeader + "." + encodedPayload, secret));
        return encodedHeader + "." + encodedPayload + "." + encodedSignature;
    }

    static String parseAndVerify(String token, String secret) {
        JwtToken parsed = parse(token);
        byte[] expectedSignature = signWithHMAC(
                parsed.encodedHeader() + "." + parsed.encodedPayload(), secret);
        if (!MessageDigest.isEqual(parsed.signature(), expectedSignature)) {
            throw new IllegalArgumentException("HMAC signature mismatch");
        }
        return parsed.payload();
    }

    private static JwtToken parse(String token) {
        Matcher matcher = JWT_PATTERN.matcher(token);
        if(matcher.matches()) {
            String encodedHeader = matcher.group(1);
            String encodedPayload = matcher.group(2);
            String encodedSignature = matcher.group(3);
            return new JwtToken(encodedHeader, encodedPayload,
                    decodeBase64UrlToString(encodedPayload), decodeBase64Url(encodedSignature));
        } else {
            throw new IllegalArgumentException("JWT token format mismatch");
        }
    }

    static String getSubject(String token) {
        JwtToken parsed = parse(token);
        try (var reader = Json.createReader(new StringReader(parsed.payload()))) {
            return reader.readObject().getString("sub");
        }
    }

    private static byte[] signWithHMAC(String string, String secret) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(secret);
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(keyBytes, "HmacSHA256"));
            return mac.doFinal(string.getBytes(UTF_8));
        } catch (Exception e) {
            throw new IllegalArgumentException("HMAC computation failed: " + e.getMessage());
        }
    }

    private static String encodeBase64Url(String string) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(string.getBytes(UTF_8));
    }

    private static String encodeBase64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String decodeBase64UrlToString(String string) {
        return new String(Base64.getUrlDecoder().decode(string), UTF_8);
    }

    private static byte[] decodeBase64Url(String string) {
        return Base64.getUrlDecoder().decode(string);
    }

    private record JwtToken(
            String encodedHeader,
            String encodedPayload,
            String payload,
            byte[] signature
    ) { }
}
