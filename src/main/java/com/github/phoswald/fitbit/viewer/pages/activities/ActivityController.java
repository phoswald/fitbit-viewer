package com.github.phoswald.fitbit.viewer.pages.activities;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.phoswald.fitbit.viewer.auth.SessionData;
import com.github.phoswald.fitbit.viewer.fitbitapi.ActivityApiClient;
import com.github.phoswald.fitbit.viewer.pages.DateRangeController;
import com.github.phoswald.fitbit.viewer.repository.ActivityDayEntity;
import com.github.phoswald.fitbit.viewer.repository.ActivityEntity;
import com.github.phoswald.fitbit.viewer.repository.ActivityRepository;
import com.github.phoswald.fitbit.viewer.repository.TcxEntity;
import com.github.phoswald.fitbit.viewer.repository.TcxRepository;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

@RequestScoped
@Path("/pages/activities")
public class ActivityController extends DateRangeController {

    private static final int PAGE_SIZE = 50;

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Inject
    private Template activities;

    @Inject
    @RestClient
    private ActivityApiClient activityClient;

    @Inject
    private ActivityRepository activityRepository;

    @Inject
    private TcxRepository tcxRepository;

    @QueryParam("excludeAuto")
    private boolean excludeAuto;

    @QueryParam("excludeLowCal")
    private boolean excludeLowCal;

    @QueryParam("label")
    private String label;

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public TemplateInstance getActivitiesPage() {
        if(!isQueryByLabelOnly()) {
            normalizeDateRange();
        }
        var session = sessionManager.parseAndVerifyCookie(sessionCookie);
        if (session.isPresent()) {
            return activities.data("model", createActivityViewModel(session.get()));
        } else {
            return activities.data("model", ActivityViewModel.createError("You are not logged in."));
        }
    }

    private ActivityViewModel createActivityViewModel(SessionData session) {
        try {
            log.info("Querying: dateBeg={}, dateEnd={}, datePeriod={}, label={}", dateBeg, dateEnd, datePeriod, label);
            Map<Long, ActivityEntity> activities;
            if(isQueryByLabelOnly()) {
                activities = activityRepository.loadByUserIdAndLabel(session.userId(), label).stream()
                        .collect(toLinkedHashMap(ActivityEntity::getLogId));
                log.debug("Found {} entities for label={}", activities.size(), label);
            } else {
                activities = activityRepository.loadByUserIdAndDateRange(session.userId(), dateBeg, dateEnd).stream()
                        .collect(toLinkedHashMap(ActivityEntity::getLogId));
                var days = activityRepository.loadDaysByUserIdAndDateRange(session.userId(), dateBeg, dateEnd).stream()
                        .collect(toSortedMap(ActivityDayEntity::getDate));
                if(!refresh && isComplete(days)) {
                    log.debug("Found {} entities for {} days", activities.size(), days.size());
                } else {
                    var oldActivities = activities;
                    activities = new LinkedHashMap<>();
                    LocalDate currentDateBeg = dateBeg;
                    while (!currentDateBeg.isAfter(dateEnd)) {
                        log.debug("Querying (current): dateBeg={}, limit={}", currentDateBeg, PAGE_SIZE);
                        var response = activityClient.getActivities(
                                "Bearer " + session.accessToken(),
                                currentDateBeg.toString(),
                                null, "asc",
                                PAGE_SIZE, 0);
                        var entitiesNew = response.activities().stream()
                                .map(entry -> ActivityEntity.create(
                                        session.userId(), entry, getLabels(oldActivities, entry.logId())))
                                .filter(e -> !e.getDate().isAfter(dateEnd))
                                .toList();
                        log.debug("Found: {}", entitiesNew.size());
                        if (addAll(activities, entitiesNew) == 0) {
                            break;
                        }
                        currentDateBeg = entitiesNew.getLast().getDate();
                        if (response.activities().size() < PAGE_SIZE) {
                            break;
                        }
                    }
                    for(LocalDate date = dateBeg; !date.isAfter(dateEnd); date = date.plusDays(1)) {
                        if(!days.containsKey(date)) {
                            log.debug("Filling gap: {}", date);
                            days.put(date, ActivityDayEntity.create(session.userId(), date));
                        }
                    }
                    log.info("Storing {} entities for {} days", activities.size(), days.size());
                    activityRepository.storeAll(activities.values());
                    activityRepository.storeAllDays(days.values());
                }
            }
            var allLabels = activityRepository.loadLabelsByUserId(session.userId());
            return ActivityViewModel.create(createDateRangeViewModel(), label, excludeAuto, excludeLowCal, activities.values(), allLabels, session.userId());
        } catch (Exception e) {
            log.warn("Failed", e);
            return ActivityViewModel.createError(e.getMessage());
        }
    }

    private boolean isQueryByLabelOnly() {
        return dateBeg == null && dateEnd == null
                && label != null && !Objects.equals(label, "(none)")
                && !refresh;
    }

    private static int addAll(Map<Long, ActivityEntity> activities, List<ActivityEntity> activitiesNew) {
        int oldSize = activities.size();
        for(ActivityEntity activity : activitiesNew) {
            if(!activities.containsKey(activity.getLogId())) {
                activities.put(activity.getLogId(), activity);
            }
        }
        return activities.size() - oldSize;
    }

    private static List<String> getLabels(Map<Long, ActivityEntity> activities, Long logId) {
        ActivityEntity activity = activities.get(logId);
        if(activity != null) {
            return activity.getLabels();
        } else {
            return Collections.emptyList();
        }
    }

    @POST
    @Path("/upgrade")
    @Transactional
    public Response upgrade() {
        var session = sessionManager.parseAndVerifyCookie(sessionCookie);
        if (session.isPresent()) {
            List<TcxEntity> entities = tcxRepository.loadUpgradeRequiredByUserId(session.get().userId(), 10);
            for(TcxEntity entity : entities) {
                log.info("Processing userId={}, logId={}", entity.getUserId(), entity.getLogId());
                tcxRepository.store(TcxEntity.create(entity.getUserId(), entity.getLogId(), entity.getTcxXml()));
            }
            if(entities.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            } else {
                return Response.ok().build();
            }
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }
}
