package bookvoyage.scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import bookvoyage.config.TestConfig

import scala.concurrent.duration._

/**
 * 시나리오 2: 배치 트리거
 *
 * - T+2분에 배치 동기화 실행
 * - 30~60초마다 상태 조회
 * - 배치 API는 permitAll이므로 인증 불필요
 */
object BatchTriggerScenario {

  // Neo4j 동기화 트리거 (인증 불필요 - permitAll)
  val triggerNeo4jSync = exec(
    http("Trigger Neo4j Sync")
      .post("/api/admin/batch/sync-neo4j")
      .check(status.in(200, 202, 409)) // 409: 이미 실행 중
      .check(jsonPath("$.jobExecutionId").optional.saveAs("neo4jJobId"))
  )

  // Elasticsearch 동기화 트리거 (인증 불필요 - permitAll)
  val triggerEsSync = exec(
    http("Trigger Elasticsearch Sync")
      .post("/api/admin/batch/sync-elasticsearch")
      .check(status.in(200, 202, 409))
      .check(jsonPath("$.jobExecutionId").optional.saveAs("esJobId"))
  )

  // 배치 상태 조회
  val checkBatchStatus = exec(
    http("Check Batch Status")
      .get("/api/admin/batch/status")
      .check(status.in(200, 404))
  )

  // 배치 트리거 시나리오 (Neo4j + ES 동시)
  val batchTrigger = scenario("Batch Trigger")
    // 2분 대기 후 배치 트리거
    .pause(TestConfig.batchTriggerDelay.seconds)
    // Neo4j와 ES 동기화 순차 트리거
    .exec(triggerNeo4jSync)
    .pause(5.seconds)
    .exec(triggerEsSync)
    // 상태 폴링 (5분간)
    .during(300.seconds) {
      exec(checkBatchStatus)
        .pause(TestConfig.batchPollInterval.seconds)
    }

  // Neo4j만 트리거
  val neo4jOnlySync = scenario("Neo4j Sync Only")
    .pause(TestConfig.batchTriggerDelay.seconds)
    .exec(triggerNeo4jSync)
    .during(300.seconds) {
      exec(checkBatchStatus)
        .pause(TestConfig.batchPollInterval.seconds)
    }

  // Elasticsearch만 트리거
  val esOnlySync = scenario("Elasticsearch Sync Only")
    .pause(TestConfig.batchTriggerDelay.seconds)
    .exec(triggerEsSync)
    .during(300.seconds) {
      exec(checkBatchStatus)
        .pause(TestConfig.batchPollInterval.seconds)
    }
}
