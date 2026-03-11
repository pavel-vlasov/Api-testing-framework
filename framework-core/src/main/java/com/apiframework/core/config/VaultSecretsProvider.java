package com.apiframework.core.config;

import java.util.Map;
import java.util.Optional;

public final class VaultSecretsProvider implements SecretsProvider {
    private final Map<String, String> vaultData;

    public VaultSecretsProvider(Map<String, String> vaultData) {
        this.vaultData = Map.copyOf(vaultData);
    }

    @Override
    public Optional<String> getSecret(String key) {
        return Optional.ofNullable(vaultData.get(key));
    }
}
