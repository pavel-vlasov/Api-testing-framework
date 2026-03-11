package com.apiframework.splunk.model;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Wrapper around a list of Splunk search results with chainable filtering and extraction utilities.
 * Returned by all search operations on {@link com.apiframework.splunk.SplunkClient}.
 *
 * @param results the ordered list of search result records
 */
public record SplunkSearchResponse(List<SplunkSearchResult> results) {

    /**
     * Returns {@code true} if the search produced no results.
     */
    public boolean isEmpty() {
        return results.isEmpty();
    }

    /**
     * Returns the number of results.
     */
    public int size() {
        return results.size();
    }

    /**
     * Returns a new {@code SplunkSearchResponse} containing only results matching the predicate.
     */
    public SplunkSearchResponse filter(Predicate<SplunkSearchResult> predicate) {
        List<SplunkSearchResult> filtered = results.stream()
            .filter(predicate)
            .collect(Collectors.toList());
        return new SplunkSearchResponse(filtered);
    }

    /**
     * Returns a new response containing only results where the {@code source} field
     * equals the given value (case-insensitive).
     */
    public SplunkSearchResponse filterBySource(String source) {
        return filter(result -> source.equalsIgnoreCase(result.source()));
    }

    /**
     * Returns a new response containing only results where the {@code host} field
     * equals the given value (case-insensitive).
     */
    public SplunkSearchResponse filterByHost(String host) {
        return filter(result -> host.equalsIgnoreCase(result.host()));
    }

    /**
     * Returns a new response containing only results where the given field name
     * equals the given value.
     */
    public SplunkSearchResponse filterByField(String fieldName, String fieldValue) {
        return filter(result -> fieldValue.equals(result.field(fieldName)));
    }

    /**
     * Returns the first result, or empty if no results.
     */
    public Optional<SplunkSearchResult> first() {
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * Returns the first result, throwing {@link AssertionError} if no results exist.
     * Useful in test assertions where at least one result is expected.
     */
    public SplunkSearchResult firstOrFail() {
        return first().orElseThrow(
            () -> new AssertionError("Expected at least one Splunk search result, but found none")
        );
    }
}
