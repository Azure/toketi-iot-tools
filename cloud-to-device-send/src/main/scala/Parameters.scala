// Copyright (c) Microsoft. All rights reserved.

import scopt.{OptionParser, RenderingMode}

case class Parameters(
    mode: String = "send",
    var connString: String = "",
    accessHostName: String = Configuration.accessHostName,
    accessPolicy: String = Configuration.accessPolicy,
    accessKey: String = Configuration.accessKey,
    deviceId: String = "",
    checkMsgId: String = "",
    checkTimeout: Int = 100,
    sendTimeout: Int = 10000,
    sendContentModel: String = "",
    sendContentType: String = "",
    sendContent: String = "",
    sendFeedback: Boolean = false,
    sendExpiration: Int = 86400,
    verbose: Boolean = false) {

  def build: OptionParser[Parameters] = {
    new scopt.OptionParser[Parameters]("c2d-send") {

      override def showUsageOnError = true

      override def renderingMode: RenderingMode = RenderingMode.TwoColumns

      head("c2d-send", "0.1.0", "-", "https://github.com/Azure/toketi-iot-tools")

      opt[String]('d', "device").required.valueName("<ID>").action((x, c) => c.copy(deviceId = x)).text("Azure IoT device ID")
      opt[Unit]('v', "verbose").action((_, c) => c.copy(verbose = true)).text("Verbose flag")
      help("help").text("Prints this usage text")

      note("")
      cmd("send")
        .action((_, c) => c.copy(mode = "send"))
        .children(opt[String]('m', "model").optional.valueName("<model>").action((x, c) => c.copy(sendContentModel = x)).text("Message model, e.g. temperature, humidity etc."),
                   opt[String]('f', "format").optional.valueName("<format>").action((x, c) => c.copy(sendContentType = x)).text("Message format, e.g. json"),
                   opt[Int]('e', "expire").optional.valueName("<seconds>").action((x, c) => c.copy(sendExpiration = x)).text("Message expiration in seconds"),
                   opt[Int]("sendtimeout").optional.valueName("<msecs>").action((x, c) => c.copy(sendTimeout = x)).text("Send timeout"),
                   opt[Unit]('c', "check").optional.action((_, c) => c.copy(sendFeedback = true)).text("Check status immediately after sending a message"),
                   opt[Int]("checktimeout").optional.valueName("<msecs>").action((x, c) => c.copy(checkTimeout = x)).text("Check timeout"),
                   arg[String]("<content>").unbounded().required().action((x, c) => c.copy(sendContent = x)).text("Message content"))

      note("")
      cmd("check")
        .action((_, c) => c.copy(mode = "check"))
        .children(opt[String]('i', "id").required.valueName("<id>").action((x, c) => c.copy(checkMsgId = x)).text("Message ID"),
                   opt[Int]("checktimeout").optional.valueName("<msecs>").action((x, c) => c.copy(checkTimeout = x)).text("Check timeout"))

      note("")
      note("Auth details can be passed as a full connection string or by single parameters:")
      note("")
      opt[String]('c', "cstring").optional.valueName("<string>").action((x, c) => c.copy(connString = x)).text("Shared access policy connection string 'HostName=...;SharedAccessKeyName=...;SharedAccessKey=...'")
      opt[String]('h', "hostname").optional.valueName("<host>").action((x, c) => c.copy(accessHostName = x)).text("Shared access policy connection host, e.g. 'myhub.azure-devices.net'")
      opt[String]('p', "policy").optional.valueName("<name>").action((x, c) => c.copy(accessPolicy = x)).text("Shared access policy, e.g. 'service'")
      opt[String]('k', "key").optional.valueName("<key>").action((x, c) => c.copy(accessKey = x)).text("Shared access key")

      note("")
      note("Examples:")
      note("")
      note("c2d-send send -d myDevice mymessage -h myhub.azure-devices.net -p service -k $IOT_SERVICE_KEY --sendtimeout 10")
      note("c2d-send send -v -d myDevice \"some text\" -h myhub.azure-devices.net -p service -k $IOT_SERVICE_KEY -c")
      note("c2d-send check -v -d myDevice -h myhub.azure-devices.net -p service -k $IOT_SERVICE_KEY -i f786dc8b-990a-46c6-9f2f-5dde9875269a")
      note("")

      /*
      TODO: copy application.conf during build
      note("")
      note("The connection parameters can optionally be stored in application.conf.")
      note("If you edit application.conf, rebuild the application to apply the changes.")
      note("\nSome configuration parameters can also be stored in environment variables and referenced from application.conf:")
      note("IOTHUB_ACCESS_HOSTNAME, IOTHUB_ACCESS_POLICY and IOTHUB_ACCESS_KEY")
      */
    }
  }

  def postBuildValidation(): Unit = {

    var _accessHostName = accessHostName
    var _accessPolicy = accessPolicy
    var _accessKey = accessKey

    if (connString.isEmpty) {

      /*
      TODO: copy application.conf during build
      if (_accessHostName.isEmpty || _accessPolicy.isEmpty || _accessKey.isEmpty) {
        try {
          val confConnPath = "iothub-c2d-send."
          val conf: Config = ConfigFactory.load()

          if (_accessHostName.isEmpty) _accessHostName = conf.getString(confConnPath + "accessHostName")
          if (_accessPolicy.isEmpty) _accessPolicy = conf.getString(confConnPath + "accessPolicy")
          if (_accessKey.isEmpty) _accessKey = conf.getString(confConnPath + "accessKey")
        } catch {
          case e: Exception => Unit
        }
      }
      */

      if (_accessHostName.isEmpty || _accessPolicy.isEmpty || _accessKey.isEmpty) {
        println("Error: Some required parameters required to connect to Azure IoT Hub are missing:")
        if (_accessHostName.isEmpty) println("  Azure IoT Hub hostname is missing")
        if (_accessPolicy.isEmpty) println("  Azure IoT Hub policy name is missing")
        if (_accessKey.isEmpty) println("  Azure IoT Hub key is missing")

        Parameters().build.showUsage
        sys.exit(-1)
      }

      this.connString = ConnectionString.build(_accessHostName, _accessPolicy, _accessKey)
    }
  }

}
