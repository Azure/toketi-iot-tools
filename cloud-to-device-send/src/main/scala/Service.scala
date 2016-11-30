// Copyright (c) Microsoft. All rights reserved.

import java.time.Instant
import java.util.concurrent.TimeUnit
import com.microsoft.azure.iot.service.sdk._
import scala.collection.JavaConverters._
import scala.language.{implicitConversions, postfixOps}

class Service(deviceId: String, connString: String, verbose: Boolean) {

  val protocol         = IotHubServiceClientProtocol.AMQPS
  val contentTypeProp  = "$$contentType"
  val contentModelProp = "$$contentModel"

  val serviceClient    = ServiceClient.createFromConnectionString(connString, protocol)
  val feedbackReceiver = serviceClient.getFeedbackReceiver(deviceId)

  def connect(): Unit = {
    log(s"Connecting to ${connString}...")
    log("Opening service client connection...")
    serviceClient.open()
    log("Opening feedback receiver connection...")
    feedbackReceiver.open()
  }

  def prepareMessage(
      deviceId: String,
      content: String,
      contentType: String,
      contentModel: String,
      expiration: Int,
      userId: String,
      correlationId: String): MessageToDevice = {
    log(s"Preparing message, correlation ID '${correlationId}', expires in ${expiration} seconds...")
    val msg = MessageToDevice(content)
      .to(deviceId)
      .correlationId(correlationId)
      .expiry(Instant.now.plusSeconds(expiration))
      .userId(userId)
      .ack(DeliveryAcknowledgement.Full)
    if (contentType.nonEmpty) msg.addProperty(contentTypeProp, contentType)
    if (contentModel.nonEmpty) msg.addProperty(contentModelProp, contentModel)
    msg
  }

  def send(msg: MessageToDevice, timeout: Int): Unit = {
    log(s"Sending message...")
    serviceClient.sendAsync(msg.deviceId, msg.message)
      .exceptionally(e ⇒ {
        err("Request failed.")
        log(e.toString, true)
        sys.exit(-1)
      })
      .get(timeout, TimeUnit.MILLISECONDS)
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

  def log(x: String, outputToUser: Boolean = false): Unit = {
    if (verbose) println(Instant.now + ": " + x)
    if (outputToUser) println(x)
  }

  def err(x: String): Unit = {
    println(Instant.now + ": " + x)
  }
}
