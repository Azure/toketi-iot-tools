// Copyright (c) Microsoft. All rights reserved.

import java.time.Instant
import java.util.concurrent.TimeUnit

import com.microsoft.azure.iot.service.sdk._

import scala.collection.JavaConverters._
import scala.language.{implicitConversions, postfixOps}

class Service(deviceId: String, connString: String, verbose: Boolean) {

  val protocol         = IotHubServiceClientProtocol.AMQPS
  val deliverTimeout   = 10000
  val feedbackTimeout  = 60000
  val contentTypeProp  = "$$contentType"
  val contentModelProp = "$$contentModel"

  lazy val serviceClient    = ServiceClient.createFromConnectionString(connString, protocol)
  lazy val feedbackReceiver = serviceClient.getFeedbackReceiver(deviceId)

  def connect(): Unit = {
    log(s"Connecting client to ${connString}...")
    serviceClient.open()
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
    log(s"Preparing message, correlation ID '${correlationId}'...")
    val msg = MessageToDevice(content)
      .to(deviceId)
      .correlationId(correlationId)
      .expiry(Instant.now.plusSeconds(expiration))
      .userId(userId)
      .ack(DeliveryAcknowledgement.Full)
    log(s"Message ID '${msg.id}'...")
    if (contentType.nonEmpty) msg.addProperty(contentTypeProp, contentType)
    if (contentModel.nonEmpty) msg.addProperty(contentModelProp, contentModel)
    msg
  }

  def send(msg: MessageToDevice): Unit = {
    log(s"Sending message...")
    serviceClient
      .sendAsync(msg.deviceId, msg.message)
      .exceptionally(e ⇒ {
        err("Request failed.")
        println(e)
        sys.exit(-1)
      })
      .get(deliverTimeout, TimeUnit.MILLISECONDS)
  }

  def getFeedback(id: String): FeedbackRecord = {
    log("Requesting feedback...")
    var found = false
    var attempts = 50
    var result: FeedbackRecord = null
    while (!found && attempts > 0) {
      attempts -= 1

      val batch = feedbackReceiver
        .receiveAsync()
        .exceptionally(e ⇒ {
          err("Request failed.")
          println(e)
          sys.exit(-1)
        })
        .get(feedbackTimeout, TimeUnit.MILLISECONDS)
      log(s"Feedback batch size: ${batch.getRecords.size()}")

      for (r ← batch.getRecords().asScala) {
        log(s"Checking feedback record: msg ${r.getOriginalMessageId}, device ${r.getDeviceId}")
        if (r.getOriginalMessageId == id) {
          result = r
          found = true
        }
      }
    }

    result
  }

  def log(x: String): Unit = {
    if (verbose) println(Instant.now + ": " + x)
  }

  def err(x: String): Unit = {
    println(Instant.now + ": " + x)
  }
}
