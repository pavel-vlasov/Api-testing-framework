import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    id("io.qameta.allure") version "3.0.1"
}

allprojects {
    group = "com.apiframework"
    version = "0.1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

val aggregatedAllureResultsDir = layout.buildDirectory.dir("allure-results")
val suiteAllureResultDirs = listOf(
    layout.projectDirectory.dir("test-suites/tests-smoke/allure-results"),
    layout.projectDirectory.dir("test-suites/tests-regression/allure-results"),
    layout.projectDirectory.dir("test-suites/tests-integration/allure-results"),
    // Backward compatibility for older local runs that still write into build/allure-results.
    layout.projectDirectory.dir("test-suites/tests-smoke/build/allure-results"),
    layout.projectDirectory.dir("test-suites/tests-regression/build/allure-results"),
    layout.projectDirectory.dir("test-suites/tests-integration/build/allure-results")
)

val allureSuiteTaskPaths = listOf(
    ":test-suites:smoke:test",
    ":test-suites:regression:test",
    ":test-suites:integration:test"
)

tasks.register("collectAllureResults") {
    group = "verification"
    description = "Collects Allure results from test modules into build/allure-results"
    mustRunAfter("runSuitesForAllure")

    doLast {
        val outputDir = aggregatedAllureResultsDir.get().asFile
        delete(outputDir)
        outputDir.mkdirs()

        copy {
            suiteAllureResultDirs.forEach { from(it) }
            into(outputDir)
            includeEmptyDirs = false
        }
    }
}

tasks.register("runSuitesForAllure") {
    group = "verification"
    description = "Runs smoke, regression and integration suites to prepare Allure results"

    dependsOn(allureSuiteTaskPaths)
}

tasks.named("allureReport") {
    dependsOn("collectAllureResults")
    mustRunAfter("runSuitesForAllure")

    doFirst {
        delete(layout.buildDirectory.dir("reports/allure-report/allureReport"))
    }
}

tasks.register("allureReportWithTests") {
    group = "verification"
    description = "Runs all suites and then builds the aggregated Allure report"

    dependsOn("runSuitesForAllure")
    dependsOn("allureReport")
}

val forceAllureSuiteRun = gradle.startParameter.taskNames.any { taskName ->
    taskName == "allureReportWithTests" ||
        taskName.endsWith(":allureReportWithTests") ||
        taskName == "runSuitesForAllure" ||
        taskName.endsWith(":runSuitesForAllure")
}

val allureSuiteTestTasks = allureSuiteTaskPaths.toSet()

subprojects {
    apply(plugin = "java-library")

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
        withJavadocJar()
        withSourcesJar()
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(21)
    }

    tasks.withType<Test>().configureEach {
        useTestNG()
        testLogging {
            events = setOf(TestLogEvent.PASSED, TestLogEvent.FAILED, TestLogEvent.SKIPPED)
            exceptionFormat = TestExceptionFormat.FULL
            showExceptions = true
            showStackTraces = true
            showStandardStreams = false
        }

        systemProperties(gradle.startParameter.systemPropertiesArgs)

        gradle.startParameter.projectProperties
            .filterKeys { key ->
                key.startsWith("framework.") || key.startsWith("test.") || key.startsWith("auth.")
            }
            .forEach { (key, value) ->
                systemProperty(key, value)
            }

        if (!systemProperties.containsKey("framework.profile")) {
            systemProperty("framework.profile", "dev")
        }

        if (forceAllureSuiteRun && path in allureSuiteTestTasks) {
            outputs.upToDateWhen { false }
        }
    }
}
