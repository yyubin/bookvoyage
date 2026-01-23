plugins {
    scala
    id("io.gatling.gradle") version "3.10.3"
}

repositories {
    mavenCentral()
}

dependencies {
    gatling("io.gatling.highcharts:gatling-charts-highcharts:3.10.3")
    gatling("io.gatling:gatling-core:3.10.3")
    gatling("io.gatling:gatling-http:3.10.3")
}

gatling {
    // Gatling 설정
    logLevel = "WARN"
    logHttp = io.gatling.gradle.LogHttp.NONE

    // 시뮬레이션 필터 (특정 시뮬레이션만 실행 시)
    // includes = listOf("bookvoyage.*")
}

tasks.withType<ScalaCompile> {
    scalaCompileOptions.additionalParameters = listOf("-feature", "-deprecation")
}
