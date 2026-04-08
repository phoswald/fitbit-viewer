package com.github.phoswald.fitbit.viewer.fitbitapi;

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
public interface ActivityApiClient {

    @GET
    @Path("/list.json")
    @Produces(MediaType.APPLICATION_JSON)
    ActivityResponse getActivities(
            @HeaderParam("Authorization") String authorizationHeader,
            @QueryParam("afterDate") String afterDate,
            @QueryParam("beforeDate") String beforeDate,
            @QueryParam("sort") String sort,
            @QueryParam("limit") int limit,
            @QueryParam("offset") int offset);

    @GET
    @Path("/list.json")
    @Produces(MediaType.APPLICATION_JSON)
    String getActivities2( // TODO: REMOVE
            @HeaderParam("Authorization") String authorizationHeader,
            @QueryParam("afterDate") String afterDate,
            @QueryParam("beforeDate") String beforeDate,
            @QueryParam("sort") String sort,
            @QueryParam("limit") int limit,
            @QueryParam("offset") int offset);

    record ActivityResponse(
            List<ActivityEntry> activities
    ) { }

    record ActivityEntry(
            Long logId,
            Integer activityTypeId,
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
            ActivitySource source,
            ActiveZoneMinutes activeZoneMinutes
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
            String url,
            List<String> trackerFeatures
    ) { }

    record ActiveZoneMinutes(
            Integer totalMinutes
    ) { }

    @GET
    @Path("/{logId}.tcx")
    @Produces(MediaType.TEXT_XML)
    String /*TcxDatabase*/ getActivityTcx(
            @HeaderParam("Authorization") String authorizationHeader,
            @PathParam("logId") Long logId);
}
