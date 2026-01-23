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
 */
object BatchTriggerScenario {

  // 관리자 계정 생성 (없으면) + 로그인
  val adminLogin = exec(
    http("Admin SignUp")
      .post("/api/auth/signup")
      .header("Content-Type", "application/json")
      .body(StringBody(
        s"""{"email": "${TestConfig.adminEmail}", "password": "${TestConfig.adminPassword}", "username": "Admin"}"""
      ))
      .check(status.in(201, 409)) // 409: 이미 존재
  ).pause(1.second).exec(
    http("Admin Login")
      .post("/api/auth/login")
      .header("Content-Type", "application/json")
      .body(StringBody(
        s"""{"email": "${TestConfig.adminEmail}", "password": "${TestConfig.adminPassword}"}"""
      ))
      .check(status.in(200, 401))
      .check(headerRegex("Set-Cookie", "accessToken=([^;]+)").optional.saveAs("adminAccessToken"))
  )

  // Neo4j 동기화 트리거
  val triggerNeo4jSync = exec(
    http("Trigger Neo4j Sync")
      .post("/api/admin/batch/sync-neo4j")
      .check(status.in(200, 202, 409)) // 409: 이미 실행 중
      .check(jsonPath("$.jobExecutionId").optional.saveAs("neo4jJobId"))
  )

  // Elasticsearch 동기화 트리거
  val triggerEsSync = exec(
    http("Trigger Elasticsearch Sync")
      .post("/api/admin/batch/sync-elasticsearch")
      .check(status.in(200, 202, 409))
      .check(jsonPath("$.jobExecutionId").optional.saveAs("esJobId"))
  )

  // 배치 상태 조회 (Job ID가 있는 경우)
  val checkBatchStatus = exec(
    http("Check Batch Status")
      .get("/api/admin/batch/status")
      .check(status.is(200))
  )

  // 배치 트리거 시나리오
  val batchTrigger = scenario("Batch Trigger")
    // 관리자 로그인
    .exec(adminLogin)
    // 2분 대기 후 배치 트리거
    .pause(TestConfig.batchTriggerDelay.seconds)
    // Neo4j와 ES 동기화 동시 트리거
    .exec(triggerNeo4jSync)
    .pause(5.seconds)
    .exec(triggerEsSync)
    // 상태 폴링 (5분간)
    .during(300.seconds) {
      exec(checkBatchStatus)
        .pause(TestConfig.batchPollInterval.seconds)
    }

  // 개별 배치만 트리거 (선택적)
  val neo4jOnlySync = scenario("Neo4j Sync Only")
    .exec(adminLogin)
    .pause(TestConfig.batchTriggerDelay.seconds)
    .exec(triggerNeo4jSync)
    .during(300.seconds) {
      exec(checkBatchStatus)
        .pause(TestConfig.batchPollInterval.seconds)
    }

  val esOnlySync = scenario("Elasticsearch Sync Only")
    .exec(adminLogin)
    .pause(TestConfig.batchTriggerDelay.seconds)
    .exec(triggerEsSync)
    .during(300.seconds) {
      exec(checkBatchStatus)
        .pause(TestConfig.batchPollInterval.seconds)
    }
}
