// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.cli.c2d.receive

import java.time.Instant

import com.microsoft.azure.iothub.{IotHubMessageResult, Message, MessageProperty}

class EventCallback(action: String, verbose: Boolean) extends com.microsoft.azure.iothub.MessageCallback {

  override def execute(message: Message, callbackContext: scala.Any): IotHubMessageResult = {
    val content = message.getBytes
    val correlationId = message.getCorrelationId
    val props: Array[MessageProperty] = message.getProperties
    val expired = message.isExpired

    // GitHub Issue 990: val id = message.getMessageId
    val id = message.getProperty("messageId")

    log(s"Received message:\n  ID: ${id}\n  Content: ${new String(content)}", true)

    val result = action match {
      case "complete" ⇒ IotHubMessageResult.COMPLETE
      case "abandon"  ⇒ IotHubMessageResult.ABANDON
      case "reject"   ⇒ IotHubMessageResult.REJECT
    }

    log(s"Replying to the message with `${result}`", true)

    result
  }

  private[this] def log(x: String, outputToUser: Boolean = false): Unit = {
    if (verbose) println(Instant.now + ": " + x)
    if (outputToUser) println(x)
  }
}
