rootProject.name = "rest-api-test-framework"

include(
    "framework-core",
    "framework-testng",
    "framework-db-oracle",
    "framework-messaging-rabbitmq",
    "framework-contracts",
    "framework-reporting",
    "framework-suite-support",
    ":examples:sample-domain",
    ":test-suites:smoke",
    ":test-suites:regression",
    ":test-suites:integration"
)

project(":examples:sample-domain").projectDir = file("examples/sample-domain")
project(":test-suites:smoke").projectDir = file("test-suites/tests-smoke")
project(":test-suites:regression").projectDir = file("test-suites/tests-regression")
project(":test-suites:integration").projectDir = file("test-suites/tests-integration")
