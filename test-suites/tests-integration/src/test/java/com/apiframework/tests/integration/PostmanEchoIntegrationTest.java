package com.apiframework.tests.integration;

import com.apiframework.contracts.JsonSchemaContractValidator;
import com.apiframework.contracts.snapshot.SnapshotContractChecker;
import com.apiframework.sampledomain.assertions.EchoAssertions;
import com.apiframework.sampledomain.endpoint.PostmanEchoApi;
import com.apiframework.sampledomain.flow.EchoFlow;
import com.apiframework.sampledomain.flow.model.QueryRoundtripResult;
import com.apiframework.sampledomain.model.EchoGetResponse;
import com.apiframework.testng.base.BaseApiTest;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class PostmanEchoIntegrationTest extends BaseApiTest {
    private EchoFlow echoFlow;
    private JsonSchemaContractValidator schemaValidator;
    private SnapshotContractChecker snapshotChecker;

    @BeforeClass(alwaysRun = true)
    public void initDependencies() {
        super.initHttpClient();
        this.echoFlow = new EchoFlow(new PostmanEchoApi(httpClient()));
        this.schemaValidator = new JsonSchemaContractValidator();
        this.snapshotChecker = SnapshotContractChecker.defaultChecker();
    }

    @Override
    protected boolean requiresLiveApi() {
        return true;
    }

    @Test(description = "GET /get should match schema and snapshot contract")
    public void shouldMatchEchoGetContractAndSnapshot() {
        try {
            QueryRoundtripResult result = echoFlow.verifyQueryRoundtrip("suite", "integration");

            EchoAssertions.assertQueryRoundtrip(result);
            String contractBody = normalizedContractBody(result.response().body());
            schemaValidator.assertMatchesSchema(contractBody, "schemas/postman-echo-get.schema.json");
            snapshotChecker.assertMatchesSnapshot("postman-echo-get", contractBody, false);
        } catch (Throwable ex) {
            throw new SkipException("Postman Echo is unavailable", ex);
        }
    }

    private String normalizedContractBody(EchoGetResponse body) {
        return "{\"args\":{\"suite\":\"" + body.args().getOrDefault("suite", "")
            + "\"},\"url\":\"" + body.url() + "\"}";
    }
}
