package com.github.phoswald.fitbit.viewer.pages.profile;

import java.time.ZonedDateTime;

import com.github.phoswald.fitbit.viewer.repository.ProfileEntity;
import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
record ProfileViewModel(
        ProfileEntity profile,
        String errorMessage,
        ZonedDateTime now
) {

    static ProfileViewModel create(ProfileEntity profile) {
        return new ProfileViewModelBuilder()
                .profile(profile)
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
