package com.apiframework.splunk.model;

import java.time.Instant;
import java.util.Map;

/**
 * Represents a single log record returned from a Splunk search.
 * Maps to one row/event in the Splunk search results JSON.
 * Follows the same model record pattern as ConsumedMessage and DbOrder.
 *
 * @param raw        the _raw field (full log line text)
 * @param time       the _time field parsed as Instant
 * @param source     the source field (log source path/identifier)
 * @param sourceType the sourcetype field
 * @param host       the host field
 * @param index      the index field
 * @param fields     all fields returned by the search as a string-keyed map
 */
public record SplunkSearchResult(
    String raw,
    Instant time,
    String source,
    String sourceType,
    String host,
    String index,
    Map<String, String> fields
) {

    /**
     * Convenience accessor for an arbitrary field by name from the fields map.
     *
     * @param fieldName the field name to look up
     * @return the field value, or {@code null} if the field does not exist
     */
    public String field(String fieldName) {
        return fields.get(fieldName);
    }
}
