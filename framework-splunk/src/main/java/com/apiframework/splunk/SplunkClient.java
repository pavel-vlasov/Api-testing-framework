package com.apiframework.splunk;

import com.apiframework.splunk.model.SplunkJobStatus;
import com.apiframework.splunk.model.SplunkSearchResponse;

/**
 * Interface for interacting with the Splunk REST API.
 * Follows the same interface + AutoCloseable pattern as MessageBusClient.
 *
 * <p>Provides two search execution modes:
 * <ul>
 *     <li><b>One-shot</b> — synchronous search via {@code /services/search/jobs/export},
 *         suitable for targeted queries with a moderate number of results.</li>
 *     <li><b>Async job</b> — creates a search job, polls for completion, then retrieves results.
 *         Suitable for larger or long-running searches.</li>
 * </ul>
 */
public interface SplunkClient extends AutoCloseable {

    /**
     * Executes a one-shot search using {@code /services/search/jobs/export}.
     * Blocks until all results are returned.
     *
     * @param splQuery     the SPL search query string
     * @param earliestTime earliest time bound (e.g. "-15m", "2024-01-01T00:00:00")
     * @param latestTime   latest time bound (e.g. "now")
     * @return search response containing all matching results
     */
    SplunkSearchResponse searchOneShot(String splQuery, String earliestTime, String latestTime);

    /**
     * Executes a one-shot search using default time bounds from {@code SplunkSearchConfig}.
     *
     * @param splQuery the SPL search query string
     * @return search response containing all matching results
     */
    SplunkSearchResponse searchOneShot(String splQuery);

    /**
     * Creates an asynchronous search job via {@code /services/search/jobs}.
     * Returns the job SID (search ID) for subsequent polling.
     *
     * @param splQuery     the SPL search query string
     * @param earliestTime earliest time bound
     * @param latestTime   latest time bound
     * @return the search job SID (string identifier)
     */
    String createSearchJob(String splQuery, String earliestTime, String latestTime);

    /**
     * Retrieves the current status of a search job by its SID.
     *
     * @param jobSid the search job SID
     * @return the current job status
     */
    SplunkJobStatus getJobStatus(String jobSid);

    /**
     * Retrieves the results of a completed search job.
     * Should only be called after the job reaches {@link SplunkJobStatus#DONE} status.
     *
     * @param jobSid the search job SID
     * @return search response containing the job's results
     */
    SplunkSearchResponse getJobResults(String jobSid);

    /**
     * Executes an async search job and blocks until it completes, then returns results.
     * Combines {@link #createSearchJob}, status polling, and {@link #getJobResults} into one call.
     *
     * @param splQuery     the SPL search query string
     * @param earliestTime earliest time bound
     * @param latestTime   latest time bound
     * @return search response containing the completed job's results
     */
    SplunkSearchResponse searchBlocking(String splQuery, String earliestTime, String latestTime);

    /**
     * Executes an async search job with default time bounds and blocks until completion.
     *
     * @param splQuery the SPL search query string
     * @return search response containing the completed job's results
     */
    SplunkSearchResponse searchBlocking(String splQuery);

    @Override
    void close();
}
