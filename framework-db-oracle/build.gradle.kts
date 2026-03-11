dependencies {
    api(project(":framework-core"))
    implementation("org.jooq:jooq:3.18.15")
    implementation("com.zaxxer:HikariCP:6.3.0")
    implementation("org.awaitility:awaitility:4.3.0")
    compileOnly("com.oracle.database.jdbc:ojdbc11:23.7.0.25.01")
    compileOnly("jakarta.xml.bind:jakarta.xml.bind-api:4.0.2")
    testImplementation("org.testng:testng:7.12.0")
}
