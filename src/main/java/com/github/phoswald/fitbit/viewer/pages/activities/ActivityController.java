package com.github.phoswald.fitbit.viewer.pages.activities;

import static com.github.phoswald.fitbit.viewer.healthapi.HealthApiHelpers.logIdOf;
import static java.util.Objects.requireNonNull;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.phoswald.fitbit.viewer.auth.SessionData;
import com.github.phoswald.fitbit.viewer.healthapi.HealthApiClient;
import com.github.phoswald.fitbit.viewer.pages.DateRangeController;
import com.github.phoswald.fitbit.viewer.tcx.TcxEntity;
import com.github.phoswald.fitbit.viewer.tcx.TcxRepository;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

@RequestScoped
@Path("/pages/activities")
public class ActivityController extends DateRangeController {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Inject
    private Template activities;

    @Inject
    private HealthApiClient healthApiClient;

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
                SortedMap<LocalDate, ActivityDayEntity> days = activityRepository.loadDaysByUserIdAndDateRange(session.userId(), dateBeg, dateEnd).stream()
                        .collect(toSortedMap(ActivityDayEntity::getDate));
                if(!refresh && isComplete(session, days)) {
                    log.debug("Found {} entities for {} days", activities.size(), days.size());
                } else {
                    Map<Long, ActivityEntity> oldActivities = activities;
                    activities = new LinkedHashMap<>();
                    for (var dateRange : getQueryDateRanges(Duration.ofDays(90))) {
                        var dataPoints = healthApiClient.getList(session, "exercise", "exercise.interval.civil_start_time", dateRange.beg(), dateRange.end());
                        activities.putAll(dataPoints.stream()
                                .filter(dataPoint -> dataPoint.getExercise() != null)
                                .map(dataPoint -> ActivityEntity.create(
                                        session.userId(), dataPoint, getLabels(oldActivities, logIdOf(dataPoint))))
                                .sorted(Comparator.comparing(ActivityEntity::getBegDateTime))
                                .collect(toLinkedHashMap(ActivityEntity::getLogId)));
                    }
                    for(LocalDate date = dateBeg; !date.isAfter(dateEnd); date = date.plusDays(1)) {
                        if(!days.containsKey(date)) {
                            days.put(date, createEmptyDay(session.userId(), date));
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

    public ActivityDayEntity createEmptyDay(String userId, LocalDate date) {
        log.debug("Filling gap: {}", date);
        ActivityDayEntity entity = new ActivityDayEntity();
        entity.setUserId(requireNonNull(userId, "userId"));
        entity.setDate(requireNonNull(date, "date"));
        return entity;
    }

    private boolean isQueryByLabelOnly() {
        return dateBeg == null && dateEnd == null
                && label != null && !Objects.equals(label, "(none)")
                && !refresh;
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
                log.info("Processing logId={}", entity.getLogId());
                tcxRepository.store(TcxEntity.create(entity.getUserId(), entity.getLogId(), entity.getTcxXml(), entity.getAltitudeCorrection()));
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
