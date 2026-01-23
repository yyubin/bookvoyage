package bookvoyage.config

import com.typesafe.config.ConfigFactory

object TestConfig {
  private val config = ConfigFactory.load()

  // Base URL
  val baseUrl: String = config.getString("test.baseUrl")

  // Auth
  val userEmailDomain: String = config.getString("test.auth.userEmailDomain")

  // Load
  val users: Int = config.getInt("test.load.users")
  val rampUpDuration: Int = config.getInt("test.load.rampUpDuration")
  val duration: Int = config.getInt("test.load.duration")
  val thinkTimeMin: Int = config.getInt("test.load.thinkTimeMin")
  val thinkTimeMax: Int = config.getInt("test.load.thinkTimeMax")

  // Batch
  val batchTriggerDelay: Int = config.getInt("test.batch.triggerDelay")
  val batchPollInterval: Int = config.getInt("test.batch.pollInterval")
}
