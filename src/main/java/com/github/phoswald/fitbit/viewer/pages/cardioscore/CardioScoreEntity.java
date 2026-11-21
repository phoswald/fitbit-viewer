package com.github.phoswald.fitbit.viewer.pages.cardioscore;

import static com.github.phoswald.fitbit.viewer.ValueHelpers.toDouble;
import static com.github.phoswald.fitbit.viewer.healthapi.HealthApiHelpers.dateOf;
import static java.util.Objects.requireNonNull;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

import com.google.api.health.v4.model.DailyVO2Max;

@Entity
@Table(name = "fitbit_cardioscore_")
@NamedQuery(
        name = "CardioScoreEntity.loadByUserIdAndDateRange",
        query = "SELECT c FROM CardioScoreEntity c WHERE c.userId = :userId AND c.date >= :dateBeg AND c.date <= :dateEnd ORDER BY c.date"
)
@IdClass(CardioScoreEntity.CardioScoreId.class)
public class CardioScoreEntity {

    @Id
    @Column(name = "user_id_", length = 32, nullable = false)
    private String userId;

    @Id
    @Column(name = "date_", nullable = false)
    private LocalDate date;

    @Column(name = "score_min_")
    private Double scoreMin;

    @Column(name = "score_max_")
    private Double scoreMax;

    @Column(name = "score_estimated_")
    private Double scoreEstimated;

    /**
     * The Google Health API returns a single value, never a range: scoreMin and scoreMax are only set on rows
     * cached from the Fitbit API. The value's "estimated" flag is not related to the scoreEstimated field.
     */
    public static CardioScoreEntity create(String userId, DailyVO2Max dailyVo2Max) {
        CardioScoreEntity entity = new CardioScoreEntity();
        entity.setUserId(requireNonNull(userId, "userId"));
        entity.setDate(requireNonNull(dateOf(dailyVo2Max.getDate()), "date"));
        entity.setScoreEstimated(toDouble(dailyVo2Max.getVo2Max()));
        return entity;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Double getScoreMin() {
        return scoreMin;
    }

    public void setScoreMin(Double scoreMin) {
        this.scoreMin = scoreMin;
    }

    public Double getScoreMax() {
        return scoreMax;
    }

    public void setScoreMax(Double scoreMax) {
        this.scoreMax = scoreMax;
    }

    public Double getScoreEstimated() {
        return scoreEstimated;
    }

    public void setScoreEstimated(Double scoreEstimated) {
        this.scoreEstimated = scoreEstimated;
    }

    public record CardioScoreId(String userId, LocalDate date) implements java.io.Serializable { }
}
