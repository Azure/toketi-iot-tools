// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.cli.d2c.send

import java.time.Instant
import java.util.UUID

import com.microsoft.azure.iothub.{Message, _}

class Device(connString: String, deviceId: String, verbose: Boolean) {

  private val protocol        = IotHubClientProtocol.HTTPS
  private val contentTypeProp = "$$contentType"
  private val messageTypeProp = "$$contentModel"

  private var ready      = true
  private val waitOnSend = 5000
  private val waitUnit   = 500
  private val client     = new DeviceClient(connString, protocol)

  def isReady: Boolean = ready

  def connect(): Unit = {
    log("Connecting...")
    client.open()
  }

  def sendMessage(
      content: String,
      contentType: String,
      messageType: String,
      correlationId: String): String = {
    try {
      if (!ready) throw new RuntimeException("The device client is busy")
      ready = false
      val msg = prepareMessage(content, contentType, messageType, correlationId)
      send(msg)
      msg.getMessageId
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

  private[this] def prepareMessage(
      content: String,
      contentType: String,
      messageType: String,
      correlationId: String): Message = {
    log(s"Preparing message...")
    val msg = new Message(content)
    msg.setMessageId(UUID.randomUUID().toString)
    msg.setCorrelationId(correlationId)
    if (contentType.nonEmpty) msg.setProperty(contentTypeProp, contentType)
    if (messageType.nonEmpty) msg.setProperty(messageTypeProp, messageType)
    msg
  }

  private[this] def send(msg: Message): Unit = {
    log(s"Sending message...")
    client.sendEventAsync(msg, new EventCallback(), None)

    // Wait a bit
    log("Waiting for confirmation...")
    var wait = waitOnSend
    if (!ready) while (wait > 0 && !ready) {
      Thread.sleep(waitUnit)
      wait -= waitUnit
    }
  }

  private class EventCallback extends IotHubEventCallback {
    override def execute(status: IotHubStatusCode, context: scala.Any): Unit = {
      ready = true
      log("Message sent.")
    }
  }

  private[this] def log(x: String, outputToUser: Boolean = false): Unit = {
    if (verbose) println(Instant.now + ": " + x)
    if (outputToUser) println(x)
  }
}


