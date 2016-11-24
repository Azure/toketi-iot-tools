// Copyright (c) Microsoft. All rights reserved.

import java.time.Instant
import java.util.UUID

import com.microsoft.azure.iot.service.sdk._

/** Send messages from Azure Cloud to IoT Devices
  */
object Main extends App {

  var verbose       = false
  val correlationId = UUID.randomUUID().toString
  val userId        = "c2d-send"
  val expiration    = 86400

  Parameters().build.parse(args, Parameters()) match {
    case Some(p) =>
      try {
        p.postBuildValidation()
        verbose = p.verbose
        val service = new Service(p.deviceId, p.connString, verbose)
        service.connect()

        p.mode match {
          case "send"  => {
            val msg = service.prepareMessage(p.deviceId, p.content, p.contentType, p.contentModel, expiration, userId, correlationId)
            println("Message ID: " + msg.id)
            service.send(msg)
            if (p.feedback) {
              val feedback = service.getFeedback(msg.id)
              showFeedback(feedback)
            }
          }
          case "check" ⇒ {
            val feedback = service.getFeedback(p.msgId)
            showFeedback(feedback)
          }
        }

      } catch {

        case e: java.util.concurrent.CompletionException =>
          throw e
          println(e)
          err(e.toString)
          sys.exit(-1)

        case e: java.util.concurrent.TimeoutException =>
          err("The operation timed out.")
          sys.exit(-1)

        case e: Exception =>
          err("Ensure that either the application is configured or all parameters are passed.")
          err(e.toString)
          sys.exit(-1)
      }

      log("Done.")
      sys.exit(0)

    case None =>
      sys.exit(-1)
  }

  def showFeedback(feedback: FeedbackRecord): Unit = {
    if (feedback == null) {
      err(s"Message not enqueued")
    }

    else {
      // success, expired, deliveryCountExceeded, rejected, unknown
      val status = feedback.getStatusCode().toString
      log(s"Message to ${feedback.getDeviceId} enqueue status: ${feedback.getDescription} [${status}], " +
        s"time ${feedback.getEnqueuedTimeUtc.toString}")
    }
  }

  def log(x: String): Unit = {
    if (verbose) println(Instant.now + ": " + x)
  }

  def err(x: String): Unit = {
    println(Instant.now + ": " + x)
  }
}
