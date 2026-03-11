package com.apiframework.core.config;

public enum EnvironmentProfile {
    DEV("dev"),
    STAGE("stage"),
    PROD("prod");

    private final String id;

    EnvironmentProfile(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public static EnvironmentProfile from(String raw) {
        for (EnvironmentProfile profile : values()) {
            if (profile.id.equalsIgnoreCase(raw)) {
                return profile;
            }
        }
        throw new IllegalArgumentException("Unsupported profile: " + raw);
    }
}
