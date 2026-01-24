package bookvoyage.simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import bookvoyage.config.TestConfig
import bookvoyage.scenarios.{AuthenticatedUserScenario, BatchTriggerScenario, LoginLoadScenario}

import scala.concurrent.duration._

/**
 * 전체 실험 시나리오 (35분)
 *
 * 1. Baseline: 사용자 부하만 10분
 * 2. Batch ON: 동일 부하 + 배치 트리거 (15분)
 * 3. Spike: 배치 실행 중 유저 부하 2배 램프업 (5분)
 * 4. Cool down: 회복 시간 확인 (5분)
 */
class FullExperimentSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl(TestConfig.baseUrl)
    .acceptHeader("application/json")
    .acceptEncodingHeader("gzip, deflate")
    .userAgentHeader("Gatling/BookVoyage-PerformanceTest")
    .disableCaching
    .shareConnections

  val baseUsers = TestConfig.users

  // 시간 구간 정의
  val baselineDuration = 10.minutes
  val batchDuration = 15.minutes
  val spikeDuration = 5.minutes
  val cooldownDuration = 5.minutes

  setUp(
    // ========================================
    // 메인 사용자 부하 (전체 기간 지속)
    // ========================================
    AuthenticatedUserScenario.normalUser.inject(
      // Phase 1: Baseline (10분)
      rampUsers((baseUsers * 0.8).toInt).during(2.minutes),
      constantUsersPerSec((baseUsers * 0.8 / 60).toInt).during(8.minutes),

      // Phase 2: Batch ON (15분) - 동일 부하 유지
      constantUsersPerSec((baseUsers * 0.8 / 60).toInt).during(15.minutes),

      // Phase 3: Spike (5분) - 2배 부하
      rampUsers((baseUsers * 0.8).toInt).during(1.minute), // 추가 유저
      constantUsersPerSec((baseUsers * 1.6 / 60).toInt).during(4.minutes),

      // Phase 4: Cooldown (5분) - 부하 감소
      nothingFor(5.minutes)
    ),

    // 고빈도 사용자
    AuthenticatedUserScenario.heavyUser.inject(
      rampUsers((baseUsers * 0.2).toInt).during(2.minutes),
      constantUsersPerSec((baseUsers * 0.2 / 60).toInt).during(8.minutes),
      constantUsersPerSec((baseUsers * 0.2 / 60).toInt).during(15.minutes),
      rampUsers((baseUsers * 0.2).toInt).during(1.minute),
      constantUsersPerSec((baseUsers * 0.4 / 60).toInt).during(4.minutes),
      nothingFor(5.minutes)
    ),

    // ========================================
    // 배치 트리거 (Phase 2 시작 시점)
    // ========================================
    BatchTriggerScenario.batchTrigger.inject(
      nothingFor(baselineDuration), // Baseline 종료 후
      atOnceUsers(1)
    ),

    // ========================================
    // 로그인 부하 (전체 기간 지속, 낮은 비율)
    // ========================================
    LoginLoadScenario.loginLoad.inject(
      rampUsers(Math.max(1, (baseUsers * 0.02).toInt)).during(2.minutes),
      constantUsersPerSec(0.5).during(33.minutes) // 2초마다 1명
    )

  ).protocols(httpProtocol)
    .assertions(
      // Phase별 다른 기준 적용은 Gatling에서 직접 지원 안 함
      // 전체 기준으로 설정
      global.successfulRequests.percent.gt(95.0),
      global.responseTime.percentile3.lt(1500), // p95 < 1.5s
      global.responseTime.percentile4.lt(5000), // p99 < 5s

      // 특정 요청 기준 (Search Books는 외부 API rate limit 문제로 비활성화)
      details("Get User Profile").responseTime.percentile3.lt(300)
    )
}
