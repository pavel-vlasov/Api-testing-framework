# REST API Test Framework (Monorepo)

Multi-module API test framework on Java 21 + Gradle + TestNG + Rest Assured with API POM, centralized retry, reporting, and integration adapters.

## Modules

- `framework-core` - HTTP abstraction, request/response model, auth strategies, profile config, secrets providers, filters and masking, error model.
- `examples/sample-domain` - sample domain for Postman Echo (`endpoint`, `flow`, `assertions`, `model`).
- `framework-testng` - base test classes, retry analyzer, listeners, soft assertions, data providers.
- `framework-db-oracle` - Oracle datasource + jOOQ repositories + await helpers.
- `framework-messaging-rabbitmq` - message bus abstraction, RabbitMQ publish/consume, correlation-id checks, await helpers.
- `framework-contracts` - JSON schema validation, JsonUnit assertions, snapshot checks.
- `framework-reporting` - Allure TestNG listener + request/response attachments with masking.
- `framework-suite-support` - aggregated dependencies for suite modules.
- `test-suites/tests-smoke` - smoke suite examples.
- `test-suites/tests-regression` - regression suite examples.
- `test-suites/tests-integration` - integration suite examples.

## Key Patterns Implemented

- TestNG centralized retry (`FrameworkRetryAnalyzer`) with:
  - global policy via JVM props (`test.retry.maxRetries`, `test.retry.delayMs`),
  - custom policy via `@RetrySetting` on class/method,
  - attempt logging and flaky report (`RetryReportingListener`).
- API POM:
  - Endpoint Objects (`PostmanEchoApi`),
  - Flow Objects (`EchoFlow`),
  - Assertion Helpers (`EchoAssertions`).
- Domain DTOs are kept in `examples/sample-domain/model` to avoid coupling framework-core to a specific API.
- `framework-api-model` is intentionally **not** a separate module: DTO/transport models stay inside each concrete domain (for example, `examples/sample-domain/model`) and do not mix with framework-core.
- Auth strategies:
  - `BasicAuthStrategy`,
  - `OAuth2ClientCredentialsStrategy`,
  - `OAuth2PasswordStrategy` with thread-safe token cache, TTL-aware refresh and 401 fallback refresh.
- Config and secrets:
  - profiles (`dev/stage/prod`),
  - `EnvSecretsProvider`,
  - `VaultSecretsProvider`,
  - centralized resolver `ConfigResolver`.
- Unified HTTP filters:
  - request/response logging,
  - correlation-id,
  - timing,
  - sensitive masking.
- Contracts and snapshots:
  - JSON schema (`JsonSchemaContractValidator`),
  - JsonUnit assertions,
  - snapshot checker (`SnapshotContractChecker`).
- Reporting:
  - Allure TestNG listener,
  - auto-attachments for request/response,
  - masking for headers and body fields.

## How reporting works (runtime flow)

End-to-end reporting pipeline for tests based on `BaseApiTest`:

1. `BaseApiTest` creates `HttpClient` with reporting-aware filter policy (`ReportingFilterPolicies.withAllureReporting()`) by default.
2. Endpoint class (for example `PostmanEchoApi`) executes transport call via `httpClient.execute(...)`.
3. `AllureHttpStepFilter` (framework-reporting filter layer) creates the HTTP step and attaches request/response/metadata (with masking).
4. Domain orchestration (`EchoFlow`) creates `Flow:` and `Action:` steps via `AllureActionExecutor`.
5. Domain checks (`EchoAssertions`) create `Assert:` steps via `AllureActionExecutor`.
6. `AllureTestNgListener` enriches test lifecycle only (labels, retry metadata, failure stacktrace, run summary).

### Reporting responsibility boundaries

| Reporting concern | Owner layer/class | Must contain | Must not contain |
| --- | --- | --- | --- |
| HTTP request/response reporting | `framework-reporting` / `AllureHttpStepFilter` | HTTP step name, request/response/metadata attachments, masking, transport error attachment | Business-flow step semantics (`Flow:`/`Action:`/`Assert:`), TestNG lifecycle summary |
| Business/test action reporting | Domain flow/assert layers via `AllureActionExecutor` | `Flow:`, `Action:`, `Assert:` semantic steps | Direct request/response attachment logic, listener lifecycle logic |
| Test lifecycle reporting | `framework-reporting` / `AllureTestNgListener` | labels/tags, retry metadata, failure stacktrace, run/test summary | Per-request HTTP step creation and request/response serialization |

Contribution rule for new reporting logic:

- new HTTP-level attachment/step detail -> `framework-reporting` filter layer,
- new business flow/action/assert step -> flow/assertion layer via `AllureActionExecutor`,
- new retry/failure/summary metadata -> `AllureTestNgListener`.

Architectural rule for endpoint classes: transport only. Do not add `Allure.step(...)` or `Allure.addAttachment(...)` directly to endpoint methods.

## Running Tests

### Quick start

Run all tests:

```bash
./gradlew test
```

> Note: this repository keeps wrapper scripts and properties, but excludes `gradle/wrapper/gradle-wrapper.jar` from VCS to avoid binary artifacts in PRs. Regenerate it locally with `gradle wrapper` if needed.

Run a specific suite module:

```bash
./gradlew :test-suites:smoke:test
./gradlew :test-suites:regression:test
./gradlew :test-suites:integration:test
```

### Automatic CLI parameter pickup

