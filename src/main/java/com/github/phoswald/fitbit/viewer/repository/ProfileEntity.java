package com.github.phoswald.fitbit.viewer.repository;

import static java.util.Objects.requireNonNull;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.github.phoswald.fitbit.viewer.fitbitapi.ProfileApiClient;

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

    public static ProfileEntity create(ProfileApiClient.UserData user) {
        ProfileEntity entity = new ProfileEntity();
        entity.setUserId(requireNonNull(user.userId(), "userId"));
        entity.setDisplayName(user.displayName());
        entity.setFullName(user.fullName());
        entity.setAvatarUrl(user.avatar());
        entity.setDateOfBirth(user.dateOfBirth());
        entity.setAge(user.age());
        entity.setGender(user.gender());
        entity.setMemberSince(user.memberSince());
        entity.setHeight(maskZero(user.height()));
        entity.setWeight(maskZero(user.weight()));
        entity.setStrideLengthWalking(maskZero(user.strideLengthWalking()));
        entity.setStrideLengthRunning(maskZero(user.strideLengthRunning()));
        entity.setAverageDailySteps(user.averageDailySteps());
        return entity;
    }

    private static Integer maskZero(Integer value) {
        return value == null || value.intValue() == 0 ? null : value;
    }

    private static Double maskZero(Double value) {
        return value == null || value.doubleValue() == 0 ? null : value;
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
