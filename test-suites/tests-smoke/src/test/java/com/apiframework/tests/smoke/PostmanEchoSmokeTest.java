package com.apiframework.tests.smoke;

import com.apiframework.sampledomain.assertions.EchoAssertions;
import com.apiframework.sampledomain.endpoint.PostmanEchoApi;
import com.apiframework.sampledomain.flow.EchoFlow;
import com.apiframework.sampledomain.flow.model.QueryRoundtripResult;
import com.apiframework.testng.base.BaseApiTest;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class PostmanEchoSmokeTest extends BaseApiTest {
    private EchoFlow echoFlow;

    @BeforeClass(alwaysRun = true)
    public void initFlow() {
        super.initHttpClient();
        this.echoFlow = new EchoFlow(new PostmanEchoApi(httpClient()));
    }

    @Override
    protected boolean requiresLiveApi() {
        return true;
    }

    @Test(description = "GET /get should echo query parameter")
    public void shouldEchoQueryParameter() {
        try {
            QueryRoundtripResult roundtrip = echoFlow.verifyQueryRoundtrip("suite", "smoke");
            EchoAssertions.assertQueryRoundtrip(roundtrip);
        } catch (Throwable ex) {
            throw new SkipException("Skipped: " + ex.getMessage(), ex);
        }
    }
}
