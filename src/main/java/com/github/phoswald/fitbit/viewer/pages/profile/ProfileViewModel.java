package com.github.phoswald.fitbit.viewer.pages.profile;

import java.time.ZonedDateTime;

import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
record ProfileViewModel(
        ProfileEntity profile,
        String userId,
        boolean userCanSwitchToFitBit, // TODO (Legacy API Migration): remove when no longer needed
        String errorMessage,
        ZonedDateTime now
) {

    static ProfileViewModel create(ProfileEntity profile, String userId) {
        return new ProfileViewModelBuilder()
                .profile(profile)
                .userId(userId)
                .userCanSwitchToFitBit(userId.endsWith("@gmail.com"))
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
