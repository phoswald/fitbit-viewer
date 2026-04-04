package com.github.phoswald.fitbit.viewer.login;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.json.Json;

class JwtUtil {

    private static final Pattern JWT_PATTERN = Pattern.compile("([A-Za-z0-9_-]+)\\.([A-Za-z0-9_-]+)\\.([A-Za-z0-9_-]+)");

    static String getSubject(String accessToken) {
        Matcher matcher = JWT_PATTERN.matcher(accessToken);
        if(matcher.matches()) {
            byte[] payload = Base64.getUrlDecoder().decode(matcher.group(2));
            try (var reader = Json.createReader(new ByteArrayInputStream(payload))) {
                return reader.readObject().getString("sub");
            }
        } else {
            return null;
        }
    }
}
