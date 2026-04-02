package com.github.phoswald.fitbit.viewer.profile;

import java.time.ZonedDateTime;

import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
record ProfileViewModel(
        User user,
        String errorMessage,
        ZonedDateTime now
) {

    @RecordBuilder
    record User(
            String displayName,
            String fullName,
            Integer age,
            String gender,
            String avatarUrl
    ) {
    }

    static ProfileViewModel create(ProfileApiClient.UserData user) {
        return new ProfileViewModelBuilder()
                .user(new UserBuilder()
                        .displayName(user.displayName())
                        .fullName(user.fullName())
                        .age(user.age())
                        .gender(user.gender())
                        .avatarUrl(user.avatar())
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
