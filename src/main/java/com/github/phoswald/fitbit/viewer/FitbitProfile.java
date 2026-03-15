package com.github.phoswald.fitbit.viewer;

public record FitbitProfile(
        String displayName,
        String fullName,
        int age,
        String gender,
        String avatarUrl
) {
}
