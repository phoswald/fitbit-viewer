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
@Path("/1/user/-/activities/active-zone-minutes")
public interface ActiveZoneMinutesApiClient {

    @GET
    @Path("/date/{startDate}/{endDate}.json")
    @Produces(MediaType.APPLICATION_JSON)
    AzmResponse getAzm(
            @HeaderParam("Authorization") String authorizationHeader,
            @PathParam("startDate") String startDate,
            @PathParam("endDate") String endDate);

    record AzmResponse(
            @JsonbProperty("activities-active-zone-minutes") List<AzmEntry> activitiesActiveZoneMinutes
    ) { }

    record AzmEntry(
            String dateTime,
            AzmValue value
    ) { }

    record AzmValue(
            Integer activeZoneMinutes,
            Integer fatBurnActiveZoneMinutes,
            Integer cardioActiveZoneMinutes,
            Integer peakActiveZoneMinutes
    ) { }
}
