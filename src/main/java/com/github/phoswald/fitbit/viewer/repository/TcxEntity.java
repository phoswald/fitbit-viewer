package com.github.phoswald.fitbit.viewer.repository;

import static com.github.phoswald.fitbit.viewer.ValueHelpers.dateOf;
import static com.github.phoswald.fitbit.viewer.ValueHelpers.max;
import static com.github.phoswald.fitbit.viewer.ValueHelpers.min;
import static com.github.phoswald.fitbit.viewer.ValueHelpers.minutesBetween;
import static com.github.phoswald.fitbit.viewer.ValueHelpers.round;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.xml.bind.JAXB;

import com.github.phoswald.fitbit.viewer.tcx.TcxDatabase;

@Entity
@Table(name = "fitbit_tcx_")
//@NamedQuery(
//        name = "TcxEntity.loadUpgradeRequiredByUserId",
//        query = "SELECT t FROM TcxEntity t WHERE t.userId = :userId AND t.tcxXmlGz IS NULL AND t.tcxXml IS NOT NULL"
//)
@IdClass(TcxEntity.TcxId.class)
public class TcxEntity {

    @Id
    @Column(name = "user_id_", length = 32, nullable = false)
    private String userId;

    @Id
    @Column(name = "log_id_", nullable = false)
    private long logId;

//    @Column(name = "tcx_xml_" /*, nullable = false */)
//    private String tcxXml;

    @Column(name = "tcx_xml_gz_" /*, nullable = false */)
    private byte[] tcxXmlGz;

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
    private Integer altitudeMin;

    @Column(name = "altitude_max_")
    private Integer altitudeMax;

    @Column(name = "altitude_correction_")
    private Integer altitudeCorrection;

    @Column(name = "heart_rate_max_")
    private Integer heartRateMax;

    public static TcxEntity create(String userId, long logId, String tcxXml, Integer altitudeCorrection) {
        TcxEntity entity = new TcxEntity();
        entity.setUserId(requireNonNull(userId, "userId"));
        entity.setLogId(requireNonNull(logId, "logId"));
        entity.setTcxXml(tcxXml);
        entity.setAltitudeCorrection(altitudeCorrection);
        TcxDatabase tcxDatabase = entity.getTcxDatabase().orElse(null);
        if(tcxDatabase != null) {
            for(var tp : tcxDatabase.collectTrackPoints()) {
                entity.setBegDateTime(min(entity.getBegDateTime(), tp.getTime()));
                entity.setEndDateTime(max(entity.getEndDateTime(), tp.getTime())); // was wrong
                if(tp.getPosition() != null) {
                    entity.setLatitudeMin(min(entity.getLatitudeMin(), tp.getPosition().getLatitudeDegrees()));
                    entity.setLatitudeMax(max(entity.getLatitudeMax(), tp.getPosition().getLatitudeDegrees()));
                    entity.setLongitudeMin(min(entity.getLongitudeMin(), tp.getPosition().getLongitudeDegrees()));
                    entity.setLongitudeMax(max(entity.getLongitudeMax(), tp.getPosition().getLongitudeDegrees()));
                }
                entity.setAltitudeMin(min(entity.getAltitudeMin(), round(tp.getAltitudeMeters())));
                entity.setAltitudeMax(max(entity.getAltitudeMax(), round(tp.getAltitudeMeters()))); // was wrong
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
        return uncompress(tcxXmlGz);
    }

//    public String getTcxXml() {
//        if(tcxXmlGz != null) {
//            return uncompress(tcxXmlGz);
//        } else {
//            return tcxXml;
//        }
//    }

    public void setTcxXml(String tcxXml) {
        this.tcxDatabase = null;
//        this.tcxXml = tcxXml;
        this.tcxXmlGz = compress(tcxXml);
    }

    public Optional<TcxDatabase> getTcxDatabase() {
        if(tcxDatabase == null && tcxXmlGz != null) {
            tcxDatabase = JAXB.unmarshal(new StringReader(uncompress(tcxXmlGz)), TcxDatabase.class);
        }
        return Optional.ofNullable(tcxDatabase);
    }

//    public Optional<TcxDatabase> getTcxDatabase() {
//        if(tcxDatabase == null) {
//            if(tcxXmlGz != null) {
//                tcxDatabase = JAXB.unmarshal(new StringReader(uncompress(tcxXmlGz)), TcxDatabase.class);
//            } else if(tcxXml != null){
//                tcxDatabase = JAXB.unmarshal(new StringReader(tcxXml), TcxDatabase.class);
//            }
//        }
//        return Optional.ofNullable(tcxDatabase);
//    }

    private static byte[] compress(String text) {
        if(text == null) {
            return null;
        }
        var buffer = new ByteArrayOutputStream();
        try (var stream = new GZIPOutputStream(buffer)) {
            stream.write(text.getBytes(UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return buffer.toByteArray();
    }

    private static String uncompress(byte[] binary) {
        if(binary == null) {
            return null;
        }
        try (var stream = new GZIPInputStream(new ByteArrayInputStream(binary))) {
            return new String(stream.readAllBytes(), UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
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

    public Integer getAltitudeMin() {
        return altitudeMin;
    }

    public void setAltitudeMin(Integer altitudeMin) {
        this.altitudeMin = altitudeMin;
    }

    public Integer getAltitudeMax() {
        return altitudeMax;
    }

    public void setAltitudeMax(Integer altitudeMax) {
        this.altitudeMax = altitudeMax;
    }

    public Integer getAltitudeCorrection() {
        return altitudeCorrection;
    }

    public void setAltitudeCorrection(Integer altitudeCorrection) {
        this.altitudeCorrection = altitudeCorrection;
    }

    public Integer getHeartRateMax() {
        return heartRateMax;
    }

    public void setHeartRateMax(Integer heartRateMax) {
        this.heartRateMax = heartRateMax;
    }

    public record TcxId(String userId, long logId) implements java.io.Serializable { }
}
