// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.cli

import java.time.Instant
import java.util.Calendar

import com.microsoft.azure.servicebus.ConnectionStringBuilder
import scopt.{OptionParser, RenderingMode}

case class Parameters(
    mode: String = "",
    var connectionString: String = "",
    var hubNamespace: String = "",
    var hubName: String = "",
    hubConsumerGroup: String = "$Default",
    var deviceId: String = "",
    var accessPolicy: String = "",
    var accessKey: String = "",
    messageType: String = "",
    contentType: String = "",
    content: String = "",
    var timeout: Int = 0,
    partition: Int = 0,
    d2cReceiveStartTime: Calendar = Calendar.getInstance(),
    d2cReceiveBatchSize: Int = 10,
    c2dSendExpire: Int = 3600,
    c2dReceiveAction: String = "",
    c2dCheckMsgId: String = "",
    verbose: Boolean = false
) {

  private val defaultSendTimeout         = 15000
  private val defaultCheckTimeout        = 2500
  private val defaultc2dSendExpire       = 3600
  private val defaultc2dReceiveBatchSize = 10
  private val eventHubNameLength         = 25

  val envHubName      = "IOTCLI_HUB_NAME"
  val envAccessPolicy = "IOTCLI_ACCESS_POLICY"
  val envAccessKey    = "IOTCLI_ACCESS_KEY"
  val envDeviceId     = "IOTCLI_DEVICE_ID"
  val envDeviceKey    = "IOTCLI_DEVICE_KEY"
  val envHubNamespace = "IOTCLI_HUB_NAMESPACE"

  def build: OptionParser[Parameters] = {
    new scopt.OptionParser[Parameters]("iot") {

      override def renderingMode: RenderingMode = RenderingMode.TwoColumns

      val cGroupChar   = 'c'
      val deviceIdChar = 'd'
      val cTypeChar    = 'f'
      val hubChar      = 'h'
      val keyChar      = 'k'
      val msgIdChar    = 'i'
      val policyChar   = 'p'
      val partChar     = 's'
      val msgTypeChar  = 't'
      val verboseChar  = 'v'

      head("Azure IoT CLI", "0.1.0", "-", "https://github.com/Azure/toketi-iot-tools")

      opt[Unit](verboseChar, "verbose").action((_, c) => c.copy(verbose = true)).text("Verbose flag")
      help("help").text("Prints this usage text")
      note("")

      cmd("d2c")
        .children(
          opt[String](hubChar, "hub").optional.valueName("<name>").action((x, c) => c.copy(hubName = x)).text("Azure IoT Hub name [required, (*)]"),
          opt[String](deviceIdChar, "device").optional.valueName("<ID>").action((x, c) => c.copy(deviceId = x)).text("Azure IoT device ID [required, (*)]"),
          opt[String](keyChar, "key").optional.valueName("<key>").action((x, c) => c.copy(accessKey = x)).text("Azure IoT device authorization key [required, (*)]"),
          note(""),
          cmd("send")
            .text("\n  Send a message from a Device to the Azure IoT Hub\n")
            .action((_, c) => c.copy(mode = "d2c.send"))
            .children(
              arg[String]("<content>").unbounded().required.action((x, c) => c.copy(content = x)).text("Message content [required]"),
              opt[String](msgTypeChar, "messagetype").optional.valueName("<t>").action((x, c) => c.copy(messageType = x)).text("Message type, e.g. temperature, humidity etc. [optional]"),
              opt[String](cTypeChar, "contenttype").optional.valueName("<f>").action((x, c) => c.copy(contentType = x)).text("Content type, e.g. json [optional]"),
              note("")
            ),
          cmd("receive")
            .text("\n  Receive a message sent from a Device\n")
            .action((_, c) => c.copy(mode = "d2c.receive"))
            .children(
              opt[String](cGroupChar, "consumergroup").optional.valueName("<g>").action((x, c) => c.copy(hubConsumerGroup = x)).text("Consumer group [optional, default: $Default]"),
              opt[Int](partChar, "partition").required.valueName("<n>").action((x, p) => p.copy(partition = x)).text("Azure IoT Hub source partition number [required]"),
              opt[String](deviceIdChar, "device").optional.valueName("<ID>").action((x, c) => c.copy(deviceId = x)).text("Azure IoT device ID [required, (*)]"),
              opt[Calendar]("starttime").optional.valueName("<date>").action((x, p) => p.copy(d2cReceiveStartTime = x)).text("Start date, e.g. 2001-06-30T23:59:59Z [optional]"),
              opt[Int]("batchsize").optional.valueName("<count>").action((x, c) => c.copy(d2cReceiveBatchSize = x)).text(s"Batch size [optional, default: ${defaultc2dReceiveBatchSize}, max: 999]"),
              note("")
            )
          /*cmd("stream")
            .text("\n  Receive all messages sent from devices to Azure IoT Hub\n")
            .action((_, c) => c.copy(mode = "d2c.stream"))
            .children(
              opt[String](cGroupChar, "consumergroup").optional.valueName("<g>").action((x, c) => c.copy(hubConsumerGroup = x)).text("Consumer group [optional, default: $Default]"),
              opt[Calendar]("starttime").optional.valueName("<date>").action((x, p) => p.copy(d2cReceiveStartTime = x)).text("Start date, e.g. 2001-06-30T23:59:59Z [optional]"),
              note("")
            )*/
        )

      cmd("c2d")
        .children(
          opt[String]("ns").optional.valueName("<ns>").action((x, c) => c.copy(hubNamespace = x)).text("Azure IoT Hub namespace [required, (*)]"),
          opt[String](hubChar, "hub").optional.valueName("<name>").action((x, c) => c.copy(hubName = x)).text("Azure IoT Hub name [required, (*)]"),
          opt[String](policyChar, "policy").optional.valueName("<name>").action((x, c) => c.copy(accessPolicy = x)).text("Azure IoT Hub shared access policy [required, (*)]"),
          opt[String](keyChar, "key").optional.valueName("<key>").action((x, c) => c.copy(accessKey = x)).text("Azure IoT Hub shared access key [required, (*)]"),
          note(""),
          cmd("receive")
            .text("\n  Receive a message send by a service to a connected device\n")
            .action((_, c) => c.copy(mode = "c2d.receive"))
            .children(
              opt[String]("action").required.valueName("<complete|abandon|reject>")
                .validate(x ⇒ {
                  val xl = x.toLowerCase()
                  if (xl == "complete" || xl == "abandon" || xl == "reject") success else failure("Value <action> must be either `complete`, `abandon` or `reject`")
                })
                .action((x, c) ⇒ c.copy(c2dReceiveAction = x.toLowerCase())).text("Action on incoming messages")
            ),
          cmd("send")
            .text("\n  Send a message to a device\n")
            .action((_, c) => c.copy(mode = "c2d.send"))
            .children(
              arg[String]("<content>").unbounded().required.action((x, c) => c.copy(content = x)).text("Message content [required]"),
              opt[String](msgTypeChar, "messagetype").optional.valueName("<t>").action((x, c) => c.copy(messageType = x)).text("Message type, e.g. temperature, humidity etc. [optional]"),
              opt[String](cTypeChar, "contenttype").optional.valueName("<f>").action((x, c) => c.copy(contentType = x)).text("Content type, e.g. json [optional]"),
              opt[Int]("expire").optional.valueName("<seconds>").optional.action((x, c) => c.copy(c2dSendExpire = x)).text(s"Message expiration in seconds [optional, default: ${defaultc2dSendExpire}]"),
              opt[Int]("timeout").optional.valueName("<msecs>").optional.action((x, c) => c.copy(timeout = x)).text(s"Send timeout [optional, default: ${defaultSendTimeout}]"),
              note("")
            ),
          cmd("check")
            .text("\n  Check the status of a message sent to a device\n")
            .action((_, c) => c.copy(mode = "c2d.check"))
            .children(
              opt[String](msgIdChar, "msgid").required.valueName("<id>").action((x, c) => c.copy(c2dCheckMsgId = x)).text("Message ID [required]"),
              opt[Int]("timeout").optional.valueName("<msecs>").action((x, c) => c.copy(timeout = x)).text(s"Check timeout [optional, default: ${defaultCheckTimeout}]"),
              note("")
            )
          /*cmd("invoke")
            .action((_, c) => c.copy(mode = "c2d.invoke"))
            .children(
              note("")
            ),
          cmd("setstatus")
            .action((_, c) => c.copy(mode = "c2d.setstatus"))
            .children(
              note("")
            )*/
        )

      note("(*) Some parameters can be stored in environment variables (command line values take precedence):")
      note("")
      note(s"  ${envHubName}      : Azure IoT Hub name")
      note(s"  ${envAccessPolicy} : access policy used by `c2d receive` / `c2d check` / `d2c receive`")
      note(s"  ${envAccessKey}    : access key used by `c2d receive` / `c2d check` / `d2c receive`")
      note(s"  ${envDeviceId}     : device ID")
      note(s"  ${envDeviceKey}    : device authorization key used by `d2c send` / `c2d receive`")
      note(s"  ${envHubNamespace} : namespace used by `d2c receive`")
    }
  }

  def postBuildValidation(): Unit = {

    // Read env vars
    if (hubNamespace.isEmpty && sys.env.contains(envHubNamespace)) {
      log(s"Retrieving Hub namespace from environment var `${envHubNamespace}`")
      hubNamespace = sys.env(envHubNamespace)
    }
    if (hubName.isEmpty && sys.env.contains(envHubName)) {
      log(s"Retrieving Hub name from environment var `${envHubName}`")
      hubName = sys.env(envHubName)
    }
    if (accessPolicy.isEmpty && sys.env.contains(envAccessPolicy)) {
      log(s"Retrieving Hub access policy from environment var `${envAccessPolicy}`")
      accessPolicy = sys.env(envAccessPolicy)
    }
    if (deviceId.isEmpty && sys.env.contains(envDeviceId)) {
      log(s"Retrieving Device ID from environment var `${envDeviceId}`")
      deviceId = sys.env(envDeviceId)
    }

    if (hubName.isEmpty) err("the Azure IoT Hub name is missing")

    mode match {

      case "d2c.send" ⇒ {

        if (accessKey.isEmpty && sys.env.contains(envDeviceKey)) {
          log(s"Retrieving Device Key from environment var `${envDeviceKey}`")
          accessKey = sys.env(envDeviceKey)
        }

        if (deviceId.isEmpty) err("the device ID is missing")
        if (accessKey.isEmpty) err("the device authorization key is missing")

        connectionString = deviceClientConnectionString(hubName, deviceId, accessKey)
      }

      case "d2c.receive" ⇒ {

        if (accessKey.isEmpty && sys.env.contains(envAccessKey)) {
          log(s"Retrieving Access Key from environment var `${envAccessKey}`")
          accessKey = sys.env(envAccessKey)
        }

        if (hubNamespace.isEmpty) err("the Azure IoT Hub namespace is missing")
        if (accessPolicy.isEmpty) err("the shared access policy is missing")
        if (accessKey.isEmpty) err("the shared access key is missing")

        connectionString = eventHubConnectionString(hubNamespace, hubName, accessPolicy, accessKey)
      }

      case "c2d.receive" ⇒ {

        if (accessKey.isEmpty && sys.env.contains(envDeviceKey)) {
          log(s"Retrieving Access Key from environment var `${envAccessKey}`")
          accessKey = sys.env(envDeviceKey)
        }

        if (deviceId.isEmpty) err("the device ID is missing")
        if (accessKey.isEmpty) err("the device authorization key is missing")

        connectionString = deviceClientConnectionString(hubName, deviceId, accessKey)
      }

      case "c2d.send" | "c2d.check" | "c2d.invoke" ⇒ {

        if (accessKey.isEmpty && sys.env.contains(envAccessKey)) {
          log(s"Retrieving Access Key from environment var `${envAccessKey}`")
          accessKey = sys.env(envAccessKey)
        }

        if (accessPolicy.isEmpty) err("the shared access policy is missing")
        if (accessKey.isEmpty) err("the shared access key is missing")

        connectionString = serviceClientConnectionString(hubName, accessPolicy, accessKey)

        if (timeout == 0 && mode == "c2d.send") timeout = defaultSendTimeout
        if (timeout == 0 && mode == "c2d.check") timeout = defaultCheckTimeout
      }

      case _ ⇒ err("Unknown command. Try --help for more information.")
    }
  }

  private[this] def eventHubConnectionString(ns: String, h: String, p: String, k: String): String =
    new ConnectionStringBuilder(ns, h.substring(0, eventHubNameLength), p, k).toString

  private[this] def deviceClientConnectionString(h: String, p: String, k: String): String =
    s"HostName=${h}.azure-devices.net;DeviceId=${p};SharedAccessKey=${k}"

  private[this] def serviceClientConnectionString(h: String, p: String, k: String): String =
    s"HostName=${h}.azure-devices.net;SharedAccessKeyName=${p};SharedAccessKey=${k}"

  private[this] def log(x: String, outputToUser: Boolean = false): Unit = {
    if (verbose) println(Instant.now + ": " + x)
    if (outputToUser) println(x)
  }

  private[this] def err(msg: String): Unit = {
    println("Error: " + msg)
    sys.exit(-1)
  }
}
