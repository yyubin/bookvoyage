package bookvoyage.simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import bookvoyage.config.TestConfig
import bookvoyage.scenarios.{AuthenticatedUserScenario, BatchTriggerScenario, LoginLoadScenario}

import scala.concurrent.duration._

/**
 * 실험 3: Spike Test
 *
 * 배치 실행 중 유저 부하 2배 램프업 (5분)
 * 시스템 한계점 측정
 */
class SpikeTestSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl(TestConfig.baseUrl)
    .acceptHeader("application/json")
    .acceptEncodingHeader("gzip, deflate")
    .userAgentHeader("Gatling/BookVoyage-PerformanceTest")
    .disableCaching
    .shareConnections

  val baseUsers = TestConfig.users
  val spikeUsers = TestConfig.users * 2

  setUp(
    // 기본 부하 (지속)
    AuthenticatedUserScenario.normalUser.inject(
      rampUsers((baseUsers * 0.8).toInt).during(60.seconds),
      nothingFor(60.seconds), // 안정화 대기
      // 스파이크: 2배 부하
      rampUsers((spikeUsers * 0.8).toInt).during(60.seconds),
      constantUsersPerSec((spikeUsers * 0.8).toInt / 60.0).during(180.seconds), // 3분간 유지
      // 쿨다운
      nothingFor(60.seconds)
    ),

    // 고빈도 사용자
    AuthenticatedUserScenario.heavyUser.inject(
      rampUsers((baseUsers * 0.2).toInt).during(60.seconds),
      nothingFor(60.seconds),
      rampUsers((spikeUsers * 0.2).toInt).during(60.seconds),
      constantUsersPerSec((spikeUsers * 0.2).toInt / 60.0).during(180.seconds),
      nothingFor(60.seconds)
    ),

    // 배치 트리거 (스파이크 시작 시점에)
    BatchTriggerScenario.batchTrigger.inject(
      nothingFor(120.seconds), // 2분 후 배치 시작
      atOnceUsers(1)
    ),

    // 로그인 스파이크 (스파이크 시점에 대량 로그인)
    LoginLoadScenario.loginSpike.inject(
      nothingFor(120.seconds),
      rampUsers(50).during(30.seconds) // 30초간 50명 로그인 시도
    )
  ).protocols(httpProtocol)
    .assertions(
      // 스파이크 중에도 90% 이상 성공
      global.successfulRequests.percent.gt(90.0),
      // 에러율 10% 이하
      global.failedRequests.percent.lt(10.0)
    )
}
