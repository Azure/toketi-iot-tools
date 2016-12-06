// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.cli

import java.time.Instant
import java.util.UUID

import com.microsoft.azure.iot.cli.c2d.send.Service
import com.microsoft.azure.iot.cli.d2c.send.Device

import scala.language.{implicitConversions, postfixOps}

object Main extends App {
  var verbose       = false
  val correlationId = UUID.randomUUID().toString
  val userId        = "azure-iot-cli"

  Parameters().build.parse(args, Parameters()) match {
    case Some(p) ⇒ {
      try {
        verbose = p.verbose
        log(s"Correlation ID: ${correlationId}")
        p.postBuildValidation()

        log(s"Hub name: ${p.hubName}")

        p.mode match {
          case "d2c.send" ⇒ {
            val device = new Device(p.connectionString, p.deviceId, p.verbose)
            device.connect()
            val id = device.sendMessage(p.content, p.contentType, p.messageType, correlationId)
            log("Message ID: " + id)
            device.disconnect()
          }

          case "c2d.send" ⇒ {
            val service = new Service(p.deviceId, p.connectionString, verbose)
            service.connect()
            val id = service.sendMessage(p.timeout, p.deviceId, p.content, p.contentType, p.messageType, p.c2dSendExpire, userId, correlationId)
            log("Message ID: " + id)
            log(s"Message '${id}' for device '${p.deviceId}' added to the queue.", true)
            service.disconnect()
          }

          case "d2c.receive" ⇒ {
            val service = new com.microsoft.azure.iot.cli.d2c.receive.Service(
              p.deviceId,
              p.connectionString,
              p.hubConsumerGroup,
              p.partition,
              p.d2cReceiveStartTime.toInstant,
              p.d2cReceiveBatchSize,
              verbose)
            service.connect()
            service.receive()
            service.disconnect()
          }

          case "d2c.stream" ⇒ {
            // Work in progress
          }

          case "c2d.receive" ⇒ {
            val client = new com.microsoft.azure.iot.cli.c2d.receive.Device(p.connectionString, verbose)
            client.connect()
            client.receive(p.c2dReceiveAction)
            scala.io.StdIn.readLine("Press enter to exit...\n")
            client.disconnect()
          }

          case "c2d.check" ⇒ {
            val service = new Service(p.deviceId, p.connectionString, verbose)
            service.connect()
            val feedback = service.getFeedback(p.c2dCheckMsgId, p.timeout)
            service.showFeedback(feedback)
          }

          case _ ⇒ {
            log("Unknown command", true)
            sys.exit(-1)
          }
        }
      } catch {
        case e: Exception =>
          println("Error: " + e.getClass + ": " + e.getMessage)
          sys.exit(-1)
      }

      println("Done.")
      sys.exit(0)
    }

    case None ⇒ {
      sys.exit(-1)
    }
  }

  private def log(x: String, outputToUser: Boolean = false): Unit = {
    if (verbose) println(Instant.now + ": " + x)
    if (outputToUser) println(x)
  }
}
