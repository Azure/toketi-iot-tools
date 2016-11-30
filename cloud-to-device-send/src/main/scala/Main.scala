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

  Parameters().build.parse(args, Parameters()) match {
    case Some(p) =>
      try {
        p.postBuildValidation()
        verbose = p.verbose
        val service = new Service(p.deviceId, p.connString, verbose)
        service.connect()

        p.mode match {
          case "send"  => {
            val msg = service.prepareMessage(p.deviceId, p.sendContent, p.sendContentType, p.sendContentModel, p.sendExpiration, userId, correlationId)
            log("Message ID: " + msg.id)
            service.send(msg, p.sendTimeout)
            log(s"Message '${msg.id}' for device '${p.deviceId}' added to the queue.", true)
            if (p.sendFeedback) {
              val feedback = service.getFeedback(msg.id, p.checkTimeout)
              showFeedback(feedback)
            }
          }
          case "check" ⇒ {
            val feedback = service.getFeedback(p.checkMsgId, p.checkTimeout)
            showFeedback(feedback)
          }
        }

      } catch {

        case e: java.util.concurrent.CompletionException =>
          throw e
          log(e.toString, true)
          err("Internal error")
          sys.exit(-1)

        case e: java.util.concurrent.TimeoutException =>
          err("The operation timed out.")
          sys.exit(-1)

        case e: Exception =>
          log(e.toString, true)
          err("Ensure that either the application is configured or all parameters are passed.")
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
      println(s"Message '${feedback.getOriginalMessageId}' status:\n" +
        s"  Enqueue time: ${feedback.getEnqueuedTimeUtc.toString}\n" +
        s"  Description: ${feedback.getDescription}\n" +
        s"  Status code: ${feedback.getStatusCode}")
    }
  }

  def log(x: String, outputToUser: Boolean = false): Unit = {
    if (verbose) println(Instant.now + ": " + x)
    if (outputToUser) println(x)
  }

  def err(x: String): Unit = {
    println(Instant.now + ": " + x)
  }
}
