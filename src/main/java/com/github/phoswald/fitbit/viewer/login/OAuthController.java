package com.github.phoswald.fitbit.viewer.login;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.net.URI;
import java.net.URLEncoder;
import java.util.Base64;

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

    private static final String AUTHORIZE_URL = "https://www.fitbit.com/oauth2/authorize";

    private static final Logger log = LoggerFactory.getLogger(OAuthController.class);

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

    @GET
    @Path("/login")
    public Response login() {
        log.debug("login: clientId={}, clientSecret={} (length), redirectUri={}",
                clientId, lengthOf(clientSecret), redirectUri);
        String url = AUTHORIZE_URL
                + "?response_type=code"
                + "&client_id=" + URLEncoder.encode(clientId, UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, UTF_8)
                + "&scope=profile+activity+heartrate";
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
            String fitbitAccessToken = tokenResponse.accessToken();
            return Response.seeOther(URI.create("pages/profile")).cookie(createCookie(fitbitAccessToken)).build();
        }
    }

    private static String createAuthorizationHeader(String clientId, String clientSecret) {
        return "Basic " + Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes(UTF_8));
    }

    private static NewCookie createCookie(String fitbitAccessToken) {
        return new NewCookie.Builder("fitbitAccessToken")
                .value(fitbitAccessToken)
                .path("/")
                .httpOnly(true)
                .build();
    }

    private static int lengthOf(String s) {
        return s == null ? 0 : s.length();
    }
}
