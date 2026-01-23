plugins {
    scala
    id("io.gatling.gradle") version "3.11.5"
}

repositories {
    mavenCentral()
}

dependencies {
    gatling("io.gatling.highcharts:gatling-charts-highcharts:3.11.5")
    gatling("io.gatling:gatling-core:3.11.5")
    gatling("io.gatling:gatling-http:3.11.5")
}

gatling {
    // 시뮬레이션 필터 (특정 시뮬레이션만 실행 시)
    // includes = listOf("bookvoyage.*")
}

tasks.withType<ScalaCompile> {
    scalaCompileOptions.additionalParameters = listOf("-feature", "-deprecation")
}
