package com.github.phoswald.fitbit.viewer.auth;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.net.URI;
import java.net.URLEncoder;
import java.time.Instant;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequestScoped
@Path("/oauth")
public class OAuthController {

    private static final String AUTHORIZE_URL = "https://accounts.google.com/o/oauth2/v2/auth";

    private static final String SCOPES = String.join(" ",
            "openid", "profile", "email",
            "https://www.googleapis.com/auth/googlehealth.activity_and_fitness.readonly",
            "https://www.googleapis.com/auth/googlehealth.health_metrics_and_measurements.readonly",
            "https://www.googleapis.com/auth/googlehealth.location.readonly",
            "https://www.googleapis.com/auth/googlehealth.profile.readonly",
            "https://www.googleapis.com/auth/googlehealth.sleep.readonly");

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Inject
    @ConfigProperty(name = "app.fitbit.client-id")
    private String clientId;

    @Inject
    @ConfigProperty(name = "app.fitbit.client-secret")
    private String clientSecret;

    @Inject
    @ConfigProperty(name = "app.fitbit.redirect-uri")
    private String redirectUri;

    @Inject
    @RestClient
    private OAuthClient tokenClient;

    @Inject
    private SessionManager sessionManager;

    @GET
    @Path("/login")
    public Response login() {
        log.info("login: clientId={}, clientSecret={} (length), redirectUri={}",
                clientId, lengthOf(clientSecret), redirectUri);
        String url = AUTHORIZE_URL
                + "?response_type=code"
                + "&client_id=" + URLEncoder.encode(clientId, UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, UTF_8)
                + "&scope=" + URLEncoder.encode(SCOPES, UTF_8);
        return Response.seeOther(URI.create(url)).build();
    }

    @GET
    @Path("/callback")
    public Response callback(@QueryParam("code") String code, @QueryParam("error") String error) {
        if (error != null) {
            log.warn("callback(): error={}", error);
            return Response.seeOther(URI.create("pages/profile?errorMessage=" + URLEncoder.encode(error, UTF_8))).build();
        } else {
            log.info("callback(): code={} (length)", lengthOf(code));

            var tokenResponse = tokenClient.exchangeCode(clientId, clientSecret, "authorization_code", code, redirectUri);
            log.info("callback(): exchanged id_token={} (length) access_token={} (length), refresh_token={} (length), expires_in={}",
                    lengthOf(tokenResponse.idToken()), lengthOf(tokenResponse.accessToken()),
                    lengthOf(tokenResponse.refreshToken()), tokenResponse.expiresIn());

            SessionData sessionData = new SessionDataBuilder()
                    .accessToken(tokenResponse.accessToken())
                    .expiresAt(Instant.now().plusSeconds(tokenResponse.expiresIn()).toEpochMilli())
                    .userId(JwtUtil.getEMail(tokenResponse.idToken()))
                    .build();
            return Response
                    .seeOther(URI.create("pages/profile"))
                    .cookie(createCookie(sessionManager.createAndSignCookie(sessionData)))
                    .build();
        }
    }

    private static NewCookie createCookie(String sessionCookie) {
        return new NewCookie.Builder(SessionManager.COOKIE_NAME)
                .value(sessionCookie)
                .path("/")
                .httpOnly(true)
                .build();
    }

    private static int lengthOf(String s) {
        return s == null ? 0 : s.length();
    }
}
