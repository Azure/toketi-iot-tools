// Copyright (c) Microsoft. All rights reserved.

import scopt.OptionParser

case class Parameters(
    hubName: String = "",
    deviceId: String = "",
    accessKey: String = "",
    contentType: String = "",
    contentFormat: String = "",
    content: String = "",
    verbose: Boolean = false) {

  def build: OptionParser[Parameters] = {
    new scopt.OptionParser[Parameters]("d2c-send") {

      override def showUsageOnError = true

      head("d2c-send", "0.1.0", "-", "https://github.com/Azure/toketi-iot-tools")

      arg[String]("<content>").unbounded().required().action((x, c) => c.copy(content = x)).text("Message content")
      opt[String]('h', "hub").required.valueName("<name>").action((x, c) => c.copy(hubName = x)).text("IoT hub name")
      opt[String]('d', "device").required.valueName("<ID>").action((x, c) => c.copy(deviceId = x)).text("IoT device ID")
      opt[String]('k', "key").required.valueName("<access key>").action((x, c) => c.copy(accessKey = x)).text("IoT device authorization key")
      opt[String]('t', "type").valueName("<type>").action((x, c) => c.copy(contentType = x)).text("Message type, e.g. temperature, humidity etc.")
      opt[String]('f', "format").valueName("<format>").action((x, c) => c.copy(contentFormat = x)).text("Message format, e.g. json")
      opt[Unit]('v', "verbose").action((_, c) => c.copy(verbose = true)).text("Verbose flag")

      help("help").text("Prints this usage text")
    }
  }
}
