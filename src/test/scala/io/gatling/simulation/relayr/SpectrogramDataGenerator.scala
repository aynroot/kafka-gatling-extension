package io.gatling.simulation.relayr

import java.io.{BufferedWriter, File, FileWriter}

import scala.io.Source

object SpectrogramDataGenerator {
  val jsonFile: String = "/Users/aynroot/Documents/work/kafka-gatling-extension/src/test/resources/spectrogram_data_template.txt"
  val deviceFile: String = "/Users/aynroot/Documents/work/kafka-gatling-extension/src/test/resources/device_ids.txt"

  val devices: List[String] = Source.fromFile(deviceFile).getLines.toList
  val devicesMap: Map[Int, String] = (devices.indices zip devices).toMap

  def jsonFileContents(orgId: String, deviceId: String): String = Source.fromFile(jsonFile).getLines.mkString
      .replaceAll("<ORG_ID>", orgId)
      .replaceAll("<DEVICE_ID>", deviceId)
      .replaceAll("<RANDOM_ID>", java.util.UUID.randomUUID().toString)
      .replaceAll("<KEY>", deviceId)

  def createDataJsonFile(orgId: String, nDevices: Integer): String = {
    val tempSessionId = java.util.UUID.randomUUID().toString
    val tempFile = File.createTempFile(tempSessionId, ".json")

    val bw = new BufferedWriter(new FileWriter(tempFile))

    bw.write("[")
    for (i <- 0 until nDevices) {
      bw.write(jsonFileContents(orgId, devicesMap(i)))

      if (i != nDevices - 1)
        bw.write(",")
    }
    bw.write("]")
    bw.close()

    tempFile.getAbsolutePath
  }
}