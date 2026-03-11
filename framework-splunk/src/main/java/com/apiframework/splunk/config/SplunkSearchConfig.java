package com.apiframework.splunk.config;

import java.time.Duration;

/**
 * Default search timing parameters used by the awaiter and search execution.
 *
 * @param defaultEarliestTime SPL earliest_time parameter, e.g. "-15m"
 * @param defaultLatestTime   SPL latest_time parameter, e.g. "now"
 * @param awaitTimeout        maximum duration to wait for search results to appear
 * @param awaitPollInterval   interval between polling attempts when awaiting results
 * @param jobPollInterval     interval between job status polls for async searches
 */
public record SplunkSearchConfig(
    String defaultEarliestTime,
    String defaultLatestTime,
    Duration awaitTimeout,
    Duration awaitPollInterval,
    Duration jobPollInterval
) {

    /**
     * Sensible defaults: search last 15 minutes, await up to 60s, poll every 3s.
     */
    public static SplunkSearchConfig defaults() {
        return new SplunkSearchConfig(
            "-15m",
            "now",
            Duration.ofSeconds(60),
            Duration.ofSeconds(3),
            Duration.ofSeconds(2)
        );
    }
}
