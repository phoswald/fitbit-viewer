package com.github.phoswald.fitbit.viewer.azm;

import java.util.List;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import com.fasterxml.jackson.annotation.JsonProperty;

@RegisterRestClient(configKey = "fitbit-api")
@Path("/1/user/-/activities/active-zone-minutes/date")
interface AzmApiClient {

    @GET
    @Path("/{startDate}/{endDate}.json")
    @Produces(MediaType.APPLICATION_JSON)
    AzmResponse getAzm(
            @HeaderParam("Authorization") String authorizationHeader,
            @PathParam("startDate") String startDate,
            @PathParam("endDate") String endDate);

    record AzmResponse(
            @JsonProperty("activities-active-zone-minutes") List<AzmEntry> activitiesActiveZoneMinutes
    ) { }

    record AzmEntry(String dateTime, AzmValue value) { }

    record AzmValue(
            Integer activeZoneMinutes,
            Integer fatBurnActiveZoneMinutes,
            Integer cardioActiveZoneMinutes,
            Integer peakActiveZoneMinutes
    ) { }
}
