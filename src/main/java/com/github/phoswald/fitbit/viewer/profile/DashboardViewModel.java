package com.github.phoswald.fitbit.viewer.profile;

import java.time.ZonedDateTime;

import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
record DashboardViewModel(
        Profile profile,
        String errorMessage,
        ZonedDateTime now
) {

    @RecordBuilder
    record Profile(
            String displayName,
            String fullName,
            Integer age,
            String gender,
            String avatarUrl
    ) {
    }

    static DashboardViewModel create(ProfileApiClient.UserData user) {
        return new DashboardViewModelBuilder()
                .profile(new ProfileBuilder()
                        .displayName(user.displayName())
                        .fullName(user.fullName())
                        .age(user.age())
                        .gender(user.gender())
                        .avatarUrl(user.avatar())
                        .build())
                .now(ZonedDateTime.now())
                .build();
    }

    static DashboardViewModel createError(String errorMessage) {
        return new DashboardViewModelBuilder()
                .errorMessage(errorMessage)
                .now(ZonedDateTime.now())
                .build();
    }
}
