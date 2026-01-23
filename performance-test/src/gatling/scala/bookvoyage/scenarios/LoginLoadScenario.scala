package bookvoyage.scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import bookvoyage.config.TestConfig

import scala.concurrent.duration._
import scala.util.Random

/**
 * 시나리오 3: 로그인 부하 (옵션)
 *
 * - 분당 1~2% 비율로 로그인 시도
 * - 토큰 갱신/쿠키 생성 비용 반영
 */
object LoginLoadScenario {

  // 기존 테스트 계정 로드 (CSV)
  val testAccountsFeeder = csv("data/test_accounts.csv").circular

  val random = new Random()

  // 기존 계정으로 로그인 (정상 케이스)
  val freshLogin = feed(testAccountsFeeder)
    .exec(
      http("Fresh Login")
        .post("/api/auth/login")
        .header("Content-Type", "application/json")
        .body(StringBody(session =>
          s"""{"email": "${session("email").as[String]}", "password": "${session("password").as[String]}"}"""
        ))
        .asJson
        .check(status.in(200, 401))
        .check(headerRegex("Set-Cookie", "accessToken=([^;]+)").optional.saveAs("newAccessToken"))
    )

  // 로그아웃
  val logout = exec(
    http("Logout")
      .post("/api/auth/logout")
      .check(status.in(200, 204))
  )

  // 토큰 갱신
  val refreshToken = exec(
    http("Refresh Token")
      .post("/api/auth/refresh")
      .check(status.in(200, 401))
      .check(headerRegex("Set-Cookie", "accessToken=([^;]+)").optional.saveAs("refreshedAccessToken"))
  )

  // 로그인 부하 시나리오
  val loginLoad = scenario("Login Load")
    .during(TestConfig.duration.seconds) {
      randomSwitch(
        70.0 -> exec(freshLogin).pause(30.seconds, 60.seconds),
        20.0 -> exec(freshLogin).exec(refreshToken).pause(30.seconds, 60.seconds),
        10.0 -> exec(freshLogin).exec(logout).pause(30.seconds, 60.seconds)
      )
    }

  // 로그인 스파이크 (순간 대량 로그인)
  val loginSpike = scenario("Login Spike")
    .feed(testAccountsFeeder)
    .exec(
      http("Spike Login")
        .post("/api/auth/login")
        .header("Content-Type", "application/json")
        .body(StringBody(session =>
          s"""{"email": "${session("email").as[String]}", "password": "${session("password").as[String]}"}"""
        ))
        .asJson
        .check(status.in(200, 401))
    )
    .pause(1.second, 3.seconds)
    .exec(
      http("Profile After Login")
        .get("/api/users/me")
        .check(status.in(200, 401))
    )
}
