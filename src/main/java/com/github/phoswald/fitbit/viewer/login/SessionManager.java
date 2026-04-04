package com.github.phoswald.fitbit.viewer.login;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.StringReader;
import java.io.StringWriter;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Optional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequestScoped
public class SessionManager {

    public static final String COOKIE_NAME = "fibtitSession";

    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Inject
    @ConfigProperty(name = "app.fitbit.cookie.secret")
    private String secret;

    public String createAndSignCookie(SessionData data) {
        String payload = ENCODER.encodeToString(toJson(data).getBytes(UTF_8));
        String sig = ENCODER.encodeToString(hmac(payload));
        return payload + "." + sig;
    }

    public Optional<SessionData> parseAndverify(String cookieValue) {
        if (cookieValue == null) {
            return Optional.empty();
        }
        int dot = cookieValue.lastIndexOf('.');
        if (dot < 0) {
            return Optional.empty();
        }
        String payload = cookieValue.substring(0, dot);
        String sig = cookieValue.substring(dot + 1);
        byte[] expected = hmac(payload);
        byte[] actual;
        try {
            actual = DECODER.decode(sig);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
        if (!MessageDigest.isEqual(expected, actual)) {
            log.warn("verify: cookie signature mismatch");
            return Optional.empty();
        }
        try {
            String json = new String(DECODER.decode(payload), UTF_8);
            return Optional.of(fromJson(json));
        } catch (Exception e) {
            log.warn("verify: cookie payload parsing failed", e);
            return Optional.empty();
        }
    }

    private byte[] hmac(String data) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(secret);
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(keyBytes, "HmacSHA256"));
            return mac.doFinal(data.getBytes(UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("HMAC computation failed", e);
        }
    }

    private static String toJson(SessionData session) {
        StringWriter sw = new StringWriter();
        try (var w = Json.createWriter(sw)) {
            w.writeObject(Json.createObjectBuilder()
                    .add("accessToken", session.accessToken())
                    .add("expiresAt", session.expiresAt().toString())
                    .add("userId", session.userId())
                    .build());
        }
        return sw.toString();
    }

    private static SessionData fromJson(String json) {
        try (var r = Json.createReader(new StringReader(json))) {
            JsonObject obj = r.readObject();
            return new SessionDataBuilder()
                    .accessToken(obj.getString("accessToken"))
                    .expiresAt(Long.valueOf(obj.getString("expiresAt")))
                    .userId(obj.getString("userId"))
                    .build();
        }
    }
}
