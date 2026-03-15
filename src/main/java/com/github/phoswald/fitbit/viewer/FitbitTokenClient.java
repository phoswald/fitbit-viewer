package com.github.phoswald.fitbit.viewer;

import com.fasterxml.jackson.annotation.JsonProperty;

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
public interface FitbitTokenClient {

    record Response(@JsonProperty("access_token") String accessToken) {}

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    Response post(
            @HeaderParam("Authorization") String authorization,
            @FormParam("grant_type") String grantType,
            @FormParam("code") String code,
            @FormParam("redirect_uri") String redirectUri);
}
