package com.apiframework.splunk;

import com.apiframework.core.json.JacksonProvider;
import com.apiframework.splunk.model.SplunkSearchResult;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Maps Splunk search result content to typed Java objects.
 * Follows the same pattern as MessagePayloadMapper: wraps JacksonProvider.defaultMapper(),
 * provides generic typed mapping.
 */
public final class SplunkResultMapper {

    private final ObjectMapper objectMapper;

    public SplunkResultMapper() {
        this.objectMapper = JacksonProvider.defaultMapper();
    }

    /**
     * Deserializes the {@code _raw} field of a SplunkSearchResult into the given target class.
     * Useful when {@code _raw} contains a structured JSON log payload.
     *
     * @param result      the Splunk search result whose _raw field will be parsed
     * @param targetClass the class to deserialize into
     * @param <T>         the target type
     * @return deserialized object of type T
     * @throws IllegalStateException if deserialization fails
     */
    public <T> T mapRaw(SplunkSearchResult result, Class<T> targetClass) {
        String raw = result.raw();
        if (raw == null || raw.isBlank()) {
            throw new IllegalStateException("Splunk result _raw field is empty, cannot map to "
                + targetClass.getSimpleName());
        }
        try {
            return objectMapper.readValue(raw, targetClass);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to map Splunk _raw field to "
                + targetClass.getSimpleName(), e);
        }
    }

    /**
     * Deserializes a specific field's value from the result's fields map into the given target class.
     *
     * @param result      the Splunk search result
     * @param fieldName   the field name whose value should be deserialized
     * @param targetClass the class to deserialize into
     * @param <T>         the target type
     * @return deserialized object of type T
     * @throws IllegalStateException if the field does not exist or deserialization fails
     */
    public <T> T mapField(SplunkSearchResult result, String fieldName, Class<T> targetClass) {
        String value = result.field(fieldName);
        if (value == null) {
            throw new IllegalStateException("Field '" + fieldName + "' not found in Splunk result");
        }
        try {
            return objectMapper.readValue(value, targetClass);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to map Splunk field '" + fieldName + "' to "
                + targetClass.getSimpleName(), e);
        }
    }

    /**
     * Deserializes a JSON string directly into the given target class.
     * Provided for consistency with MessagePayloadMapper.map().
     *
     * @param json        raw JSON string
     * @param targetClass the class to deserialize into
     * @param <T>         the target type
     * @return deserialized object of type T
     * @throws IllegalStateException if deserialization fails
     */
    public <T> T map(String json, Class<T> targetClass) {
        try {
            return objectMapper.readValue(json, targetClass);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to map JSON to " + targetClass.getSimpleName(), e);
        }
    }
}
