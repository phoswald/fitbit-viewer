package com.github.phoswald.fitbit.viewer.healthapi;

import static com.github.phoswald.fitbit.viewer.HealthApiHelpers.dateRange;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.net.URLEncoder;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.phoswald.fitbit.viewer.auth.SessionData;
import com.google.api.health.v4.api.DefaultApi;
import com.google.api.health.v4.model.DailyRollUpDataPointsRequest;
import com.google.api.health.v4.model.DailyRollUpDataPointsResponse;
import com.google.api.health.v4.model.DailyRollupDataPoint;
import com.google.api.health.v4.model.DataPoint;
import com.google.api.health.v4.model.ExportExerciseTcxResponse;
import com.google.api.health.v4.model.ListDataPointsResponse;
import com.google.api.health.v4.model.Profile;
import com.google.api.health.v4.model.ReconcileDataPointsResponse;
import com.google.api.health.v4.model.ReconciledDataPoint;

@Dependent
public class HealthApiClient {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Inject
    @RestClient
    private DefaultApi defaultApiClient;

    public Profile getProfile(SessionData session) {
        log.info("Requesting Profile");
        return defaultApiClient.healthUsersGetProfile(
                "Bearer " + session.accessToken(),
                "me");
    }

    public List<DailyRollupDataPoint> postDailyRollup(SessionData session, String dataType, LocalDate dateBeg, LocalDate dateEnd) {
        log.info("Requesting {} (DailyRollUp): dateBeg={}, dateEnd={}", dataType, dateBeg, dateEnd);
        List<DailyRollupDataPoint> dataPoints = new ArrayList<>();
        String pageToken = null;
        int iterations = 0;
        do {
            DailyRollUpDataPointsRequest request = new DailyRollUpDataPointsRequest()
                    .range(dateRange(dateBeg, dateEnd))
                    .windowSizeDays(1)
                    .pageToken(pageToken);
            DailyRollUpDataPointsResponse response = defaultApiClient.healthUsersDataTypesDataPointsDailyRollUp(
                    "Bearer " + session.accessToken(),
                    "users/me/dataTypes/" + dataType,
                    request);
            if(response.getRollupDataPoints() != null) {
                dataPoints.addAll(response.getRollupDataPoints());
            }
            pageToken = response.getNextPageToken();
            iterations++;
        } while(pageToken != null);
        log.info("Received {}: {} points, {} iterations", dataType, dataPoints.size(), iterations);
        return dataPoints;
    }

    public List<ReconciledDataPoint> getReconcile(SessionData session, String dataType, String filterField, LocalDate dateBeg, LocalDate dateEnd) {
        log.info("Requesting {} (Reconcile): dateBeg={}, dateEnd={}, filterField={}", dataType, dateBeg, dateEnd, filterField);
        List<ReconciledDataPoint> dataPoints = new ArrayList<>();
        String pageToken = null;
        int iterations = 0;
        do {
            ReconcileDataPointsResponse response = defaultApiClient.healthUsersDataTypesDataPointsReconcile(
                    "Bearer " + session.accessToken(),
                    "users/me/dataTypes/" + dataType,
                    pageToken, null, null,
                    encodeQueryParam(dateFilter(filterField, dateBeg, dateEnd)));
            if(response.getDataPoints() != null) {
                dataPoints.addAll(response.getDataPoints());
            }
            pageToken = response.getNextPageToken();
            iterations++;
        } while(pageToken != null);
        log.info("Received {}: {} points, {} iterations", dataType, dataPoints.size(), iterations);
        return dataPoints;
    }
    
    public List<DataPoint> getList(SessionData session, String dataType, String filterField, LocalDate dateBeg, LocalDate dateEnd) {
        log.info("Requesting {} (List): dateBeg={}, dateEnd={}, filterField={}", dataType, dateBeg, dateEnd, filterField);
        List<DataPoint> dataPoints = new ArrayList<>();
        String pageToken = null;
        int iterations = 0;
        do {
            ListDataPointsResponse response = defaultApiClient.healthUsersDataTypesDataPointsList(
                    "Bearer " + session.accessToken(),
                    "users/me/dataTypes/" + dataType,
                    pageToken, null,
                    encodeQueryParam(dateFilter(filterField, dateBeg, dateEnd)), null);
            if(response.getDataPoints() != null) {
                dataPoints.addAll(response.getDataPoints());
            }
            pageToken = response.getNextPageToken();
            iterations++;
        } while(pageToken != null);
        log.info("Received {}: {} points, {} iterations", dataType, dataPoints.size(), iterations);
        return dataPoints;
    }

    public DataPoint getFirstOfList(SessionData session, String dataType) {
        log.info("Requesting {} (first of List)", dataType);
        List<DataPoint> dataPoints = new ArrayList<>();
        ListDataPointsResponse response = defaultApiClient.healthUsersDataTypesDataPointsList(
                "Bearer " + session.accessToken(),
                "users/me/dataTypes/" + dataType,
                null, 1,
                null, null);
        if(response.getDataPoints() != null) {
            dataPoints.addAll(response.getDataPoints());
        }
        log.info("Received {}: {} points", dataType, dataPoints.size());
        return dataPoints.isEmpty() ? null : dataPoints.getFirst();
    }

    public String getExportExerciseTcx(SessionData session, long logId) {
        log.info("Requesting TCX: logId={}", logId);
        ExportExerciseTcxResponse response = defaultApiClient.healthUsersDataTypesDataPointsExportExerciseTcx(
                "Bearer " + session.accessToken(),
                "users/me/dataTypes/exercise/dataPoints/" + logId,
                null);
        log.info("Received TCX: {} chars", response.getTcxData().length());
        return response.getTcxData();
    }

    private String dateFilter(String field, LocalDate dateBeg, LocalDate dateEnd) {
        return field + " >= \"" + dateBeg + "\" AND " + field + " < \"" + dateEnd.plusDays(1) + "\"";
    }

    private String encodeQueryParam(String value) {
        // TODO (cleanup): get rid of this, also see @Encoded in application.properties
        return URLEncoder.encode(value, UTF_8).replace("+", "%20");
    }
}
