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
@Path("/1.2/user/-/sleep")
public interface SleepApiClient {

    @GET
    @Path("/date/{startDate}/{endDate}.json")
    @Produces(MediaType.APPLICATION_JSON)
    SleepResponse getSleep(
            @HeaderParam("Authorization") String authorizationHeader,
            @PathParam("startDate") String startDate,
            @PathParam("endDate") String endDate);

    record SleepResponse(
            List<SleepEntry> sleep
    ) { }

    record SleepEntry(
            String dateOfSleep,
            String type,
            SleepLevels levels
    ) { }

    record SleepLevels(
            SleepLevelsSummary summary
    ) { }

    record SleepLevelsSummary(
            SleepLevelDetail asleep,
            SleepLevelDetail restless,
            SleepLevelDetail awake,
            SleepLevelDetail deep,
            SleepLevelDetail light,
            SleepLevelDetail rem,
            SleepLevelDetail wake
    ) { }

    record SleepLevelDetail(
            Integer minutes
    ) { }
}
