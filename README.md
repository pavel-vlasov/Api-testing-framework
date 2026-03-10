# REST API Test Framework (Monorepo)

Multi-module API test framework on Java 17 + Gradle + TestNG + Rest Assured with DTO-first, API POM, centralized retry, reporting, and integration adapters.

## Modules

- `framework-core` - HTTP abstraction, request/response model, auth strategies, profile config, secrets providers, filters and masking, error model.
- `framework-api-model` - DTOs, endpoint objects, flow objects, domain assertions.
- `framework-testng` - base test classes, retry analyzer, listeners, soft assertions, data providers.
- `framework-db-oracle` - Oracle datasource + jOOQ repositories + await helpers.
- `framework-messaging-rabbitmq` - message bus abstraction, RabbitMQ publish/consume, correlation-id checks, await helpers.
- `framework-contracts` - JSON schema validation, JsonUnit assertions, snapshot checks.
- `framework-reporting` - Allure TestNG listener + request/response attachments with masking.
- `tests-smoke` - smoke suite examples.
- `tests-regression` - regression suite examples.
- `tests-integration` - integration suite examples.

## Key Patterns Implemented

- TestNG centralized retry (`FrameworkRetryAnalyzer`) with:
  - global policy via JVM props (`test.retry.maxAttempts`, `test.retry.delayMs`),
  - custom policy via `@RetrySetting` on class/method,
  - attempt logging and flaky report (`RetryReportingListener`).
- API POM:
  - Endpoint Objects (`AuthApi`, `UserApi`, `OrderApi`),
  - Flow Objects (`RegistrationFlow`, `OrderPlacementFlow`),
  - Assertion Helpers (`UserAssertions`, `OrderAssertions`).
- DTO-first across requests/responses.
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

## Running Tests

### Quick start

Run all tests:

```bash
./gradlew test
```

Run a specific suite module:

```bash
./gradlew :tests-smoke:test
./gradlew :tests-regression:test
./gradlew :tests-integration:test
```

### Automatic CLI parameter pickup

Framework parameters passed from command line are now forwarded automatically into test JVMs.
You can use either JVM properties (`-D`) or Gradle project properties (`-P`) with prefixes `framework.`, `test.`, `auth.`.

Examples:

```bash
./gradlew :tests-smoke:test -Dframework.profile=stage -Dframework.runLiveTests=true
./gradlew :tests-regression:test -Ptest.retry.maxAttempts=3 -Ptest.retry.delayMs=500
./gradlew :tests-integration:test -Pauth.basic.username=my_user -Pauth.basic.password=my_pass
```

You can combine `-D` and `-P` in one command.


## Allure Reports

The root project provides custom Gradle tasks for aggregated Allure report generation across test modules (without relying on the Allure Gradle plugin internals).

1) Run tests (one module or all):

```bash
gradle :tests-smoke:test
gradle :tests-regression:test
gradle :tests-integration:test
```

2) Build a single HTML report from collected results:

```bash
gradle allureReport
```

Generated report path:

- `build/reports/allure-report/allureReport/index.html`

3) Open report locally with temporary web server:

```bash
gradle allureServe
```

Notes:
- Task `aggregateAllureResults` collects results from `tests-*/build/allure-results` and legacy `tests-*/allure-results` locations.
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
-Dtest.retry.maxAttempts=2 -Dtest.retry.delayMs=500
```

Environment secret convention (`EnvSecretsProvider`):

- `FRAMEWORK_SECRET_AUTH_BASIC_USERNAME`
- `FRAMEWORK_SECRET_AUTH_BASIC_PASSWORD`
- `FRAMEWORK_SECRET_AUTH_OAUTH2_CLIENTSECRET`
- etc.

## Suite Entry Points

- Smoke: `tests-smoke/src/test/resources/testng-smoke.xml`
- Regression: `tests-regression/src/test/resources/testng-regression.xml`
- Integration: `tests-integration/src/test/resources/testng-integration.xml`

## Notes

- Oracle and RabbitMQ modules are implemented as reusable adapters and require runtime infra credentials/hosts.
