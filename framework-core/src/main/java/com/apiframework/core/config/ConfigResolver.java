package com.apiframework.core.config;

import com.apiframework.core.model.HttpRetryPolicy;
import org.aeonbits.owner.ConfigFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class ConfigResolver {
    private ConfigResolver() {
    }

    public static FrameworkRuntimeConfig resolveFromSystem() {
        String rawProfile = System.getProperty("framework.profile", "dev");
        return resolve(EnvironmentProfile.from(rawProfile), List.of(new EnvSecretsProvider()));
    }

    public static FrameworkRuntimeConfig resolve(EnvironmentProfile profile, List<SecretsProvider> providers) {
        ConfigFactory.setProperty("framework.profile", profile.id());
        FrameworkProperties properties = ConfigFactory.create(FrameworkProperties.class, System.getProperties());

        List<SecretsProvider> chain = new ArrayList<>();
        chain.addAll(providers);
        chain.add(new EnvSecretsProvider());

        String basicUsername = resolveSecret(properties.basicUsernameSecretKey(), chain)
            .orElseGet(() -> System.getProperty("auth.basic.username", ""));
        String basicPassword = resolveSecret(properties.basicPasswordSecretKey(), chain)
            .orElseGet(() -> System.getProperty("auth.basic.password", ""));

        String oauth2ClientSecret = resolveSecret(properties.oauth2ClientSecretKey(), chain)
            .orElseGet(() -> System.getProperty("auth.oauth2.clientSecret", ""));
        String oauth2Password = resolveSecret(properties.oauth2PasswordSecretKey(), chain)
            .orElseGet(() -> System.getProperty("auth.oauth2.password", ""));

        HttpRetryPolicy retryPolicy = new HttpRetryPolicy(
            properties.retryMaxAttempts(),
            Duration.ofMillis(properties.retryDelayMs()),
            Set.of(429, 500, 502, 503, 504)
        );

        return new FrameworkRuntimeConfig(
            profile,
            properties.apiBaseUrl(),
            properties.connectTimeoutMs(),
            properties.readTimeoutMs(),
            retryPolicy,
            new BasicAuthConfig(basicUsername, basicPassword),
            new OAuth2Config(
                properties.oauth2TokenUrl(),
                properties.oauth2ClientId(),
                oauth2ClientSecret,
                properties.oauth2Username(),
                oauth2Password,
                properties.oauth2Scope(),
                Duration.ofSeconds(properties.oauth2RefreshSkewSeconds())
            )
        );
    }

    @SafeVarargs
    public static FrameworkRuntimeConfig resolve(EnvironmentProfile profile, SecretsProvider... providers) {
        return resolve(profile, Arrays.asList(providers));
    }

    private static Optional<String> resolveSecret(String key, List<SecretsProvider> providers) {
        return providers.stream()
            .map(provider -> provider.getSecret(key))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
    }
}
