package com.apiframework.core.config;

import java.util.Locale;
import java.util.Optional;

public final class EnvSecretsProvider implements SecretsProvider {
    private final String prefix;

    public EnvSecretsProvider() {
        this("FRAMEWORK_SECRET_");
    }

    public EnvSecretsProvider(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public Optional<String> getSecret(String key) {
        String normalized = key.toUpperCase(Locale.ROOT).replace('.', '_');
        return Optional.ofNullable(System.getenv(prefix + normalized));
    }
}
