package io.gatling.kafka

import java.time.Instant

import io.gatling.core.Predef.Session
import io.gatling.core.protocol.Protocol
import org.apache.avro.Schema
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.json4s._
import org.json4s.native.Serialization._


class KafkaProducerProtocol(props: java.util.HashMap[String, Object], topics: String) extends Protocol {
  implicit val formats: DefaultFormats.type = org.json4s.DefaultFormats

  private final val kafkaProducer = new KafkaProducer[String, String](props)
  private var key: String = _
  private var value: String = _


  def call(session: Session,
           schema: Option[Schema] = None): Unit = {
    val attributes = session.attributes

    if (attributes.nonEmpty) {
      val deviceId = attributes("authEntityId").asInstanceOf[String]
      val name = attributes("name").asInstanceOf[String]

      val dataMap = Map(
        "deviceId" -> deviceId,
        "name" -> name,
        "value" -> scala.util.Random.nextInt(200),
        "recorded" -> Instant.now().toString,
        "received" -> Instant.now().toString
      )

      val finalMessage = attributes - "name" - "value" + ("data" -> dataMap)

      key = attributes("key").toString
      value = write(finalMessage)
    } else {
      throw new UnsupportedOperationException("Feeder is not specified.")
    }

    val record = new ProducerRecord[String, String](topics, key, value)
    kafkaProducer.send(record)
  }
}