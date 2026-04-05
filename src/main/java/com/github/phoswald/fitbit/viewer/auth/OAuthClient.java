package com.github.phoswald.fitbit.viewer.auth;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "fitbit-api")
@Path("/oauth2/token")
interface OAuthClient {

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    TokenResponse exchangeCode(
            @HeaderParam("Authorization") String authorizationHeader,
            @FormParam("grant_type") String grantType,
            @FormParam("code") String code,
            @FormParam("redirect_uri") String redirectUri);

    record TokenResponse(
            @JsonbProperty("access_token") String accessToken,
            @JsonbProperty("refresh_token") String refreshToken,
            @JsonbProperty("expires_in") Integer expiresIn
    ) { }
}
