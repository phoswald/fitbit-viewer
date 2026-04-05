package com.github.phoswald.fitbit.viewer.fitbitapi;

import java.util.List;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.json.bind.annotation.JsonbProperty;

@RegisterRestClient(configKey = "fitbit-api")
@Path("/1/user/-/activities/heart")
public interface HeartRateApiClient {

    @GET
    @Path("/date/{startDate}/{endDate}.json")
    @Produces(MediaType.APPLICATION_JSON)
    HeartRateResponse getHeartRate(
            @HeaderParam("Authorization") String authorizationHeader,
            @PathParam("startDate") String startDate,
            @PathParam("endDate") String endDate);

    record HeartRateResponse(
            @JsonbProperty("activities-heart") List<HeartRateEntry> activitiesHeart
    ) { }

    record HeartRateEntry(
            String dateTime,
            HeartRateValue value
    ) { }

    record HeartRateValue(
            Integer restingHeartRate
    ) { }
}
