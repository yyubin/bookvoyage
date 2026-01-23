package bookvoyage.simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import bookvoyage.config.TestConfig
import bookvoyage.scenarios.AuthenticatedUserScenario

import scala.concurrent.duration._

/**
 * 실험 1: Baseline
 *
 * 사용자 부하만 10분 (p95 기준선 측정)
 * 배치 없이 순수 API 성능 측정
 */
class BaselineSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl(TestConfig.baseUrl)
    .acceptHeader("application/json")
    .acceptEncodingHeader("gzip, deflate")
    .userAgentHeader("Gatling/BookVoyage-PerformanceTest")
    // 쿠키 자동 관리
    .disableCaching
    .shareConnections

  // 일반 사용자 80%
  val normalUserCount = (TestConfig.users * 0.8).toInt
  // 고빈도 사용자 20%
  val heavyUserCount = (TestConfig.users * 0.2).toInt

  setUp(
    AuthenticatedUserScenario.normalUser.inject(
      rampUsers(normalUserCount).during(TestConfig.rampUpDuration.seconds)
    ),
    AuthenticatedUserScenario.heavyUser.inject(
      rampUsers(heavyUserCount).during(TestConfig.rampUpDuration.seconds)
    )
  ).protocols(httpProtocol)
    .assertions(
      // 전역 성공률 99% 이상
      global.successfulRequests.percent.gt(99.0),
      // p95 응답시간 500ms 이하
      global.responseTime.percentile3.lt(500),
      // p99 응답시간 1000ms 이하
      global.responseTime.percentile4.lt(1000)
    )
}
