package com.github.phoswald.fitbit.viewer.auth;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "auth-api")
@Path("/token")
interface OAuthClient {

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    TokenResponse exchangeCode(
            @FormParam("client_id") String clientId,
            @FormParam("client_secret") String clientSecret,
            @FormParam("grant_type") String grantType,
            @FormParam("code") String code,
            @FormParam("redirect_uri") String redirectUri);

    record TokenResponse(
            @JsonbProperty("id_token") String idToken,
            @JsonbProperty("access_token") String accessToken,
            @JsonbProperty("refresh_token") String refreshToken,
            @JsonbProperty("expires_in") Integer expiresIn
    ) { }
}
