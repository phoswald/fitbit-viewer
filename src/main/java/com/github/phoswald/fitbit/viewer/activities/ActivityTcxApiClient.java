package com.github.phoswald.fitbit.viewer.activities;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "fitbit-api")
@Path("/1/user/-/activities")
interface ActivityTcxApiClient {

    @GET
    @Path("/{logId}/tcx.json")
    @Produces(MediaType.WILDCARD)
    String getTcx(
            @HeaderParam("Authorization") String authorizationHeader,
            @PathParam("logId") Long logId);
}
