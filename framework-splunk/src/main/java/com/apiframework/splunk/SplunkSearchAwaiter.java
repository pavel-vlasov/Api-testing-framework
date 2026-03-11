package com.apiframework.splunk;

import com.apiframework.splunk.config.SplunkSearchConfig;
import com.apiframework.splunk.model.SplunkSearchResponse;
import org.awaitility.Awaitility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

/**
 * Polls Splunk searches until results matching a predicate appear.
 * Handles Splunk indexing latency — results may not be available immediately after an event.
 *
 * <p>Follows the same Awaitility + AtomicReference pattern as MessageAwaiter.
 */
public final class SplunkSearchAwaiter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SplunkSearchAwaiter.class);

    private final SplunkClient splunkClient;
    private final SplunkSearchConfig searchConfig;

    /**
     * Creates an awaiter with the given client and search config.
     */
    public SplunkSearchAwaiter(SplunkClient splunkClient, SplunkSearchConfig searchConfig) {
        this.splunkClient = splunkClient;
        this.searchConfig = searchConfig;
    }

    /**
     * Creates an awaiter using the default SplunkSearchConfig.
     */
    public SplunkSearchAwaiter(SplunkClient splunkClient) {
        this(splunkClient, SplunkSearchConfig.defaults());
    }

    /**
     * Repeatedly executes a one-shot search until at least one result matches the predicate.
     *
     * @param splQuery     SPL query to execute
     * @param earliestTime earliest time bound for the search
     * @param latestTime   latest time bound for the search
     * @param timeout      maximum time to wait for matching results
     * @param pollInterval interval between search attempts
     * @param predicate    predicate applied to the SplunkSearchResponse to determine success
     * @return search response containing results that satisfied the predicate
     */
    public SplunkSearchResponse awaitResults(
        String splQuery,
        String earliestTime,
        String latestTime,
        Duration timeout,
        Duration pollInterval,
        Predicate<SplunkSearchResponse> predicate
    ) {
        LOGGER.info("Awaiting Splunk results for query: {} [timeout={}, poll={}]",
            splQuery, timeout, pollInterval);

        AtomicReference<SplunkSearchResponse> captured = new AtomicReference<>();

        Awaitility.await("Splunk search: " + splQuery)
            .atMost(timeout)
            .pollInterval(pollInterval)
            .pollInSameThread()
            .until(() -> {
                SplunkSearchResponse response = splunkClient.searchOneShot(splQuery, earliestTime, latestTime);
                if (predicate.test(response)) {
                    captured.set(response);
                    return true;
                }
                LOGGER.debug("Splunk search returned {} result(s), predicate not yet satisfied",
                    response.size());
                return false;
            });

        LOGGER.info("Splunk await completed with {} result(s)", captured.get().size());
        return captured.get();
    }

    /**
     * Awaits results using default timeout and poll interval from SplunkSearchConfig.
     */
    public SplunkSearchResponse awaitResults(
        String splQuery,
        String earliestTime,
        String latestTime,
        Predicate<SplunkSearchResponse> predicate
    ) {
        return awaitResults(
            splQuery, earliestTime, latestTime,
            searchConfig.awaitTimeout(), searchConfig.awaitPollInterval(),
            predicate
        );
    }

    /**
     * Awaits results using all defaults from SplunkSearchConfig (including time bounds).
     */
    public SplunkSearchResponse awaitResults(
        String splQuery,
        Predicate<SplunkSearchResponse> predicate
    ) {
        return awaitResults(
            splQuery,
            searchConfig.defaultEarliestTime(),
            searchConfig.defaultLatestTime(),
            predicate
        );
    }

    /**
     * Awaits search results that are non-empty (at least one result exists).
     * Convenience method for the most common use case.
     */
    public SplunkSearchResponse awaitNonEmpty(String splQuery) {
        return awaitResults(splQuery, response -> !response.isEmpty());
    }

    /**
     * Awaits search results and returns only those where the given field matches the expected value.
     *
     * @param splQuery   SPL query to execute
     * @param fieldName  field name to filter on (e.g. "correlationId")
     * @param fieldValue expected field value
     * @return search response containing only matching results
     */
    public SplunkSearchResponse awaitResultsWithField(
        String splQuery,
        String fieldName,
        String fieldValue
    ) {
        SplunkSearchResponse response = awaitResults(
            splQuery,
            resp -> !resp.filterByField(fieldName, fieldValue).isEmpty()
        );
        return response.filterByField(fieldName, fieldValue);
    }
}
