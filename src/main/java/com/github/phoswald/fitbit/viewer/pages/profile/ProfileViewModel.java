package com.github.phoswald.fitbit.viewer.pages.profile;

import java.time.ZonedDateTime;

import com.github.phoswald.fitbit.viewer.fitbitapi.ProfileApiClient;
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
            String dateOfBirth,
            Integer age,
            String gender,
            String memberSince,
            Integer height,
            Integer weight,
            Double strideLengthWalking,
            Double strideLengthRunning,
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
                        .dateOfBirth(user.dateOfBirth())
                        .age(user.age())
                        .gender(user.gender())
                        .memberSince(user.memberSince())
                        .height(user.height())
                        .weight(user.weight())
                        .strideLengthWalking(user.strideLengthWalking())
                        .strideLengthRunning(user.strideLengthRunning())
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
