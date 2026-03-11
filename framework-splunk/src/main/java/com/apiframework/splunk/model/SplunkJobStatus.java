package com.apiframework.splunk.model;

import java.util.Locale;

/**
 * Represents the lifecycle states of a Splunk search job.
 * Maps to the "dispatchState" field in the Splunk REST API response.
 */
public enum SplunkJobStatus {

    QUEUED,
    PARSING,
    RUNNING,
    PAUSED,
    FINALIZING,
    DONE,
    FAILED,
    UNKNOWN;

    /**
     * Parses the Splunk dispatchState string into this enum.
     * Returns {@link #UNKNOWN} for unrecognized values.
     */
    public static SplunkJobStatus fromDispatchState(String dispatchState) {
        if (dispatchState == null || dispatchState.isBlank()) {
            return UNKNOWN;
        }
        try {
            return valueOf(dispatchState.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return UNKNOWN;
        }
    }

    /**
     * Returns {@code true} if the job has reached a terminal state ({@link #DONE} or {@link #FAILED}).
     */
    public boolean isTerminal() {
        return this == DONE || this == FAILED;
    }
}
