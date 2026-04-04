package com.github.phoswald.fitbit.viewer.login;

import java.util.Optional;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.json.bind.Jsonb;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequestScoped
public class SessionManager {

    public static final String COOKIE_NAME = "fibtitSession";

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Inject
    private Jsonb jsonb;

    @Inject
    @ConfigProperty(name = "app.fitbit.cookie.secret")
    private String secret;

    public String createAndSignCookie(SessionData session) {
        log.debug("createAndSignCookie(): {}", session);
        String payload = jsonb.toJson(session);
        return JwtUtil.createAndSign(payload, secret);
    }

    public Optional<SessionData> parseAndVerifyCookie(String cookieValue) {
        if (cookieValue == null) {
            return Optional.empty();
        }
        try {
            String payload = JwtUtil.parseAndVerify(cookieValue, secret);
            SessionData session = jsonb.fromJson(payload, SessionData.class);
            log.debug("parseAndVerifyCookie(): {}", session);
            return Optional.of(session);
        } catch(IllegalArgumentException e) {
            log.warn("parseAndVerifyCookie(): {}", e.getMessage());
            return Optional.empty();
        }
    }
}
