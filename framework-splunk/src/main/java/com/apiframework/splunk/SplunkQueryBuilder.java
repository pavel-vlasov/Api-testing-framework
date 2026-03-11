package com.apiframework.splunk;

import java.util.ArrayList;
import java.util.List;

/**
 * Fluent builder for constructing SPL (Search Processing Language) queries programmatically.
 * Eliminates error-prone manual string concatenation of SPL search syntax.
 *
 * <p>Example usage:
 * <pre>{@code
 * String query = SplunkQueryBuilder.search()
 *     .index("main")
 *     .source("/var/log/middleware.log")
 *     .where("correlationId", "abc-123")
 *     .spath("middlewareCorrelationId")
 *     .build();
 * // Produces: search index=main source="/var/log/middleware.log" correlationId="abc-123"
 * //           | spath output=middlewareCorrelationId path=middlewareCorrelationId
 * }</pre>
 */
public final class SplunkQueryBuilder {

    private String index;
    private String source;
    private String sourceType;
    private String host;
    private final List<String> searchTerms;
    private final List<String> pipeClauses;

    private SplunkQueryBuilder() {
        this.searchTerms = new ArrayList<>();
        this.pipeClauses = new ArrayList<>();
    }

    /**
     * Creates a new query builder. The resulting query starts with "search ".
     */
    public static SplunkQueryBuilder search() {
        return new SplunkQueryBuilder();
    }

    /**
     * Sets the index to search.
     */
    public SplunkQueryBuilder index(String index) {
        this.index = index;
        return this;
    }

    /**
     * Sets the source filter.
     */
    public SplunkQueryBuilder source(String source) {
        this.source = source;
        return this;
    }

    /**
     * Sets the sourcetype filter.
     */
    public SplunkQueryBuilder sourceType(String sourceType) {
        this.sourceType = sourceType;
        return this;
    }

    /**
     * Sets the host filter.
     */
    public SplunkQueryBuilder host(String host) {
        this.host = host;
        return this;
    }

    /**
     * Adds a field=value filter. The value is quoted in the resulting SPL.
     *
     * @param fieldName  the field name (e.g. "correlationId")
     * @param fieldValue the field value (will be SPL-quoted)
     */
    public SplunkQueryBuilder where(String fieldName, String fieldValue) {
        searchTerms.add(fieldName + "=\"" + escapeQuotes(fieldValue) + "\"");
        return this;
    }

    /**
     * Adds a quoted keyword search term.
     * Example: {@code keyword("ERROR")} produces {@code "ERROR"} in the search.
     */
    public SplunkQueryBuilder keyword(String term) {
        searchTerms.add("\"" + escapeQuotes(term) + "\"");
        return this;
    }

    /**
     * Adds an {@code | spath} extraction command where the output field name matches the path.
     * Produces: {@code | spath output=<path> path=<path>}
     *
     * @param path the JSON path to extract
     */
    public SplunkQueryBuilder spath(String path) {
        return spath(path, path);
    }

    /**
     * Adds an {@code | spath} extraction command with a custom output field name.
     * Produces: {@code | spath output=<outputField> path=<path>}
     *
     * @param path        the JSON path to extract
     * @param outputField the output field name for the extracted value
     */
    public SplunkQueryBuilder spath(String path, String outputField) {
        pipeClauses.add("spath output=" + outputField + " path=" + path);
        return this;
    }

    /**
     * Adds a {@code | rex} extraction command against the {@code _raw} field.
     * Produces: {@code | rex field=_raw "<regexPattern>"}
     *
     * @param regexPattern regex with named capture groups, e.g.
     *                     {@code "correlationId=(?<middlewareCorrId>[^\\s,}]+)"}
     */
    public SplunkQueryBuilder rex(String regexPattern) {
        return rex("_raw", regexPattern);
    }

    /**
     * Adds a {@code | rex} extraction command against a specific field.
     * Produces: {@code | rex field=<field> "<regexPattern>"}
     *
     * @param field        the field to apply the regex against
     * @param regexPattern regex with named capture groups
     */
    public SplunkQueryBuilder rex(String field, String regexPattern) {
        pipeClauses.add("rex field=" + field + " \"" + regexPattern + "\"");
        return this;
    }

    /**
     * Adds a generic pipe command. The leading {@code |} is prepended automatically.
     * Example: {@code pipe("stats count by host")} produces {@code | stats count by host}
     *
     * @param splFragment the SPL fragment to append after the pipe
     */
    public SplunkQueryBuilder pipe(String splFragment) {
        pipeClauses.add(splFragment);
        return this;
    }

    /**
     * Appends a raw SPL fragment as-is. No pipe is prepended.
     * Use for advanced query parts that don't fit the builder methods.
     *
     * @param splFragment the raw SPL fragment
     */
    public SplunkQueryBuilder raw(String splFragment) {
        searchTerms.add(splFragment);
        return this;
    }

    /**
     * Builds the final SPL query string.
     *
     * @return the complete SPL query starting with "search "
     */
    public String build() {
        StringBuilder sb = new StringBuilder("search ");

        if (index != null) {
            sb.append("index=").append(index).append(' ');
        }
        if (source != null) {
            sb.append("source=\"").append(escapeQuotes(source)).append("\" ");
        }
        if (sourceType != null) {
            sb.append("sourcetype=\"").append(escapeQuotes(sourceType)).append("\" ");
        }
        if (host != null) {
            sb.append("host=\"").append(escapeQuotes(host)).append("\" ");
        }
        for (String term : searchTerms) {
            sb.append(term).append(' ');
        }
        for (String clause : pipeClauses) {
            sb.append("| ").append(clause).append(' ');
        }

        return sb.toString().trim();
    }

    private static String escapeQuotes(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
