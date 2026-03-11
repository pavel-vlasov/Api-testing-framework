package com.apiframework.contracts;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

public final class JsonUnitContractAssertions {
    private JsonUnitContractAssertions() {
    }

    public static void assertJsonEquals(String actualJson, String expectedJson, String... ignoredPaths) {
        var assertion = assertThatJson(actualJson);
        if (ignoredPaths != null && ignoredPaths.length > 0) {
            assertion = assertion.whenIgnoringPaths(ignoredPaths);
        }
        assertion.isEqualTo(expectedJson);
    }
}
