plugins {
    java
    id("org.springframework.boot") version "4.0.0"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "org.yyubin"
version = "0.0.1-SNAPSHOT"
description = "recommendation"

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
    // Domain dependency
    implementation(project(":domain"))
    implementation(project(":application"))

    // Neo4j
    implementation("org.springframework.boot:spring-boot-starter-data-neo4j")

    // Elasticsearch
    implementation("org.springframework.boot:spring-boot-starter-data-elasticsearch")

    // Web (recommendation API)
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Redis (추천 결과 저장용)
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    // Kafka (event ingest)
    implementation("org.springframework.kafka:spring-kafka")

    // JSON processing
    implementation("com.fasterxml.jackson.core:jackson-databind")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// 라이브러리 모듈이므로 bootJar 비활성화
tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = false
}

tasks.named<Jar>("jar") {
    enabled = true
}
