dependencies {
    api(project(":framework-core"))
    implementation(project(":framework-testng"))
    implementation("io.qameta.allure:allure-testng:2.33.0")
    implementation("io.qameta.allure:allure-rest-assured:2.33.0")
    testImplementation("org.testng:testng:7.12.0")
}
