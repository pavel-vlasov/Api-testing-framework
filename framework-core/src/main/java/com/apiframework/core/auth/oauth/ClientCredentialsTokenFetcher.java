package com.apiframework.core.auth.oauth;

import com.apiframework.core.json.JacksonProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public final class ClientCredentialsTokenFetcher implements OAuth2TokenFetcher {
    private final String tokenUrl;
    private final String clientId;
    private final String clientSecret;
    private final String scope;
    private final Clock clock;
    private final ObjectMapper objectMapper;

    public ClientCredentialsTokenFetcher(String tokenUrl, String clientId, String clientSecret, String scope) {
        this(tokenUrl, clientId, clientSecret, scope, Clock.systemUTC(), JacksonProvider.defaultMapper());
    }

    public ClientCredentialsTokenFetcher(
        String tokenUrl,
        String clientId,
        String clientSecret,
        String scope,
        Clock clock,
        ObjectMapper objectMapper
    ) {
        this.tokenUrl = Objects.requireNonNull(tokenUrl, "tokenUrl must not be null");
        this.clientId = Objects.requireNonNull(clientId, "clientId must not be null");
        this.clientSecret = Objects.requireNonNull(clientSecret, "clientSecret must not be null");
        this.scope = scope;
        this.clock = clock;
        this.objectMapper = objectMapper;
    }

    @Override
    public OAuthToken fetchToken() {
        Response response = RestAssured.given()
            .contentType(ContentType.URLENC)
            .formParam("grant_type", "client_credentials")
            .formParam("client_id", clientId)
            .formParam("client_secret", clientSecret)
            .formParam("scope", scope)
            .post(tokenUrl)
            .andReturn();

        if (response.statusCode() >= 400) {
            throw new IllegalStateException("Unable to obtain OAuth2 token. Status: " + response.statusCode());
        }

        try {
            JsonNode jsonNode = objectMapper.readTree(response.asString());
            String accessToken = jsonNode.path("access_token").asText();
            long expiresIn = jsonNode.path("expires_in").asLong(3600);
            return new OAuthToken(accessToken, Instant.now(clock).plusSeconds(expiresIn));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to parse OAuth2 token response", exception);
        }
    }
}
