package com.github.phoswald.fitbit.viewer.profile;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
record ProfileViewModel(
        User user,
        String errorMessage,
        ZonedDateTime now
) {

    @RecordBuilder
    record User(
            String userId,
            String displayName,
            String fullName,
            String avatarUrl,
            Integer age,
            String dateOfBirth,
            String gender,
            String memberSince,
            String averageDailySteps
    ) {
    }

    static ProfileViewModel create(ProfileApiClient.UserData user) {
        return new ProfileViewModelBuilder()
                .user(new UserBuilder()
                        .userId(user.userId())
                        .displayName(user.displayName())
                        .fullName(user.fullName())
                        .avatarUrl(user.avatar())
                        .age(user.age())
                        .dateOfBirth(user.dateOfBirth())
                        .gender(user.gender())
                        .memberSince(user.memberSince())
                        .averageDailySteps(user.averageDailySteps())
                        .build())
                .now(ZonedDateTime.now())
                .build();
    }

    static ProfileViewModel createError(String errorMessage) {
        return new ProfileViewModelBuilder()
                .errorMessage(errorMessage)
                .now(ZonedDateTime.now())
                .build();
    }
}
