package com.apiframework.core.config;

import org.aeonbits.owner.Config;

@Config.Sources({
    "classpath:application-${framework.profile}.properties",
    "classpath:application.properties"
})
public interface FrameworkProperties extends Config {

    @Key("api.baseUrl")
    @DefaultValue("https://api.example.local")
    String apiBaseUrl();

    @Key("http.connectTimeoutMs")
    @DefaultValue("5000")
    int connectTimeoutMs();

    @Key("http.readTimeoutMs")
    @DefaultValue("15000")
    int readTimeoutMs();

    @Key("http.retry.maxAttempts")
    @DefaultValue("1")
    int retryMaxAttempts();

    @Key("http.retry.delayMs")
    @DefaultValue("0")
    long retryDelayMs();

    @Key("oauth2.refreshSkewSeconds")
    @DefaultValue("30")
    long oauth2RefreshSkewSeconds();

    @Key("auth.basic.usernameSecretKey")
    @DefaultValue("auth.basic.username")
    String basicUsernameSecretKey();

    @Key("auth.basic.passwordSecretKey")
    @DefaultValue("auth.basic.password")
    String basicPasswordSecretKey();

    @Key("auth.oauth2.tokenUrl")
    @DefaultValue("https://auth.example.local/oauth/token")
    String oauth2TokenUrl();

    @Key("auth.oauth2.clientId")
    @DefaultValue("example-client")
    String oauth2ClientId();

    @Key("auth.oauth2.clientSecretKey")
    @DefaultValue("auth.oauth2.clientSecret")
    String oauth2ClientSecretKey();

    @Key("auth.oauth2.username")
    @DefaultValue("api-user")
    String oauth2Username();

    @Key("auth.oauth2.passwordSecretKey")
    @DefaultValue("auth.oauth2.password")
    String oauth2PasswordSecretKey();

    @Key("auth.oauth2.scope")
    @DefaultValue("openid profile")
    String oauth2Scope();
}
