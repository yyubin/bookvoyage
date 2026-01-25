package bookvoyage.simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import bookvoyage.config.TestConfig

import scala.concurrent.duration._

class RecommendationMaxRpsSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl(TestConfig.baseUrl)
    .acceptHeader("application/json")
    .acceptEncodingHeader("gzip, deflate")
    .userAgentHeader("Gatling/Recommendation-MaxRPS")
    .shareConnections
    .disableCaching

  // 추천 API (50:50)
  val getBookRecommendations =
    http("Get Book Recommendations")
      .get("/api/recommendations/books")
      .queryParam("limit", "20")
      .check(status.in(200, 204))

  val getReviewRecommendations =
    http("Get Review Recommendations")
      .get("/api/recommendations/reviews")
      .queryParam("limit", "20")
      .check(status.in(200, 204))

  val mixedRecommendations =
    randomSwitch(
      50.0 -> exec(getBookRecommendations),
      50.0 -> exec(getReviewRecommendations)
    )

  val scenarioDef =
    scenario("Recommendation Max RPS")
      .during(TestConfig.duration.seconds) {
        exec(mixedRecommendations)
      }

  val baseRps = TestConfig.users          // 시작 RPS
  val maxRps  = baseRps * 10              // 최대 RPS

  setUp(
    scenarioDef.inject(
      // Warm-up (1분)
      rampUsersPerSec(baseRps * 0.2).to(baseRps).during(1.minute),

      // RPS 증가 구간 (10분)
      rampUsersPerSec(baseRps).to(maxRps).during(10.minutes),

      // 최대 부하 유지 (5분)
      constantUsersPerSec(maxRps).during(5.minutes)
    )
  )
    .protocols(httpProtocol)
    .assertions(
      global.successfulRequests.percent.gt(99.0),
      global.responseTime.percentile3.lt(500),
      global.responseTime.percentile4.lt(1000)
    )
}
