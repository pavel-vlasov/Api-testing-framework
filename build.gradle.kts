import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.jvm.toolchain.JavaLanguageVersion

allprojects {
    group = "com.apiframework"
    version = "0.1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

val testModules = listOf("tests-smoke", "tests-regression", "tests-integration")

val aggregateAllureResults by tasks.registering(Copy::class) {
    group = "verification"
    description = "Collects Allure results from test modules into a single directory."

    from(testModules.map { "$it/build/allure-results" })
    from(testModules.map { "$it/allure-results" })
    into(layout.buildDirectory.dir("allure-results"))

    mustRunAfter(testModules.map { ":$it:test" })
}

val allureCommandline by configurations.creating

val allureVersion = "2.33.0"

dependencies {
    allureCommandline("io.qameta.allure:allure-commandline:$allureVersion@zip")
}

val unpackAllureCommandline by tasks.registering(Copy::class) {
    group = "verification"
    description = "Downloads and unpacks Allure commandline."

    from({ allureCommandline.resolve().map { zipTree(it) } })
    into(layout.buildDirectory.dir("allure/commandline"))
}

val allureReport by tasks.registering(Exec::class) {
    group = "verification"
    description = "Builds Allure HTML report from aggregated results."
    dependsOn(aggregateAllureResults, unpackAllureCommandline)

    val outputDir = layout.buildDirectory.dir("reports/allure-report/allureReport")
    val inputDir = layout.buildDirectory.dir("allure-results")
    val executablePath = layout.buildDirectory.file("allure/commandline/allure-${allureVersion}/bin/allure")

    commandLine(
        executablePath.get().asFile.absolutePath,
        "generate",
        inputDir.get().asFile.absolutePath,
        "--clean",
        "-o",
        outputDir.get().asFile.absolutePath
    )
}

tasks.register<Exec>("allureServe") {
    group = "verification"
    description = "Builds and serves Allure report locally."
    dependsOn(aggregateAllureResults, unpackAllureCommandline)

    val inputDir = layout.buildDirectory.dir("allure-results")
    val executablePath = layout.buildDirectory.file("allure/commandline/allure-${allureVersion}/bin/allure")

    commandLine(
        executablePath.get().asFile.absolutePath,
        "serve",
        inputDir.get().asFile.absolutePath
    )
}

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
    }
}
