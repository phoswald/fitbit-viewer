package com.github.phoswald.fitbit.viewer.activities;

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

import com.github.phoswald.fitbit.viewer.login.SessionData;
import com.github.phoswald.fitbit.viewer.login.SessionManager;

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
                        date.minusDays(1).toString(), "asc", 0, 10);
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
        if (tcxDatabase == null || tcxDatabase.activities == null) {
            return List.of();
        }
        return tcxDatabase.activities.stream()
                .filter(a -> a.laps != null)
                .flatMap(a -> a.laps.stream())
                .filter(l -> l.trackpoints != null)
                .flatMap(l -> l.trackpoints.stream())
                .filter(tp -> tp.position != null && tp.position.latitudeDegrees != null && tp.position.longitudeDegrees != null)
                .map(tp -> new TrackPoint(tp.position.latitudeDegrees, tp.position.longitudeDegrees))
                .toList();
    }
}
