package com.github.phoswald.fitbit.viewer.auth;

import com.github.phoswald.record.builder.RecordBuilder;

@RecordBuilder
public record SessionData(
        String accessToken,
        Long expiresAt,
        String userId
) { }
