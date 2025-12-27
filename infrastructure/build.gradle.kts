plugins {
    java
    id("org.springframework.boot") version "4.0.0"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "org.yyubin"
version = "0.0.1-SNAPSHOT"
description = "infrastructure"

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
    implementation(project(":application")) // Port 구현이므로 허용
    implementation(project(":domain"))
    implementation(project(":support")) // JWT 필터 사용
    implementation(project(":recommendation"))

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.kafka:spring-kafka:4.0.0")
    implementation("org.springframework.boot:spring-boot-starter-data-elasticsearch")

    // AWS S3
    implementation("software.amazon.awssdk:s3:2.20.26")

    // Redisson for distributed lock
    implementation("org.redisson:redisson:3.27.0")

    compileOnly("org.projectlombok:lombok")
    runtimeOnly("com.mysql:mysql-connector-j")
    annotationProcessor("org.projectlombok:lombok")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.h2database:h2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    annotationProcessor("io.github.yyubin:jinx-processor:0.0.20")
    implementation("io.github.yyubin:jinx-core:0.0.20")
}

val jinxCli by configurations.creating

dependencies {
    jinxCli("io.github.yyubin:jinx-cli:0.0.20")
}

tasks.register<JavaExec>("jinxMigrate") {
    group = "jinx"
    classpath = configurations["jinxCli"]
    mainClass.set("org.jinx.cli.JinxCli")

    dependsOn("classes")
    args("db", "migrate", "-d", "mysql")
}

tasks.register<JavaExec>("jinxPromoteBaseline") {
    group = "jinx"
    classpath = configurations["jinxCli"]
    mainClass.set("org.jinx.cli.JinxCli")

    dependsOn("classes")
    args("db", "promote-baseline", "--force")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.bootJar {
    enabled = false
}
