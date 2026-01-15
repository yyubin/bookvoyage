plugins {
    java
    id("org.springframework.boot") version "4.0.0" apply false
}

group = "org.yyubin"
version = "0.0.1-SNAPSHOT"
description = "batch"

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
    // Module dependencies
    implementation(project(":domain"))
    implementation(project(":application"))
    implementation(project(":infrastructure"))
    implementation(project(":recommendation"))

    // Spring Boot / Spring Batch
    implementation("org.springframework.boot:spring-boot-starter-batch:4.0.0")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:4.0.0")
    implementation("org.springframework.boot:spring-boot-starter-data-redis:4.0.0")

    // Spring Kafka
    implementation("org.springframework.kafka:spring-kafka:3.2.2")

    // Jackson
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    implementation("com.fasterxml.jackson.core:jackson-core:2.17.2")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.17.2")

    // ShedLock
    implementation("net.javacrumbs.shedlock:shedlock-spring:5.10.0")
    implementation("net.javacrumbs.shedlock:shedlock-provider-redis-spring:5.10.0")

    // Database
    runtimeOnly("com.mysql:mysql-connector-j:8.4.0")

    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test:4.0.0")
    testImplementation("org.springframework.batch:spring-batch-test:5.1.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.2")
}


tasks.withType<Test> {
    useJUnitPlatform()
}
