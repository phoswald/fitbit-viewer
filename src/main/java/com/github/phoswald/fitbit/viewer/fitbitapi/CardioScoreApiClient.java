package com.github.phoswald.fitbit.viewer.fitbitapi;

import java.util.List;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;


@RegisterRestClient(configKey = "fitbit-api")
@Path("/1/user/-/cardioscore")
public interface CardioScoreApiClient {

    @GET
    @Path("/date/{startDate}/{endDate}.json")
    @Produces(MediaType.APPLICATION_JSON)
    CardioScoreResponse getCardioScore(
            @HeaderParam("Authorization") String authorizationHeader,
            @PathParam("startDate") String startDate,
            @PathParam("endDate") String endDate);

    record CardioScoreResponse(
            List<CardioScoreEntry> cardioScore
    ) { }

    record CardioScoreEntry(
            String dateTime,
            CardioScoreValue value
    ) { }

    record CardioScoreValue(
            String vo2Max
    ) { }
}
