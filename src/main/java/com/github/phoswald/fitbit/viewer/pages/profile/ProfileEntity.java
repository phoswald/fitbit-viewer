package com.github.phoswald.fitbit.viewer.pages.profile;

import static com.github.phoswald.fitbit.viewer.ValueHelpers.divideBy;
import static com.github.phoswald.fitbit.viewer.ValueHelpers.maskZero;
import static com.github.phoswald.fitbit.viewer.ValueHelpers.toDateString;
import static com.github.phoswald.fitbit.viewer.healthapi.HealthApiHelpers.dateOf;
import static com.github.phoswald.fitbit.viewer.healthapi.HealthApiHelpers.firstOf;
import static java.util.Objects.requireNonNull;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.github.phoswald.fitbit.viewer.auth.SessionData;
import com.google.api.health.v4.model.Profile;

@Entity
@Table(name = "fitbit_profile_")
public class ProfileEntity {

    @Id
    @Column(name = "user_id_", length = 32, nullable = false)
    private String userId;

    @Column(name = "display_name_")
    private String displayName;

    @Column(name = "full_name_")
    private String fullName;

    @Column(name = "avatar_url_")
    private String avatarUrl;

    @Column(name = "date_of_birth_")
    private String dateOfBirth;

    @Column(name = "age_")
    private Integer age;

    @Column(name = "gender_")
    private String gender;

    @Column(name = "member_since_")
    private String memberSince;

    @Column(name = "height_")
    private Integer height;

    @Column(name = "weight_")
    private Integer weight;

    @Column(name = "stride_length_walking_")
    private Double strideLengthWalking;

    @Column(name = "stride_length_running_")
    private Double strideLengthRunning;

    @Column(name = "average_daily_steps_")
    private String averageDailySteps;

    public static ProfileEntity create(SessionData session, Profile profile, Integer height, Integer weight) {
        ProfileEntity entity = new ProfileEntity();
        entity.setUserId(requireNonNull(session.userId(), "userId"));
        entity.setFullName(session.userFullName());
        entity.setAvatarUrl(session.userPictureUrl());
        entity.setAge(profile.getAge());
        entity.setMemberSince(toDateString(dateOf(profile.getMembershipStartDate())));
        entity.setHeight(maskZero(height));
        entity.setWeight(maskZero(weight));
        entity.setStrideLengthWalking(divideBy(firstOf(profile.getUserConfiguredWalkingStrideLengthMm(), profile.getAutoWalkingStrideLengthMm()), 10));
        entity.setStrideLengthRunning(divideBy(firstOf(profile.getUserConfiguredRunningStrideLengthMm(), profile.getAutoRunningStrideLengthMm()), 10));
        return entity;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getMemberSince() {
        return memberSince;
    }

    public void setMemberSince(String memberSince) {
        this.memberSince = memberSince;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public Double getStrideLengthWalking() {
        return strideLengthWalking;
    }

    public void setStrideLengthWalking(Double strideLengthWalking) {
        this.strideLengthWalking = strideLengthWalking;
    }

    public Double getStrideLengthRunning() {
        return strideLengthRunning;
    }

    public void setStrideLengthRunning(Double strideLengthRunning) {
        this.strideLengthRunning = strideLengthRunning;
    }

    public String getAverageDailySteps() {
        return averageDailySteps;
    }

    public void setAverageDailySteps(String averageDailySteps) {
        this.averageDailySteps = averageDailySteps;
    }
}
