package com.apiframework.splunk.config;

import com.apiframework.core.config.EnvSecretsProvider;
import com.apiframework.core.config.SecretsProvider;

import java.util.List;
import java.util.Optional;

/**
 * Connection parameters for the Splunk REST API.
 * Follows the same record-based config pattern as RabbitMqConnectionConfig and OracleConnectionConfig.
 *
 * @param baseUrl          Splunk management REST API base URL, e.g. "https://splunk.example.com:8089"
 * @param username         Splunk login username
 * @param password         Splunk login password
 * @param allowUntrustedSsl whether to relax SSL validation (common for Splunk self-signed certs)
 */
public record SplunkConnectionConfig(
    String baseUrl,
    String username,
    String password,
    boolean allowUntrustedSsl
) {

    /**
     * Resolves config from system properties and the given SecretsProvider chain.
     * Password is resolved via the same SecretsProvider pattern used by ConfigResolver
     * for OAuth2/Basic credentials.
     *
     * <p>System properties:
     * <ul>
     *     <li>{@code splunk.baseUrl} — Splunk REST API base URL</li>
     *     <li>{@code splunk.username} — Splunk login username</li>
     *     <li>{@code splunk.allowUntrustedSsl} — whether to allow untrusted SSL (default: true)</li>
     * </ul>
     *
     * <p>Secret key: {@code splunk.password} — resolved via providers
     * (e.g. env var {@code FRAMEWORK_SECRET_SPLUNK_PASSWORD}), with fallback to system property.
     */
    public static SplunkConnectionConfig fromSystem(List<SecretsProvider> providers) {
        String password = resolveSecret("splunk.password", providers)
            .orElseGet(() -> System.getProperty("splunk.password", ""));

        return new SplunkConnectionConfig(
            System.getProperty("splunk.baseUrl", "https://localhost:8089"),
            System.getProperty("splunk.username", ""),
            password,
            Boolean.parseBoolean(System.getProperty("splunk.allowUntrustedSsl", "true"))
        );
    }

    /**
     * Resolves config from system properties using the default EnvSecretsProvider.
     */
    public static SplunkConnectionConfig fromSystem() {
        return fromSystem(List.of(new EnvSecretsProvider()));
    }

    private static Optional<String> resolveSecret(String key, List<SecretsProvider> providers) {
        return providers.stream()
            .map(provider -> provider.getSecret(key))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
    }
}
