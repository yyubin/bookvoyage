package bookvoyage.simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import bookvoyage.config.TestConfig
import bookvoyage.scenarios.{AuthenticatedUserScenario, BatchTriggerScenario, LoginLoadScenario}

import scala.concurrent.duration._

/**
 * 실험 2: Batch ON
 *
 * 동일 부하 + 배치 트리거 (15분)
 * 배치 실행 중 API 성능 저하 측정
 */
class BatchWithLoadSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl(TestConfig.baseUrl)
    .acceptHeader("application/json")
    .acceptEncodingHeader("gzip, deflate")
    .userAgentHeader("Gatling/BookVoyage-PerformanceTest")
    .disableCaching
    .shareConnections

  // 사용자 분포
  val normalUserCount = (TestConfig.users * 0.8).toInt
  val heavyUserCount = (TestConfig.users * 0.2).toInt

  // 로그인 부하 (전체의 2%)
  val loginUserCount = Math.max(1, (TestConfig.users * 0.02).toInt)

  setUp(
    // 일반 사용자 부하
    AuthenticatedUserScenario.normalUser.inject(
      rampUsers(normalUserCount).during(TestConfig.rampUpDuration.seconds)
    ),

    // 고빈도 사용자 부하
    AuthenticatedUserScenario.heavyUser.inject(
      rampUsers(heavyUserCount).during(TestConfig.rampUpDuration.seconds)
    ),

    // 로그인 부하 (지속적)
    LoginLoadScenario.loginLoad.inject(
      rampUsers(loginUserCount).during(TestConfig.rampUpDuration.seconds)
    ),

    // 배치 트리거 (단일 사용자)
    BatchTriggerScenario.batchTrigger.inject(
      atOnceUsers(1)
    )
  ).protocols(httpProtocol)
    .assertions(
      // 배치 중에는 기준 완화
      global.successfulRequests.percent.gt(95.0),
      global.responseTime.percentile3.lt(1000), // p95 < 1s
      global.responseTime.percentile4.lt(3000)  // p99 < 3s
    )
}
