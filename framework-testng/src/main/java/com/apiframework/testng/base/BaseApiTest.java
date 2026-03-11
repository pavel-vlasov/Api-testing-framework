package com.apiframework.testng.base;

import com.apiframework.core.auth.AuthStrategy;
import com.apiframework.core.client.ApiClientFactory;
import com.apiframework.core.config.ConfigResolver;
import com.apiframework.core.config.FrameworkRuntimeConfig;
import com.apiframework.core.filter.CorrelationIdFilter;
import com.apiframework.core.filter.FilterPolicyProvider;
import com.apiframework.core.filter.HttpFilterPolicy;
import com.apiframework.core.http.HttpClient;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.UUID;
import java.util.function.Function;

public abstract class BaseApiTest {
    public static final String TEST_CONTEXT_ATTRIBUTE = "framework.test.context";

    protected FrameworkRuntimeConfig runtimeConfig;
    protected HttpClient httpClient;
    protected TestExecutionContext testContext;

    @BeforeSuite(alwaysRun = true)
    public void initRuntimeConfig() {
        this.runtimeConfig = ConfigResolver.resolveFromSystem();
    }

    @BeforeClass(alwaysRun = true)
    public void initHttpClient() {
        if (runtimeConfig == null) {
            initRuntimeConfig();
        }

        if (requiresLiveApi() && !Boolean.parseBoolean(System.getProperty("framework.runLiveTests", "false"))) {
            throw new SkipException("Live API tests are disabled. Set -Dframework.runLiveTests=true");
        }

        this.httpClient = ApiClientFactory.create(runtimeConfig, authStrategy(), filterPolicy());
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeEach(ITestResult result) {
        this.testContext = new TestExecutionContext(
            result.getTestClass().getName() + "." + result.getMethod().getMethodName(),
            UUID.randomUUID().toString(),
            Instant.now(),
            environmentTags()
        );

        result.setAttribute(TEST_CONTEXT_ATTRIBUTE, testContext);
        result.setAttribute(CorrelationIdFilter.HEADER_NAME, testContext.correlationId());
        Reporter.log("[framework] testContext=" + testContext, true);
    }

    @AfterMethod(alwaysRun = true)
    public void afterEach(ITestResult result) {
        Reporter.log("[framework] completed=" + testContext.testId() + " status=" + result.getStatus(), true);
    }

    protected AuthStrategy authStrategy() {
        return AuthStrategy.none();
    }

    /**
     * Default policy for all descendants: discovers a reporting-aware filter provider
     * via {@link ServiceLoader}. Falls back to {@link HttpFilterPolicy#defaultPolicy()}
     * when no provider is on the classpath.
     *
     * <p>Can be overridden in a specific test class for a custom filter pipeline.
     */
    protected HttpFilterPolicy filterPolicy() {
        return ServiceLoader.load(FilterPolicyProvider.class)
            .findFirst()
            .map(FilterPolicyProvider::provide)
            .orElse(HttpFilterPolicy.defaultPolicy());
    }

    protected boolean requiresLiveApi() {
        return false;
    }

    protected <T> T api(Function<HttpClient, T> apiFactory) {
        return apiFactory.apply(httpClient());
    }

    protected HttpClient httpClient() {
        if (httpClient == null) {
            initHttpClient();
        }
        return httpClient;
    }

    protected Map<String, String> environmentTags() {
        Map<String, String> tags = new LinkedHashMap<>();
        tags.put("profile", runtimeConfig.profile().name());
        tags.put("baseUrl", runtimeConfig.baseUrl());
        tags.put("liveTests", System.getProperty("framework.runLiveTests", "false"));
        return tags;
    }
}
