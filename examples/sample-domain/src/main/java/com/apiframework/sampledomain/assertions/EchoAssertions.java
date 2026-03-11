package com.apiframework.sampledomain.assertions;

import com.apiframework.core.model.ApiResponse;
import com.apiframework.reporting.steps.AllureActionExecutor;
import com.apiframework.sampledomain.flow.model.PayloadRoundtripResult;
import com.apiframework.sampledomain.flow.model.QueryRoundtripResult;
import com.apiframework.sampledomain.model.EchoGetResponse;
import com.apiframework.sampledomain.model.EchoPostResponse;

import static org.assertj.core.api.Assertions.assertThat;

public final class EchoAssertions {
    private static final AllureActionExecutor EXECUTOR = new AllureActionExecutor();

    private EchoAssertions() {
    }

    public static void assertQueryRoundtrip(QueryRoundtripResult result) {
        EXECUTOR.assertion("Verify echoed query matches expected", () -> {
            ApiResponse<EchoGetResponse> response = result.response();
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.body()).isNotNull();
            assertThat(response.body().args()).containsEntry(result.key(), result.expectedValue());
        });
    }

    public static void assertPayloadRoundtrip(PayloadRoundtripResult result) {
        EXECUTOR.assertion("Verify echoed payload matches expected", () -> {
            ApiResponse<EchoPostResponse> response = result.response();
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.body()).isNotNull();
            assertThat(response.body().json()).isNotNull();
            assertThat(response.body().json().event()).isEqualTo(result.payload().event());
            assertThat(response.body().json().amount()).isEqualTo(result.payload().amount());
            assertThat(response.body().json().active()).isEqualTo(result.payload().active());
        });
    }

    public static void assertGetEcho(ApiResponse<EchoGetResponse> response, String key, String value) {
        assertQueryRoundtrip(new QueryRoundtripResult(key, value, response));
    }

    public static void assertPostEcho(ApiResponse<EchoPostResponse> response, String event, int amount) {
        EXECUTOR.assertion("Verify echoed payload fields", () -> {
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.body()).isNotNull();
            assertThat(response.body().json()).isNotNull();
            assertThat(response.body().json().event()).isEqualTo(event);
            assertThat(response.body().json().amount()).isEqualTo(amount);
        });
    }
}
