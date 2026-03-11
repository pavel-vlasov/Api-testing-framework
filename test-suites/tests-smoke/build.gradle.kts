dependencies {
    testImplementation(project(":framework-suite-support"))
}

tasks.test {
    useTestNG {
        suites("src/test/resources/testng-smoke.xml")
    }
}
