package com.github.phoswald.fitbit.viewer.pages.activities;

import static java.util.function.Predicate.not;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.phoswald.fitbit.viewer.auth.SessionData;
import com.github.phoswald.fitbit.viewer.fitbitapi.ActivityApiClient;
import com.github.phoswald.fitbit.viewer.pages.BaseController;
import com.github.phoswald.fitbit.viewer.repository.ActivityRepository;
import com.github.phoswald.fitbit.viewer.repository.TcxEntity;
import com.github.phoswald.fitbit.viewer.repository.TcxRepository;
import com.github.phoswald.fitbit.viewer.tcx.GeoPoint;
import com.github.phoswald.fitbit.viewer.tcx.TcxDatabase;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

@RequestScoped
@Path("/pages/activities/{logId}")
public class ActivityDetailController extends BaseController {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Inject
    private Template activityDetail;

    @Inject
    @RestClient
    private ActivityApiClient activityApiClient;

    @Inject
    private TcxRepository tcxRepository;

    @Inject
    private ActivityRepository activityRepository;

    @PathParam("logId")
    private Long logId;

    @QueryParam("refreshTcx")
    private boolean refreshTcx;

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public TemplateInstance getActivityDetailPage() {
        var session = sessionManager.parseAndVerifyCookie(sessionCookie);
        if (session.isPresent()) {
            return activityDetail.data("model", createActivityDetailViewModel(session.get()));
        } else {
            return activityDetail.data("model", ActivityDetailViewModel.createError("You are not logged in."));
        }
    }

    private ActivityDetailViewModel createActivityDetailViewModel(SessionData session) {
        try {
            log.info("Querying: logId={}", logId);
            var entity = activityRepository.loadByUserIdAndLogId(session.userId(), logId);
            if (entity.isEmpty()) {
                return ActivityDetailViewModel.createError("Activity not found");
            }
            log.debug("Found entity with {} activity levels, {} heartrate zones, {} labels", entity.get().getActivityLevels().size(), entity.get().getHeartRateZones().size(), entity.get().getLabels().size());
            var tcxEntity = tcxRepository.load(session.userId(), logId);
            if(!refreshTcx && tcxEntity.isPresent()) {
                log.debug("Found TCX entity");
            } else {
                String tcxXml = activityApiClient.getActivityTcx("Bearer " + session.accessToken(), logId);
                tcxEntity = Optional.of(TcxEntity.create(session.userId(), logId, tcxXml));
                log.info("Storing TCX entity");
                tcxRepository.store(tcxEntity.get());
            }
            List<GeoPoint> track = tcxEntity
                    .flatMap(TcxEntity::getTcxDatabase)
                    .map(TcxDatabase::collectGeoPoints)
                    .orElse(List.of());
            log.debug("Found TCX track with {} points", track.size());
            List<String> allLabels = activityRepository.loadAllLabels(session.userId());
            return ActivityDetailViewModel.create(logId, entity.get(), allLabels, track);
        } catch (Exception e) {
            log.warn("Failed", e);
            return ActivityDetailViewModel.createError(e.getMessage());
        }
    }

    @POST
    @Path("/labels")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response postLabels(@FormParam("label") List<String> labels) {
        var session = sessionManager.parseAndVerifyCookie(sessionCookie);
        if (session.isPresent()) {
            var entity = activityRepository.loadByUserIdAndLogId(session.get().userId(), logId);
            if(entity.isPresent()) {
                log.info("Updating labels for logId={}: {}", logId, labels);
                entity.get().setLabels(normalizeLabels(labels));
            }
        }
        return Response.seeOther(URI.create("pages/activities/" + logId)).build();
    }

    private static List<String> normalizeLabels(List<String> labels) {
        return labels.stream()
                .filter(not(Objects::isNull))
                .filter(not(String::isBlank))
                .map(String::trim)
                .distinct()
                .toList();
    }
}
