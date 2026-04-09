package com.github.phoswald.fitbit.viewer.repository;

import static com.github.phoswald.fitbit.viewer.ValueHelpers.dateOf;
import static com.github.phoswald.fitbit.viewer.ValueHelpers.max;
import static com.github.phoswald.fitbit.viewer.ValueHelpers.min;
import static com.github.phoswald.fitbit.viewer.ValueHelpers.minutesBetween;
import static java.util.Objects.requireNonNull;

import java.io.StringReader;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.xml.bind.JAXB;

import com.github.phoswald.fitbit.viewer.tcx.TcxDatabase;

@Entity
@Table(name = "fitbit_tcx_")
@IdClass(TcxEntity.TcxId.class)
public class TcxEntity {

    @Id
    @Column(name = "user_id_", length = 32, nullable = false)
    private String userId;

    @Id
    @Column(name = "log_id_", nullable = false)
    private long logId;

    @Column(name = "tcx_xml_", nullable = false)
    private String tcxXml;

    private transient TcxDatabase tcxDatabase;

    @Column(name = "date_")
    private LocalDate date;

    @Column(name = "beg_date_time_")
    private OffsetDateTime begDateTime;

    @Column(name = "end_date_time_")
    private OffsetDateTime endDateTime;

    @Column(name = "duration_minutes_")
    private Double durationMinutes;

    @Column(name = "distance_")
    private Double distance;

    @Column(name = "latitude_min_")
    private Double latitudeMin;

    @Column(name = "latitude_max_")
    private Double latitudeMax;

    @Column(name = "longitude_min_")
    private Double longitudeMin;

    @Column(name = "longitude_max_")
    private Double longitudeMax;

    @Column(name = "altitude_min_")
    private Double altitudeMin;

    @Column(name = "altitude_max_")
    private Double altitudeMax;

    @Column(name = "heart_rate_max_")
    private Integer heartRateMax;

    public static TcxEntity create(String userId, long logId, String tcxXml) {
        TcxEntity entity = new TcxEntity();
        entity.setUserId(requireNonNull(userId, "userId"));
        entity.setLogId(requireNonNull(logId, "logId"));
        entity.setTcxXml(tcxXml);
        TcxDatabase tcxDatabase = entity.getTcxDatabase().orElse(null);
        if(tcxDatabase != null) {
            for(var tp : tcxDatabase.collectTrackPoints()) {
                entity.setBegDateTime(min(entity.getBegDateTime(), tp.getTime()));
                entity.setEndDateTime(max(entity.getBegDateTime(), tp.getTime()));
                if(tp.getPosition() != null) {
                    entity.setLatitudeMin(min(entity.getLatitudeMin(), tp.getPosition().getLatitudeDegrees()));
                    entity.setLatitudeMax(max(entity.getLatitudeMax(), tp.getPosition().getLatitudeDegrees()));
                    entity.setLongitudeMin(min(entity.getLongitudeMin(), tp.getPosition().getLongitudeDegrees()));
                    entity.setLongitudeMax(max(entity.getLongitudeMax(), tp.getPosition().getLongitudeDegrees()));
                }
                entity.setAltitudeMin(min(entity.getAltitudeMin(), tp.getAltitudeMeters()));
                entity.setAltitudeMax(max(entity.getAltitudeMin(), tp.getAltitudeMeters()));
                entity.setDistance(max(entity.getDistance(), tp.getDistanceMeters())); // use max() because because last point is 0.0
                if(tp.getHeartRateBpm() != null) {
                    entity.setHeartRateMax(max(entity.getHeartRateMax(), tp.getHeartRateBpm().getValue()));
                }
            }
            entity.setDate(dateOf(entity.getBegDateTime()));
            entity.setDurationMinutes(minutesBetween(entity.getBegDateTime(), entity.getEndDateTime()));
        }
        return entity;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getLogId() {
        return logId;
    }

    public void setLogId(long logId) {
        this.logId = logId;
    }

    public String getTcxXml() {
        return tcxXml;
    }

    public void setTcxXml(String tcxXml) {
        this.tcxXml = tcxXml;
        this.tcxDatabase = null;
    }

    public Optional<TcxDatabase> getTcxDatabase() {
        if(tcxDatabase == null && tcxXml != null) {
            tcxDatabase = JAXB.unmarshal(new StringReader(tcxXml), TcxDatabase.class);
        }
        return Optional.ofNullable(tcxDatabase);
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public OffsetDateTime getBegDateTime() {
        return begDateTime;
    }

    public void setBegDateTime(OffsetDateTime begDateTime) {
        this.begDateTime = begDateTime;
    }

    public OffsetDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(OffsetDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public Double getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Double durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public Double getLatitudeMin() {
        return latitudeMin;
    }

    public void setLatitudeMin(Double latitudeMin) {
        this.latitudeMin = latitudeMin;
    }

    public Double getLatitudeMax() {
        return latitudeMax;
    }

    public void setLatitudeMax(Double latitudeMax) {
        this.latitudeMax = latitudeMax;
    }

    public Double getLongitudeMin() {
        return longitudeMin;
    }

    public void setLongitudeMin(Double longitudeMin) {
        this.longitudeMin = longitudeMin;
    }

    public Double getLongitudeMax() {
        return longitudeMax;
    }

    public void setLongitudeMax(Double longitudeMax) {
        this.longitudeMax = longitudeMax;
    }

    public Double getAltitudeMin() {
        return altitudeMin;
    }

    public void setAltitudeMin(Double altitudeMin) {
        this.altitudeMin = altitudeMin;
    }

    public Double getAltitudeMax() {
        return altitudeMax;
    }

    public void setAltitudeMax(Double altitudeMax) {
        this.altitudeMax = altitudeMax;
    }

    public Integer getHeartRateMax() {
        return heartRateMax;
    }

    public void setHeartRateMax(Integer heartRateMax) {
        this.heartRateMax = heartRateMax;
    }

    public record TcxId(String userId, long logId) implements java.io.Serializable { }
}
