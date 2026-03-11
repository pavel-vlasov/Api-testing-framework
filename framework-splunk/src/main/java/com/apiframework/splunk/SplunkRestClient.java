package com.apiframework.splunk;

import com.apiframework.core.json.JacksonProvider;
import com.apiframework.splunk.config.SplunkConnectionConfig;
import com.apiframework.splunk.config.SplunkSearchConfig;
import com.apiframework.splunk.model.SplunkJobStatus;
import com.apiframework.splunk.model.SplunkSearchResponse;
import com.apiframework.splunk.model.SplunkSearchResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link SplunkClient} that calls the Splunk REST API directly using RestAssured.
 *
 * <p>Authentication follows the session-key model:
 * <ol>
 *     <li>POST to {@code /services/auth/login} with username/password</li>
 *     <li>Parse session key from response</li>
 *     <li>Use {@code Authorization: Splunk <sessionKey>} for all subsequent calls</li>
 *     <li>Re-login automatically on 401 (session expiry)</li>
 * </ol>
 *
 * <p>Follows the same pattern as RabbitMqClient: final class, constructor takes config record,
 * wraps all checked exceptions in IllegalStateException.
 */
public final class SplunkRestClient implements SplunkClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(SplunkRestClient.class);

    private static final String AUTH_LOGIN_ENDPOINT = "/services/auth/login";
    private static final String SEARCH_JOBS_ENDPOINT = "/services/search/jobs";
    private static final String SEARCH_EXPORT_ENDPOINT = "/services/search/jobs/export";

    private final SplunkConnectionConfig connectionConfig;
    private final SplunkSearchConfig searchConfig;
    private final ObjectMapper objectMapper;
    private volatile String sessionKey;

    /**
     * Creates a new SplunkRestClient with the given connection config and default search config.
     */
    public SplunkRestClient(SplunkConnectionConfig connectionConfig) {
        this(connectionConfig, SplunkSearchConfig.defaults());
    }

    /**
     * Creates a new SplunkRestClient with both connection and search configurations.
     */
    public SplunkRestClient(SplunkConnectionConfig connectionConfig, SplunkSearchConfig searchConfig) {
        this.connectionConfig = connectionConfig;
        this.searchConfig = searchConfig;
        this.objectMapper = JacksonProvider.defaultMapper();
    }

    @Override
    public SplunkSearchResponse searchOneShot(String splQuery, String earliestTime, String latestTime) {
        LOGGER.info("Executing one-shot Splunk search: {} [earliest={}, latest={}]",
            splQuery, earliestTime, latestTime);

        Response response = executeWithAuth(() ->
            baseRequest()
                .formParam("search", splQuery)
                .formParam("earliest_time", earliestTime)
                .formParam("latest_time", latestTime)
                .formParam("output_mode", "json")
                .post(connectionConfig.baseUrl() + SEARCH_EXPORT_ENDPOINT)
        );

        List<SplunkSearchResult> results = parseExportResults(response.getBody().asString());
        LOGGER.info("One-shot search returned {} result(s)", results.size());
        return new SplunkSearchResponse(results);
    }

    @Override
    public SplunkSearchResponse searchOneShot(String splQuery) {
        return searchOneShot(splQuery, searchConfig.defaultEarliestTime(), searchConfig.defaultLatestTime());
    }

    @Override
    public String createSearchJob(String splQuery, String earliestTime, String latestTime) {
        LOGGER.info("Creating Splunk search job: {} [earliest={}, latest={}]",
            splQuery, earliestTime, latestTime);

        Response response = executeWithAuth(() ->
            baseRequest()
                .formParam("search", splQuery)
                .formParam("earliest_time", earliestTime)
                .formParam("latest_time", latestTime)
                .formParam("output_mode", "json")
                .post(connectionConfig.baseUrl() + SEARCH_JOBS_ENDPOINT)
        );

        try {
            JsonNode root = objectMapper.readTree(response.getBody().asString());
            String sid = root.path("sid").asText();
            if (sid == null || sid.isBlank()) {
                throw new IllegalStateException("Splunk search job response did not contain a SID");
            }
            LOGGER.info("Splunk search job created with SID: {}", sid);
            return sid;
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to parse Splunk search job response", e);
        }
    }

    @Override
    public SplunkJobStatus getJobStatus(String jobSid) {
        Response response = executeWithAuth(() ->
            baseRequest()
                .queryParam("output_mode", "json")
                .get(connectionConfig.baseUrl() + SEARCH_JOBS_ENDPOINT + "/" + jobSid)
        );

        try {
            JsonNode root = objectMapper.readTree(response.getBody().asString());
            String dispatchState = root.path("entry").path(0)
                .path("content").path("dispatchState").asText();
            return SplunkJobStatus.fromDispatchState(dispatchState);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to parse Splunk job status for SID: " + jobSid, e);
        }
    }

    @Override
    public SplunkSearchResponse getJobResults(String jobSid) {
        LOGGER.info("Retrieving results for Splunk job SID: {}", jobSid);

        Response response = executeWithAuth(() ->
            baseRequest()
                .queryParam("output_mode", "json")
                .queryParam("count", 0)
                .get(connectionConfig.baseUrl() + SEARCH_JOBS_ENDPOINT + "/" + jobSid + "/results")
        );

        List<SplunkSearchResult> results = parseJobResults(response.getBody().asString());
        LOGGER.info("Job {} returned {} result(s)", jobSid, results.size());
        return new SplunkSearchResponse(results);
    }

    @Override
    public SplunkSearchResponse searchBlocking(String splQuery, String earliestTime, String latestTime) {
        String sid = createSearchJob(splQuery, earliestTime, latestTime);

        long timeoutMs = searchConfig.awaitTimeout().toMillis();
        long pollMs = searchConfig.jobPollInterval().toMillis();
        long startTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - startTime < timeoutMs) {
            SplunkJobStatus status = getJobStatus(sid);
            LOGGER.debug("Splunk job {} status: {}", sid, status);

            if (status == SplunkJobStatus.DONE) {
                return getJobResults(sid);
            }
            if (status == SplunkJobStatus.FAILED) {
                throw new IllegalStateException("Splunk search job failed. SID: " + sid);
            }

            try {
                Thread.sleep(pollMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while waiting for Splunk job: " + sid, e);
            }
        }

        throw new IllegalStateException(
            "Splunk search job timed out after " + searchConfig.awaitTimeout() + ". SID: " + sid
        );
    }

    @Override
    public SplunkSearchResponse searchBlocking(String splQuery) {
        return searchBlocking(splQuery, searchConfig.defaultEarliestTime(), searchConfig.defaultLatestTime());
    }

    @Override
    public void close() {
        LOGGER.debug("SplunkRestClient closed");
    }

    // ── Authentication ──────────────────────────────────────────────

    /**
     * Authenticates with Splunk and returns the session key.
     * POST /services/auth/login with username/password form params.
     */
    private String authenticate() {
        LOGGER.info("Authenticating with Splunk at {}", connectionConfig.baseUrl());

        RequestSpecification spec = RestAssured.given();
        if (connectionConfig.allowUntrustedSsl()) {
            spec.relaxedHTTPSValidation();
        }

        Response response = spec
            .formParam("username", connectionConfig.username())
            .formParam("password", connectionConfig.password())
            .formParam("output_mode", "json")
            .post(connectionConfig.baseUrl() + AUTH_LOGIN_ENDPOINT);

        if (response.statusCode() != 200) {
            throw new IllegalStateException(
                "Splunk authentication failed with status " + response.statusCode()
                    + ": " + response.getBody().asString()
            );
        }

        try {
            JsonNode root = objectMapper.readTree(response.getBody().asString());
            String key = root.path("sessionKey").asText();
            if (key == null || key.isBlank()) {
                throw new IllegalStateException("Splunk authentication response did not contain sessionKey");
            }
            LOGGER.info("Splunk authentication successful");
            return key;
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to parse Splunk authentication response", e);
        }
    }

    /**
     * Ensures we have a valid session key, then executes the request.
     * Re-authenticates on 401 (expired session) and retries once.
     */
    private Response executeWithAuth(RequestExecutor executor) {
        if (sessionKey == null) {
            sessionKey = authenticate();
        }

        Response response = executor.execute();

        if (response.statusCode() == 401) {
            LOGGER.info("Splunk session expired, re-authenticating");
            sessionKey = authenticate();
            response = executor.execute();
        }

        if (response.statusCode() >= 400) {
            throw new IllegalStateException(
                "Splunk REST API returned status " + response.statusCode()
                    + ": " + response.getBody().asString()
            );
        }

        return response;
    }

    /**
     * Creates a base RestAssured RequestSpecification with session-key auth and SSL config.
     */
    private RequestSpecification baseRequest() {
        RequestSpecification spec = RestAssured.given()
            .header("Authorization", "Splunk " + sessionKey);
        if (connectionConfig.allowUntrustedSsl()) {
            spec.relaxedHTTPSValidation();
        }
        return spec;
    }

    // ── Response Parsing ────────────────────────────────────────────

    /**
     * Parses NDJSON (newline-delimited JSON) from one-shot export responses.
     * Splunk's /services/search/jobs/export returns one JSON object per line.
     * Each object has a "result" field containing the event data.
     */
    private List<SplunkSearchResult> parseExportResults(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return Collections.emptyList();
        }

        List<SplunkSearchResult> results = new ArrayList<>();
        String[] lines = responseBody.split("\n");

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            try {
                JsonNode node = objectMapper.readTree(trimmed);
                if (node.has("result")) {
                    results.add(toSearchResult(node.get("result")));
                }
            } catch (Exception e) {
                LOGGER.debug("Skipping unparseable export line: {}", trimmed);
            }
        }
        return results;
    }

    /**
     * Parses the standard JSON results array from async job result responses.
     */
    private List<SplunkSearchResult> parseJobResults(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return Collections.emptyList();
        }

        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode resultsNode = root.path("results");
            if (!resultsNode.isArray()) {
                return Collections.emptyList();
            }

            List<SplunkSearchResult> results = new ArrayList<>();
            for (JsonNode node : resultsNode) {
                results.add(toSearchResult(node));
            }
            return results;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to parse Splunk job results", e);
        }
    }

    /**
     * Converts a single JSON result node into a SplunkSearchResult record.
     */
    private SplunkSearchResult toSearchResult(JsonNode resultNode) {
        String raw = resultNode.path("_raw").asText(null);
        String timeStr = resultNode.path("_time").asText(null);
        String source = resultNode.path("source").asText(null);
        String sourceType = resultNode.path("sourcetype").asText(null);
        String host = resultNode.path("host").asText(null);
        String index = resultNode.path("index").asText(null);

        Instant time = parseTime(timeStr);

        Map<String, String> fields = new LinkedHashMap<>();
        for (Map.Entry<String, JsonNode> entry : resultNode.properties()) {
            fields.put(entry.getKey(), entry.getValue().asText());
        }

        return new SplunkSearchResult(raw, time, source, sourceType, host, index,
            Collections.unmodifiableMap(fields));
    }

    private static Instant parseTime(String timeStr) {
        if (timeStr == null || timeStr.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(timeStr);
        } catch (DateTimeParseException e) {
            // Splunk may return non-ISO formats; attempt epoch seconds
            try {
                return Instant.ofEpochSecond(Long.parseLong(timeStr.split("\\.")[0]));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
    }

    @FunctionalInterface
    private interface RequestExecutor {
        Response execute();
    }
}
