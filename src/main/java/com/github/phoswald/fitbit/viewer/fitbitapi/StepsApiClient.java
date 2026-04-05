package com.github.phoswald.fitbit.viewer.fitbitapi;

import java.util.List;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "fitbit-api")
@Path("/1/user/-/activities/steps")
public interface StepsApiClient {

    @GET
    @Path("/date/{startDate}/{endDate}.json")
    @Produces(MediaType.APPLICATION_JSON)
    StepsResponse getSteps(
            @HeaderParam("Authorization") String authorizationHeader,
            @PathParam("startDate") String startDate,
            @PathParam("endDate") String endDate);

    record StepsResponse(
            @JsonbProperty("activities-steps") List<StepsEntry> activitiesSteps
    ) { }

    record StepsEntry(
            String dateTime,
            String value
    ) { }
}
