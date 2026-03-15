package com.github.phoswald.fitbit.viewer;

import java.net.URI;
import java.net.URLEncoder;

import static java.nio.charset.StandardCharsets.UTF_8;
import static com.github.phoswald.fitbit.viewer.FitbitApiConstants.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

@RequestScoped
@Path("/oauth")
public class FitbitOAuthController {

    private static final Logger log = LoggerFactory.getLogger(FitbitOAuthController.class);

    @Inject
    private FitbitConfig config;

    @Inject
    private FitbitApiClient apiClient;

    @GET
    @Path("/login")
    public Response login() {
        log.debug("login: clientId={}, clientSecret={} chars, redirectUri={}",
                config.getClientId(), config.getClientSecret() == null ? 0 : config.getClientSecret().length(), config.getRedirectUri());
        String url = AUTHORIZE_URL
                + "?response_type=code"
                + "&client_id=" + URLEncoder.encode(config.getClientId(), UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(config.getRedirectUri(), UTF_8)
                + "&scope=profile";
        return Response.seeOther(URI.create(url)).build();
    }

    @GET
    @Path("/callback")
    public Response callback(@QueryParam("code") String code, @QueryParam("error") String error) {
        log.debug("callback: code={}, error={}", code, error);
        if (error != null) {
            // TODO: add error handling to view
            return Response.seeOther(URI.create("pages/dashboard?error=" + URLEncoder.encode(error, UTF_8))).build();
        }
        String accessToken = apiClient.exchangeCode(code);
        NewCookie cookie = new NewCookie.Builder("fitbit_access_token")
                .value(accessToken)
                .path("/")
                .httpOnly(true)
                .build();
        return Response.seeOther(URI.create("pages/dashboard")).cookie(cookie).build();
    }
}
