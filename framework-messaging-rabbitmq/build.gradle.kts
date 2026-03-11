dependencies {
    api(project(":framework-core"))
    implementation("com.rabbitmq:amqp-client:5.29.0")
    implementation("org.awaitility:awaitility:4.3.0")
    testImplementation("org.testng:testng:7.12.0")
}
