package com.apiframework.splunk;

import java.util.UUID;

/**
 * Generates prefixed correlation IDs for test tracing in Splunk.
 *
 * <p>The existing {@code CorrelationIdFilter} in framework-core auto-generates a plain UUID
 * when no header is set. This factory lets a test create a <b>prefixed</b> ID before sending
 * the request, so the same value can be used for both the HTTP header and the subsequent Splunk query.
 *
 * <p>When a correlation ID is pre-set via {@code ApiRequest.builder().header(HEADER_NAME, corrId)},
 * the {@code CorrelationIdFilter} skips UUID generation and preserves the test's prefixed ID.
 *
 * <p>Example usage:
 * <pre>{@code
 * String corrId = TestCorrelationIdFactory.generate("order-chain");
 * // → "order-chain-a1b2c3d4-e5f6-7890-abcd-ef1234567890"
 *
 * ApiRequest<Foo> req = ApiRequest.<Foo>builder(HttpMethod.POST, "/api/orders")
 *     .header(CorrelationIdFilter.HEADER_NAME, corrId)
 *     .body(payload)
 *     .build();
 *
 * // Later, search Splunk using the same corrId
 * String query = SplunkQueryBuilder.search()
 *     .index("main")
 *     .keyword(corrId)
 *     .build();
 * }</pre>
 */
public final class TestCorrelationIdFactory {

    private TestCorrelationIdFactory() {
    }

    /**
     * Generates a correlation ID with the given prefix and a UUID suffix.
     * The prefix makes the ID uniquely searchable in Splunk.
     *
     * @param prefix a test-specific prefix, e.g. "order-test", "registration-flow"
     * @return a prefixed correlation ID, e.g. "order-test-a1b2c3d4-..."
     */
    public static String generate(String prefix) {
        return prefix + "-" + UUID.randomUUID();
    }

    /**
     * Generates a correlation ID with the default "test" prefix.
     *
     * @return a prefixed correlation ID, e.g. "test-a1b2c3d4-..."
     */
    public static String generate() {
        return generate("test");
    }
}
