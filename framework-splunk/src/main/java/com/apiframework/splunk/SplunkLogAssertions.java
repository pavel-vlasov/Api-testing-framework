package com.apiframework.splunk;

import com.apiframework.splunk.model.SplunkSearchResponse;
import com.apiframework.splunk.model.SplunkSearchResult;

/**
 * Static assertion helpers for validating Splunk log record content.
 * Follows the same pattern as CorrelationIdAssertions and OrderAssertions:
 * final class, private constructor, all static methods.
 */
public final class SplunkLogAssertions {

    private SplunkLogAssertions() {
    }

    /**
     * Asserts that the search response contains at least one result.
     *
     * @param response    the Splunk search response
     * @param queryContext the query that produced this response (included in error message for diagnostics)
     */
    public static void assertHasResults(SplunkSearchResponse response, String queryContext) {
        if (response == null || response.isEmpty()) {
            throw new AssertionError(
                "Expected Splunk results for query [" + queryContext + "] but found none"
            );
        }
    }

    /**
     * Asserts that the search response contains exactly the expected number of results.
     */
    public static void assertResultCount(SplunkSearchResponse response, int expectedCount) {
        if (response.size() != expectedCount) {
            throw new AssertionError(
                "Expected " + expectedCount + " Splunk result(s), but found " + response.size()
            );
        }
    }

    /**
     * Asserts that a specific field in a result matches the expected value.
     */
    public static void assertFieldEquals(SplunkSearchResult result, String fieldName, String expectedValue) {
        String actual = result.field(fieldName);
        if (!expectedValue.equals(actual)) {
            throw new AssertionError(
                "Expected field '" + fieldName + "' = '" + expectedValue + "', actual = '" + actual + "'"
            );
        }
    }

    /**
     * Asserts that a specific field in a result contains the expected substring.
     */
    public static void assertFieldContains(SplunkSearchResult result, String fieldName, String expectedSubstring) {
        String actual = result.field(fieldName);
        if (actual == null || !actual.contains(expectedSubstring)) {
            throw new AssertionError(
                "Expected field '" + fieldName + "' to contain '" + expectedSubstring
                    + "', actual = '" + actual + "'"
            );
        }
    }

    /**
     * Asserts that the _raw field of a result contains the given substring.
     */
    public static void assertRawContains(SplunkSearchResult result, String expectedSubstring) {
        if (result.raw() == null || !result.raw().contains(expectedSubstring)) {
            throw new AssertionError(
                "Expected _raw to contain '" + expectedSubstring + "', actual: " + result.raw()
            );
        }
    }

    /**
     * Asserts that a result came from the expected source.
     */
    public static void assertSource(SplunkSearchResult result, String expectedSource) {
        if (!expectedSource.equals(result.source())) {
            throw new AssertionError(
                "Expected source '" + expectedSource + "', actual '" + result.source() + "'"
            );
        }
    }

    /**
     * Asserts that a result came from the expected host.
     */
    public static void assertHost(SplunkSearchResult result, String expectedHost) {
        if (!expectedHost.equals(result.host())) {
            throw new AssertionError(
                "Expected host '" + expectedHost + "', actual '" + result.host() + "'"
            );
        }
    }

    /**
     * Asserts that the search response contains at least one result where
     * the given field matches the given value.
     */
    public static void assertAnyResultHasField(SplunkSearchResponse response, String fieldName, String expectedValue) {
        boolean found = response.results().stream()
            .anyMatch(r -> expectedValue.equals(r.field(fieldName)));
        if (!found) {
            throw new AssertionError(
                "No Splunk result has field '" + fieldName + "' = '" + expectedValue
                    + "' (total results: " + response.size() + ")"
            );
        }
    }
}
