package com.apiframework.core.config;

import java.util.Optional;

public interface SecretsProvider {
    Optional<String> getSecret(String key);
}
