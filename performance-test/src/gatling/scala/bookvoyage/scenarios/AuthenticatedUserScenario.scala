package bookvoyage.scenarios

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import bookvoyage.config.TestConfig

import scala.concurrent.duration._
import scala.util.Random

/**
 * 시나리오 1: 인증 사용자 트래픽 (주요 부하)
 *
 * - 검색: 40%
 * - 추천 조회: 30%
 * - 리뷰 상세 조회: 20%
 * - 사용자 프로필: 10%
 */
object AuthenticatedUserScenario {

  // 기존 테스트 계정 로드 (CSV)
  val testAccountsFeeder = csv("data/test_accounts.csv").circular

  // 검색 키워드 목록
  val searchKeywords = Array(
    "해리포터", "반지의제왕", "1984", "어린왕자", "데미안",
    "노인과바다", "위대한개츠비", "토지", "삼국지", "홍길동전",
    "코스모스", "사피엔스", "총균쇠", "이기적유전자", "침묵의봄"
  )

  // 리뷰 ID 범위 (실제 데이터에 맞게 조정)
  val reviewIdRange = 1 to 100

  // 랜덤 생성기
  val random = new Random()

  // 기존 테스트 계정으로 로그인
  val login = feed(testAccountsFeeder)
    .exec(
      http("Login")
        .post("/api/auth/login")
        .header("Content-Type", "application/json")
        .body(StringBody(session =>
          s"""{"email": "${session("email").as[String]}", "password": "${session("password").as[String]}"}"""
        ))
        .asJson
        .check(status.in(200, 401))
        .check(headerRegex("Set-Cookie", "accessToken=([^;]+)").optional.saveAs("accessToken"))
    )

  // 검색 API - 외부 API rate limit 문제로 비활성화
  // val search = exec(
  //   http("Search Books")
  //     .get("/api/search")
  //     .queryParam("q", _ => searchKeywords(random.nextInt(searchKeywords.length)))
  //     .queryParam("page", _ => random.nextInt(5))
  //     .queryParam("size", "20")
  //     .check(status.in(200, 204))
  // )

  // 추천 조회 (50% - 검색 비활성화로 비율 상향)
  val recommendations = exec(
    randomSwitch(
      50.0 -> exec(
        http("Get Book Recommendations")
          .get("/api/recommendations/books")
          .queryParam("limit", "20")
          .check(status.in(200, 204))
      ),
      50.0 -> exec(
        http("Get Review Recommendations")
          .get("/api/recommendations/reviews")
          .queryParam("limit", "20")
          .check(status.in(200, 204))
      )
    )
  )

  // 리뷰 상세 조회 (20%)
  val reviewDetail = exec(
    http("Get Review Detail")
      .get(session => s"/api/reviews/${reviewIdRange.start + random.nextInt(reviewIdRange.size)}")
      .check(status.in(200, 404)) // 없는 리뷰일 수 있음
  )

  // 사용자 프로필 조회 (10%)
  val userProfile = exec(
    http("Get User Profile")
      .get("/api/users/me")
      .check(status.in(200, 401))
  )

  // Think time (3~8초)
  def thinkTime = pause(
    TestConfig.thinkTimeMin.seconds,
    TestConfig.thinkTimeMax.seconds
  )

  // 메인 시나리오: 가중치 기반 액션 선택 (검색 비활성화로 재조정)
  val weightedActions = randomSwitch(
    50.0 -> exec(recommendations).exec(thinkTime),
    35.0 -> exec(reviewDetail).exec(thinkTime),
    15.0 -> exec(userProfile).exec(thinkTime)
  )

  // 일반 사용자 시나리오 (80%)
  val normalUser = scenario("Normal User Traffic")
    .exec(login)
    .during(TestConfig.duration.seconds) {
      exec(weightedActions)
    }

  // 고빈도 사용자 시나리오 (20%) - Think time 짧음 (검색 비활성화)
  val heavyUser = scenario("Heavy User Traffic")
    .exec(login)
    .during(TestConfig.duration.seconds) {
      exec(
        randomSwitch(
          50.0 -> exec(recommendations),
          35.0 -> exec(reviewDetail),
          15.0 -> exec(userProfile)
        )
      ).pause(1.second, 3.seconds) // 더 짧은 think time
    }
}
