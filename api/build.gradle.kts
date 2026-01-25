plugins {
    java
    id("org.springframework.boot") version "4.0.0"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "org.yyubin"
version = "v1"
description = "api"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":application"))
    implementation(project(":domain"))
    implementation(project(":infrastructure"))
    implementation(project(":support"))
    implementation(project(":recommendation"))
    implementation(project(":batch"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")

    // Monitoring - Actuator & Prometheus
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")
    implementation("com.fasterxml.jackson.core:jackson-databind")

    // Neo4j (recommendation 모듈에서 사용)
    implementation("org.springframework.boot:spring-boot-starter-data-neo4j")

    // Elasticsearch (recommendation 모듈에서 사용)
    implementation("org.springframework.boot:spring-boot-starter-data-elasticsearch")

    // API Documentation - SpringDoc OpenAPI 3
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0")

    // dotenv for environment variables management
    implementation("io.github.cdimascio:dotenv-java:3.0.0")

    runtimeOnly("com.mysql:mysql-connector-j")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // Test dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-redis")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
