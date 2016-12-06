// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.cli.c2d.send

import java.time.Instant
import java.util.concurrent.TimeUnit
import com.microsoft.azure.iot.service.sdk._
import scala.collection.JavaConverters._
import scala.language.{implicitConversions, postfixOps}

class Service(deviceId: String, connString: String, verbose: Boolean) {

  val protocol        = IotHubServiceClientProtocol.AMQPS
  val contentTypeProp = "$$contentType"
  val messageTypeProp = "$$contentModel"

  val serviceClient    = ServiceClient.createFromConnectionString(connString, protocol)
  val feedbackReceiver = serviceClient.getFeedbackReceiver(deviceId)

  def connect(): Unit = {
    log("Connecting...")
    log("Opening service client connection...")
    serviceClient.open()
    log("Opening feedback receiver connection...")
    feedbackReceiver.open()
  }

  def sendMessage(
      timeout: Int,
      deviceId: String,
      content: String,
      contentType: String,
      messageType: String,
      expiration: Int,
      userId: String,
      correlationId: String): String = {
    val msg = prepareMessage(deviceId, content, contentType, messageType, expiration, userId, correlationId)
    send(msg, timeout)
    msg.id
  }

  def getFeedback(id: String, timeout: Int): FeedbackRecord = {
    log("Requesting message status ...")
    var found = false
    var attempts = 20
    var result: FeedbackRecord = null
    while (!found && attempts > 0) {
      attempts -= 1

      log(s"Receiving batch, timeout ${timeout} milliseconds ...")
      val batch = feedbackReceiver.receive(timeout)
      log(s"Received feedback batch, size: ${batch.getRecords.size()}")

      for (r ← batch.getRecords().asScala) {
        log(s"Checking feedback record: msg ${r.getOriginalMessageId}, device ${r.getDeviceId}")
        if (r.getOriginalMessageId == id) {
          result = r
          found = true
        }
      }
    }

    if (!found) {
      err("Message status not found.")
      sys.exit(-1)
    }

    result
  }

  def disconnect(): Unit = {
    feedbackReceiver.close()
    serviceClient.close()
  }

  def showFeedback(feedback: FeedbackRecord): Unit = {
    if (feedback == null) {
      err(s"Message not enqueued")
    }

    else {
      // success, expired, deliveryCountExceeded, rejected, unknown
      println(s"Message '${feedback.getOriginalMessageId}' status:\n" +
        s"  Enqueue time: ${feedback.getEnqueuedTimeUtc.toString}\n" +
        s"  Description: ${feedback.getDescription}\n" +
        s"  Status code: ${feedback.getStatusCode}")
    }
  }

  private[this] def prepareMessage(
      deviceId: String,
      content: String,
      contentType: String,
      messageType: String,
      expiration: Int,
      userId: String,
      correlationId: String): MessageToDevice = {
    log(s"Preparing message, expires in ${expiration} seconds...")
    val msg = MessageToDevice(content)
      .to(deviceId)
      .correlationId(correlationId)
      .expiry(Instant.now.plusSeconds(expiration))
      .userId(userId)
      .ack(DeliveryAcknowledgement.Full)
    if (contentType.nonEmpty) msg.addProperty(contentTypeProp, contentType)
    if (messageType.nonEmpty) msg.addProperty(messageTypeProp, messageType)
    msg
  }

  private[this] def send(msg: MessageToDevice, timeout: Int): Unit = {
    log(s"Sending message...")
    serviceClient.sendAsync(msg.deviceId, msg.message)
      .exceptionally(e ⇒ {
        err("Request failed.")
        log(e.toString, true)
        sys.exit(-1)
      })
      .get(timeout, TimeUnit.MILLISECONDS)
  }

  private[this] def log(x: String, outputToUser: Boolean = false): Unit = {
    if (verbose) println(Instant.now + ": " + x)
    if (outputToUser) println(x)
  }

  private[this] def err(x: String): Unit = {
    println(Instant.now + ": " + x)
  }
}

