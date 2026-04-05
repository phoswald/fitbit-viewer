package com.github.phoswald.fitbit.viewer.activities;

import java.util.List;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "fitbit-api")
@Path("/1/user/-/activities")
interface ActivityApiClient {

    @GET
    @Path("/list.json")
    @Produces(MediaType.APPLICATION_JSON)
    ActivityResponse getActivities(
            @HeaderParam("Authorization") String authorizationHeader,
            @QueryParam("afterDate") String afterDate,
            @QueryParam("sort") String sort,
            @QueryParam("offset") int offset,
            @QueryParam("limit") int limit);

    record ActivityResponse(
            List<ActivityEntry> activities
    ) { }

    record ActivityEntry(
            Long logId,
            String activityName,
            String startTime,
            Long duration,
            Long activeDuration,
            Integer calories,
            Integer steps,
            Double distance,
            String distanceUnit,
            Integer averageHeartRate,
            String logType,
            List<HeartRateZone> heartRateZones,
            List<ActivityLevel> activityLevel,
            Double pace,
            Double speed,
            Double elevationGain,
            Integer floors,
            ActivitySource source
    ) { }

    record HeartRateZone(
            String name,
            Integer minutes,
            Double caloriesOut,
            Integer min,
            Integer max
    ) { }

    record ActivityLevel(
            String name,
            Integer minutes
    ) { }

    record ActivitySource(
            String name,
            String type,
            String url
    ) { }

    @GET
    @Path("/{logId}.tcx")
    @Produces(MediaType.TEXT_XML)
    TcxDatabase getActivityTcx(
            @HeaderParam("Authorization") String authorizationHeader,
            @PathParam("logId") Long logId);
}
