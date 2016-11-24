// Copyright (c) Microsoft. All rights reserved.

import java.util.UUID

import com.microsoft.azure.iothub._

class Device(hubName: String, deviceId: String, accessKey: String, verbose: Boolean) {

  private val connString = ConnectionString.build(hubName, deviceId, accessKey)
  private val modelProp  = "$$contentModel"
  private val formatProp = "$$contentType"
  private var ready      = true
  private val waitOnSend = 5000
  private val waitUnit   = 100

  log("Connecting...")
  private val client = new DeviceClient(connString, IotHubClientProtocol.HTTPS)
  client.open()

  private class EventCallback extends IotHubEventCallback {
    override def execute(status: IotHubStatusCode, context: scala.Any): Unit = {
      ready = true
      log("Message sent.")
    }
  }

  def isReady: Boolean = ready

  def sendMessage(content: String, format: String, model: String): Unit = {
    try {
      if (!ready) {
        throw new RuntimeException("The device client is busy")
      }

      ready = false

      // Prepare message
      val message = new Message(content)
      message.setCorrelationId(UUID.randomUUID().toString)
      if (format.nonEmpty) message.setProperty(formatProp, format)
      if (model.nonEmpty) message.setProperty(modelProp, model)

      // Send
      log(s"Sending message '${content}' ...")
      client.sendEventAsync(message, new EventCallback(), None)

      // Wait a bit
      log("Waiting for confirmation...")
      var wait = waitOnSend
      if (!ready) while (wait > 0 && !ready) {
        Thread.sleep(waitUnit)
        wait -= waitUnit
      }
    } catch {
      case e: Exception => {
        ready = false
        client.close()
        throw e
      }
    }
  }

  def disconnect(): Unit = {
    log("Disconnecting...")
    ready = false
    client.close()
    log(s"Disconnected.")
  }

  def log(x: String): Unit = {
    if (verbose) println(x)
  }
}

