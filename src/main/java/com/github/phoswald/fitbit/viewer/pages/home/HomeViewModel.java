package com.github.phoswald.fitbit.viewer.pages.home;

import java.time.ZonedDateTime;

import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
record HomeViewModel(
        String userId,
        ZonedDateTime now
) {

    static HomeViewModel create(String userId) {
        return new HomeViewModelBuilder()
                .userId(userId)
                .now(ZonedDateTime.now())
                .build();
    }
}
