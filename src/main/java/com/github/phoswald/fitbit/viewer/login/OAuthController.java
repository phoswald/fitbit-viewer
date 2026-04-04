package com.github.phoswald.fitbit.viewer.login;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URLEncoder;
import java.time.Instant;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.ext.web.Session;

@RequestScoped
@Path("/oauth")
public class OAuthController {

    private static final String AUTHORIZE_URL = "https://www.fitbit.com/oauth2/authorize";

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
        log.debug("login: clientId={}, clientSecret={} (length), redirectUri={}",
                clientId, lengthOf(clientSecret), redirectUri);
        String url = AUTHORIZE_URL
                + "?response_type=code"
                + "&client_id=" + URLEncoder.encode(clientId, UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, UTF_8)
                + "&scope=profile+activity+heartrate+cardio_fitness";
        return Response.seeOther(URI.create(url)).build();
    }

    @GET
    @Path("/callback")
    public Response callback(@QueryParam("code") String code, @QueryParam("error") String error) {
        if (error != null) {
            log.warn("callback: error={}", error);
            return Response.seeOther(URI.create("pages/profile?errorMessage=" + URLEncoder.encode(error, UTF_8))).build();
        } else {
            log.debug("callback: code={} (length)", lengthOf(code));
            var tokenResponse = tokenClient.exchangeCode(
                    createAuthorizationHeader(clientId, clientSecret),
                    "authorization_code", code, redirectUri);

            log.debug("exchanged: access_token={} (length), refresh_token={} (length), expires_in={}",
                    lengthOf(tokenResponse.accessToken()), lengthOf(tokenResponse.refreshToken()), tokenResponse.expiresIn());

            String userId = JwtUtil.getSubject(tokenResponse.accessToken());
            log.debug("exracted: userId={}", userId);

            SessionData sessionData = new SessionDataBuilder()
                    .accessToken(tokenResponse.accessToken())
                    .expiresAt(Instant.now().plusSeconds(tokenResponse.expiresIn()).toEpochMilli())
                    .userId(userId)
                    .build();
            return Response
                    .seeOther(URI.create("pages/profile"))
                    .cookie(createCookie(sessionManager.createAndSignCookie(sessionData)))
                    .build();
        }
    }

    private static String createAuthorizationHeader(String clientId, String clientSecret) {
        return "Basic " + Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes(UTF_8));
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
