package com.github.phoswald.fitbit.viewer.pages.activities;

import java.time.LocalDate;
import java.util.List;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.phoswald.fitbit.viewer.auth.SessionManager;
import com.github.phoswald.fitbit.viewer.fitbitapi.ActivityApiClient;
import com.github.phoswald.fitbit.viewer.tcx.TcxDatabase;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

@RequestScoped
@Path("/pages/activities/{logId}")
public class ActivityDetailController {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Inject
    private Template activityDetail;

    @Inject
    @RestClient
    private ActivityApiClient activityClient;

    @Inject
    private SessionManager sessionManager;

    @CookieParam(SessionManager.COOKIE_NAME)
    private String sessionCookie;

    @PathParam("logId")
    private Long logId;

    @QueryParam("date")
    private LocalDate date;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getActivityDetailPage() {
        if (date == null) {
            return activityDetail.data("model",
                    ActivityDetailViewModel.createError("Query parameter 'date' is required."));
        }
        var session = sessionManager.parseAndVerifyCookie(sessionCookie);
        if (session.isPresent()) {
            try {
                log.info("getActivityDetailPage(): logId={}, date={}", logId, date);
                var response = activityClient.getActivities(
                        "Bearer " + session.get().accessToken(),
                        date.toString(), null, "asc", 10, 0);
                var activity = response.activities() == null ? null :
                        response.activities().stream()
                        .filter(e -> logId.equals(e.logId()))
                        .findFirst()
                        .orElse(null);
                if (activity == null) {
                    return activityDetail.data("model",
                            ActivityDetailViewModel.createError("Activity not found"));
                }
                var tcxDatabase = activityClient.getActivityTcx("Bearer " + session.get().accessToken(), logId);
                var trackPoints = collectTrackPoints(tcxDatabase);
                log.debug("getActivityDetailPage(): logId={}: found {} points", logId, trackPoints.size());
                return activityDetail.data("model", ActivityDetailViewModel.create(logId, date, activity, trackPoints));
            } catch (Exception e) {
                log.warn("getActivityDetailPage(): failed", e);
                return activityDetail.data("model", ActivityDetailViewModel.createError(e.getMessage()));
            }
        } else {
            return activityDetail.data("model", ActivityDetailViewModel.createError("You are not logged in."));
        }
    }

    private List<TrackPoint> collectTrackPoints(TcxDatabase tcxDatabase) {
        if (tcxDatabase == null || tcxDatabase.getActivities() == null) {
            return List.of();
        }
        return tcxDatabase.getActivities().stream()
                .filter(activity -> activity.getLaps() != null)
                .flatMap(activity -> activity.getLaps().stream())
                .filter(lap -> lap.getTrackpoints() != null)
                .flatMap(lap -> lap.getTrackpoints().stream())
                .filter(trackpoint -> trackpoint.getPosition() != null && trackpoint.getPosition().getLatitudeDegrees() != null && trackpoint.getPosition().getLongitudeDegrees() != null)
                .map(trackpoint -> new TrackPoint(trackpoint.getPosition().getLatitudeDegrees(), trackpoint.getPosition().getLongitudeDegrees()))
                .toList();
    }
}