Framework parameters passed from command line are now forwarded automatically into test JVMs.
You can use either JVM properties (`-D`) or Gradle project properties (`-P`) with prefixes `framework.`, `test.`, `auth.`.

Examples:

```bash
./gradlew :test-suites:smoke:test -Dframework.profile=stage -Dframework.runLiveTests=true
./gradlew :test-suites:regression:test -Ptest.retry.maxRetries=3 -Ptest.retry.delayMs=500
./gradlew :test-suites:integration:test -Pauth.basic.username=my_user -Pauth.basic.password=my_pass
```

You can combine `-D` and `-P` in one command.



### HTTP reporting is enabled by default via `BaseApiTest`

Any test extending `BaseApiTest` automatically gets reporting-aware HTTP filter policy:

- `framework.reporting.httpSteps.enabled=true` by default,
- `framework.reporting.attachments.enabled=true` by default,
- request/response/metadata attachments are generated in Allure per HTTP call,
- can be turned off only via system properties:

```bash
-Dframework.reporting.httpSteps.enabled=false
-Dframework.reporting.attachments.enabled=false
```

`BaseApiTest` still allows local override of `filterPolicy()` for special scenarios.

## Sample domain architecture (`examples/sample-domain`)

`examples/sample-domain` is the reference usage pattern:

- `endpoint` -> transport-level operations only (`PostmanEchoApi`),
- `flow` -> orchestration and business intent (`EchoFlow`),
- `assertions` -> domain-level checks (`EchoAssertions`),
- `model` -> API DTOs for this domain (`EchoGetResponse`, `EchoPostResponse`, `EchoPayload`).

DTOs should live in the concrete domain/example module (for example, `examples/sample-domain/model`), not in framework modules.

### Minimal runnable happy path

```java
public class PostmanEchoSmokeTest extends BaseApiTest {
    private EchoFlow echoFlow;

    @BeforeClass(alwaysRun = true)
    public void initFlow() {
        this.echoFlow = new EchoFlow(new PostmanEchoApi(httpClient()));
    }

    @Test
    public void shouldEchoQueryParameter() {
        QueryRoundtripResult result = echoFlow.verifyQueryRoundtrip("suite", "smoke");
        EchoAssertions.assertQueryRoundtrip(result);
    }
}
```

Expected Allure structure for this path:

- `Flow: Verify query roundtrip`
  - `Action: GET /get with query parameter`
    - `GET /get -> 200` (from HTTP filter, with request/response/metadata attachments)
  - `Assert: Verify transport success`
- `Assert: Verify echoed query matches expected`

## Allure Reports

The root project uses the Allure Gradle plugin configured via the Kotlin DSL `plugins {}` block.
Before report generation, root task `collectAllureResults` aggregates results from:

- `test-suites/tests-smoke/allure-results`
- `test-suites/tests-regression/allure-results`
- `test-suites/tests-integration/allure-results`

into a single source directory:

- `build/allure-results`

`allureReport` depends only on this aggregation step and builds HTML from the aggregated catalog.

### Allure tasks in root project

- `collectAllureResults` - collects existing results from suite modules into `build/allure-results`.
- `runSuitesForAllure` - explicitly runs smoke/regression/integration tests to prepare fresh Allure results (for this flow suite tests are forced to execute, not taken from up-to-date cache).
- `allureReport` - builds HTML report from already existing/collected results (does **not** run test suites by itself).
- `allureReportWithTests` - orchestration task: runs `runSuitesForAllure` and then generates `allureReport`.

1) Build aggregated HTML report from existing results:

```bash
./gradlew allureReport
```

2) Run all suites and then build aggregated report:

```bash
./gradlew allureReportWithTests
```

Generated report path:

- `build/reports/allure-report/allureReport/index.html`

3) Open report locally with temporary web server:

```bash
./gradlew allureServe
```

Notes:
- Use only plugin-based Allure tasks (`allureReport`, `allureServe`) and avoid duplicate manual task declarations with the same names.
- If tests are skipped (for example, live API disabled), Allure still generates a report with skipped status.

## Run Profiles and Secrets

Active profile:

```bash
-Dframework.profile=dev|stage|prod
```

Live API tests are disabled by default. Enable explicitly:

```bash
-Dframework.runLiveTests=true
```

Global retry policy:

```bash
-Dtest.retry.maxRetries=2 -Dtest.retry.delayMs=500
```

Environment secret convention (`EnvSecretsProvider`):

- `FRAMEWORK_SECRET_AUTH_BASIC_USERNAME`
- `FRAMEWORK_SECRET_AUTH_BASIC_PASSWORD`
- `FRAMEWORK_SECRET_AUTH_OAUTH2_CLIENTSECRET`
- etc.

## Suite Entry Points

- Smoke: `test-suites/tests-smoke/src/test/resources/testng-smoke.xml`
- Regression: `test-suites/tests-regression/src/test/resources/testng-regression.xml`
- Integration: `test-suites/tests-integration/src/test/resources/testng-integration.xml`

## Notes

- Oracle and RabbitMQ modules are implemented as reusable adapters and require runtime infra credentials/hosts.

## Retry Extensions (Pluggable)

You can swap retry behavior without changing framework code:

```bash
-Dtest.retry.predicateClass=com.example.CustomRetryPredicate
-Dtest.retry.delayStrategyClass=com.example.ExponentialBackoffDelayStrategy
```

Both classes must implement `RetryPredicate` and `RetryDelayStrategy` respectively and have a no-arg constructor.

