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
    private ActivityRepository activityRepository;

    @Inject
    private TcxRepository tcxRepository;

    @PathParam("logId")
    private Long logId;

    @QueryParam("refreshTcx")
    private boolean refreshTcx;

    @QueryParam("editLabels")
    private boolean editLabels;

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
                Integer altitudeCorrection = tcxEntity.map(TcxEntity::getAltitudeCorrection).orElse(null);
                tcxEntity = Optional.of(TcxEntity.create(session.userId(), logId, tcxXml, altitudeCorrection));
                log.info("Storing TCX entity");
                tcxRepository.store(tcxEntity.get());
            }
            List<String> allLabels = activityRepository.loadLabelsByUserId(session.userId());
            return ActivityDetailViewModel.create(logId, entity.get(), tcxEntity, allLabels, editLabels, session.userId());
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

    @POST
    @Path("/altitude")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response postAltitude(@FormParam("altitudeCorrection") Integer correction) {
        var session = sessionManager.parseAndVerifyCookie(sessionCookie);
        if (session.isPresent()) {
            var tcxEntity = tcxRepository.load(session.get().userId(), logId);
            if(tcxEntity.isPresent()) {
                log.info("Updating altitude correction for logId={}", tcxEntity.get().getLogId());
                tcxEntity.get().setAltitudeCorrection(correction);
            }
        }
        return Response.seeOther(URI.create("pages/activities/" + logId)).build();
    }

    @GET
    @Path("/tcx")
    @Produces(MediaType.TEXT_XML)
    @Transactional
    public Response getTcx() {
        var session = sessionManager.parseAndVerifyCookie(sessionCookie);
        if (session.isPresent()) {
            var tcxEntity = tcxRepository.load(session.get().userId(), logId);
            if(tcxEntity.isPresent()) {
                var tcxXml = tcxEntity.get().getTcxXml();
                var tcxFile = "tcx-" + tcxEntity.get().getLogId() + ".xml";
                return Response
                        .ok(tcxXml, MediaType.APPLICATION_XML)
                        .header("Content-Disposition", "attachment; filename=\"" + tcxFile + "\"")
                        .build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }
}
