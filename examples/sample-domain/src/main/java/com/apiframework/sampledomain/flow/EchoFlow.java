package com.apiframework.sampledomain.flow;

import com.apiframework.core.model.ApiResponse;
import com.apiframework.reporting.steps.AllureActionExecutor;
import com.apiframework.sampledomain.endpoint.PostmanEchoApi;
import com.apiframework.sampledomain.flow.model.PayloadRoundtripResult;
import com.apiframework.sampledomain.flow.model.QueryRoundtripResult;
import com.apiframework.sampledomain.model.EchoGetResponse;
import com.apiframework.sampledomain.model.EchoPayload;
import com.apiframework.sampledomain.model.EchoPostResponse;

import static org.assertj.core.api.Assertions.assertThat;

public final class EchoFlow {
    private final PostmanEchoApi echoApi;
    private final AllureActionExecutor stepExecutor;

    public EchoFlow(PostmanEchoApi echoApi) {
        this(echoApi, new AllureActionExecutor());
    }

    public EchoFlow(PostmanEchoApi echoApi, AllureActionExecutor stepExecutor) {
        this.echoApi = echoApi;
        this.stepExecutor = stepExecutor;
    }

    public QueryRoundtripResult verifyQueryRoundtrip(String key, String value) {
        return stepExecutor.composite("Verify query roundtrip", () -> {
            ApiResponse<EchoGetResponse> response = stepExecutor.action(
                "GET /get with query parameter",
                () -> echoApi.getEcho(key, value)
            );
            stepExecutor.assertion("Verify transport success", () -> assertThat(response.statusCode()).isEqualTo(200));
            return new QueryRoundtripResult(key, value, response);
        });
    }

    public PayloadRoundtripResult sendPayloadAndVerifyRoundtrip(EchoPayload payload) {
        return stepExecutor.composite("Verify payload roundtrip", () -> {
            ApiResponse<EchoPostResponse> response = stepExecutor.action(
                "POST /post with payload",
                () -> echoApi.postEcho(payload)
            );
            stepExecutor.assertion("Verify transport success", () -> assertThat(response.statusCode()).isEqualTo(200));
            return new PayloadRoundtripResult(payload, response);
        });
    }

    public ApiResponse<EchoPostResponse> sendPayload(EchoPayload payload) {
        return sendPayloadAndVerifyRoundtrip(payload).response();
    }
}
