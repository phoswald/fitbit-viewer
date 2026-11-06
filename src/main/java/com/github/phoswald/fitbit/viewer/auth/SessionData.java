package com.github.phoswald.fitbit.viewer.auth;

import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
public record SessionData(
        String accessToken,
        Long expiresAt,
        String userId,
        String userFullName,
        String userPictureUrl
) {

    public static SessionDataBuilder builder() {
        return new SessionDataBuilder();
    }

    public SessionDataBuilder toBuilder() {
        return new SessionDataBuilder(this);
    }
}
