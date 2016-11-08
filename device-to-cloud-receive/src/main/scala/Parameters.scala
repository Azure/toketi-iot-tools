// Copyright (c) Microsoft. All rights reserved.

import java.time.Instant

import scopt.OptionParser
import java.util.Calendar

case class Parameters(
    partition: Int = 0,
    start: Calendar = Calendar.getInstance(),
    hubNamespace: String = Configuration.iotHubNamespace,
    hubName: String = Configuration.iotHubName,
    hubKeyName: String = Configuration.iotHubKeyName,
    hubKey: String = Configuration.iotHubKey,
    hubConsumerGroup: String = Configuration.receiverConsumerGroup,
    batchSize: Int = Configuration.receiverBatchSize,
    verbose: Boolean = false
) {

  start.setTimeInMillis(Instant.now.toEpochMilli)

  def build: OptionParser[Parameters] = {
    new scopt.OptionParser[Parameters]("d2c-send") {

      override def showUsageOnError = true

      head("d2c-receive", "0.1.0", "-", "https://github.com/Azure/toketi-iot-tools")

      opt[String]('n', "namespace").optional.valueName("<nse>").action((x, c) => c.copy(hubNamespace = x)).text("Azure IoT hub namespace")
      opt[String]('h', "hub").optional.valueName("<hub>").action((x, c) => c.copy(hubName = x)).text("Azure IoT Hub name")
      opt[Int]('s', "partition").required.valueName("<n>").action((x, p) => p.copy(partition = x)).text("Azure IoT Hub source partition number")
      opt[String]('p', "policy").optional.valueName("<name>").action((x, c) => c.copy(hubKeyName = x)).text("Azure IoT Hub messaging auth key name")
      opt[String]('k', "key").optional.valueName("<key>").action((x, c) => c.copy(hubConsumerGroup = x)).text("Azure IoT Hub messaging auth key")
      opt[Calendar]('t', "starttime").optional.valueName("<date>").action((x, p) => p.copy(start = x)).text("Start date, e.g. 2001-06-30T23:59:59Z")
      opt[Int]('b', "batch").optional.valueName("<count>").action((x, c) => c.copy(batchSize = x)).text("Batch size")
      opt[Unit]('v', "verbose").action((_, c) => c.copy(verbose = true)).text("Verbose flag")

      help("help").text("Prints this usage text")

      note("\nThe connection parameters can optionally be stored in application.conf.")
      note("If you edit application.conf, rebuild the application before using it.")
      note("\nSome configuration parameters can also be stored in environment variables:")
      note("IOTHUB_NAMESPACE, IOTHUB_NAME, IOTHUB_ACCESS_KEY_NAME and IOTHUB_ACCESS_KEY_VALUE")
    }
  }
}
