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
    @RestClient
    private ActivityTcxApiClient tcxClient;

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
                    ActivityDetailViewModel.createError(logId, null, "Query parameter 'date' is required."));
        }
        var session = sessionManager.parseAndVerifyCookie(sessionCookie);
        if (session.isPresent()) {
            log.debug("getActivityDetailPage: logId={}, date={}", logId, date);
            try {
                var response = activityClient.getActivities(
                        "Bearer " + session.get().accessToken(),
                        date.minusDays(1).toString(), "asc", 0, 10);
                var match = response.activities() == null ? null :
                        response.activities().stream()
                                .filter(e -> logId.equals(e.logId()))
                                .findFirst()
                                .orElse(null);
                if (match == null) {
                    return activityDetail.data("model",
                            ActivityDetailViewModel.createError(logId, date,
                                    "Activity with logId " + logId + " not found for date " + date + "."));
                }
                var trackPoints = fetchTrackPoints("Bearer " + session.get().accessToken());
                return activityDetail.data("model", ActivityDetailViewModel.create(logId, date, match, trackPoints));
            } catch (Exception e) {
                log.warn("getActivityDetailPage: failed to fetch activity", e);
                return activityDetail.data("model",
                        ActivityDetailViewModel.createError(logId, date, e.getMessage()));
            }
        } else {
            return activityDetail.data("model",
                    ActivityDetailViewModel.createError(logId, date, "You are not logged in."));
        }
    }

    private List<TrackPoint> fetchTrackPoints(String authorizationHeader) {
        try {
            log.debug("Querying TCX for logId={}...", logId); // XXX
            String tcx = tcxClient.getTcx(authorizationHeader, logId);
            log.debug("Querying TCX for logId={}: found string: {}", logId, tcx); // XXX
            var trackPoints = TcxParser.parse(tcx);
            log.info("Querying TCX for logId={}: found points: {}", logId, trackPoints.size()); // XXX
            return trackPoints;
        } catch (Exception e) {
            log.error("TCX not available for logId={}", logId); // XXX
            return List.of();
        }
    }
}
