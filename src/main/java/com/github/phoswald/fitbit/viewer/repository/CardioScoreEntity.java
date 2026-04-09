package com.github.phoswald.fitbit.viewer.repository;

import static com.github.phoswald.fitbit.viewer.ValueHelpers.parseDate;
import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

import com.github.phoswald.fitbit.viewer.fitbitapi.CardioScoreApiClient;

@Entity
@Table(name = "fitbit_cardioscore_")
@NamedQuery(
        name = "CardioScoreEntity.loadByUserIdAndDateRange",
        query = "SELECT c FROM CardioScoreEntity c WHERE c.userId = :userId AND c.date >= :begDate AND c.date <= :endDate ORDER BY c.date"
)
@IdClass(CardioScoreEntity.CardioScoreId.class)
public class CardioScoreEntity {

    private static final Pattern PATTERN_RANGE = Pattern.compile("([0-9]+)-([0-9]+)");

    @Id
    @Column(name = "user_id_", length = 32, nullable = false)
    private String userId;

    @Id
    @Column(name = "date_", nullable = false)
    private LocalDate date;

    @Column(name = "score_min_")
    private Integer scoreMin;

    @Column(name = "score_max_")
    private Integer scoreMax;

    public static CardioScoreEntity create(String userId, CardioScoreApiClient.CardioScoreEntry entry) {
        CardioScoreEntity entity = new CardioScoreEntity();
        entity.setUserId(requireNonNull(userId, "userId"));
        entity.setDate(parseDate(requireNonNull(entry.dateTime(), "date")));
        Matcher matcher = PATTERN_RANGE.matcher(entry.value() == null ? "" : entry.value().vo2Max());
        if(matcher.matches()) {
            entity.setScoreMin(Integer.parseInt(matcher.group(1)));
            entity.setScoreMax(Integer.parseInt(matcher.group(2)));
        }
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

    public Integer getScoreMin() {
        return scoreMin;
    }

    public void setScoreMin(Integer scoreMin) {
        this.scoreMin = scoreMin;
    }

    public Integer getScoreMax() {
        return scoreMax;
    }

    public void setScoreMax(Integer scoreMax) {
        this.scoreMax = scoreMax;
    }

    public record CardioScoreId(String userId, LocalDate date) implements java.io.Serializable { }
}
