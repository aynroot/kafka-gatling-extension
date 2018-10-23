package io.gatling.simulation.relayr

import java.util

import io.gatling.core.Predef.{Simulation, _}
import io.gatling.kafka.{KafkaProducerBuilder, KafkaProducerProtocol}
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer


class SpectrogramDataSimulation extends Simulation {

  // mvn package &&
  //   mvn gatling:execute -Dgatling.simulationClass=io.gatling.simulation.relayr.SpectrogramDataSimulation -Dusers=100 -Dduration=600 -DrampUpDuration=30 -Ddevices=100


  val kafkaBrokers: String = System.getProperty("kafkaBrokers", "localhost:9092")
  val kafkaTopic: String = System.getProperty("kafkaTopic", "test_topic")
  val orgId: String = System.getProperty("orgId", "00000000-0000-0000-0000-000000000000")

  val usersPerSec: Double = System.getProperty("users", "10").toDouble
  val rampUpDuration: Integer = Integer.getInteger("rampUpDuration", 30)
  val duration: Integer = Integer.getInteger("duration", 600)
  val nDevices: Integer = Integer.getInteger("devices", 10)


  val props = new util.HashMap[String, Object]()
  props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBrokers)
  props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer])
  props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer])

  val kafkaProducerProtocol = new KafkaProducerProtocol(props, kafkaTopic)

  println()
  println("SpectrogramDataSimulation is being instantiated")
  println("\t kafkaBrokers = " + kafkaBrokers)
  println("\t kafkaTopic = " + kafkaTopic)
  println("\t orgId = " + orgId)
  println("\t nDevices = " + nDevices)
  println()
  println("Ingesting at a rate of " + usersPerSec + " users / sec for " + duration + " seconds.")
  println()

  setUp(scenario("Kafka Producer Call")
    .feed(jsonFile(SpectrogramDataGenerator.createDataJsonFile(orgId, nDevices)).circular)
    .exec(KafkaProducerBuilder())

    .inject(
      rampUsersPerSec(1) to usersPerSec during rampUpDuration,
      constantUsersPerSec(usersPerSec) during duration
    )
  ).protocols(kafkaProducerProtocol)
}
