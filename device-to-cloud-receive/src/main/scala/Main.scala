// Copyright (c) Microsoft. All rights reserved.

import com.microsoft.azure.eventhubs.EventHubClient
import com.microsoft.azure.servicebus.ConnectionStringBuilder
import messagesFromDevices.MessageFromDevice

import scala.collection.JavaConverters._
import scala.language.{implicitConversions, postfixOps}

/** Receive messages sent from IoT devices
  */
object Main extends App {

  val attemptsOnEmpty = 3
  val sleepOnEmpty    = 2000
  var verbose         = false

  Parameters().build.parse(args, Parameters()) match {
    case Some(p) =>
      verbose = p.verbose

      // Some validation
      if (p.hubNamespace.isEmpty || p.hubName.isEmpty || p.accessPolicy.isEmpty || p.accessKey.isEmpty) {
        println("Error: Some required parameters required to connect to Azure IoT Hub are missing:")
        if (p.hubNamespace.isEmpty) println("  Azure IoT Hub messaging namespace is missing")
        if (p.hubName.isEmpty) println("  Azure IoT Hub name is missing")
        if (p.accessPolicy.isEmpty) println("  Azure IoT Hub policy name is missing")
        if (p.accessKey.isEmpty) println("  Azure IoT Hub key is missing")
        Parameters().build.showUsage()
        sys.exit(-1)
      }

      // Download and print messages
      try {
        log(s"Connecting to ${p.hubName} ${p.hubNamespace} ${p.accessPolicy}")
        val connString = new ConnectionStringBuilder(p.hubNamespace, p.hubName, p.accessPolicy, p.accessKey).toString
        val client = EventHubClient.createFromConnectionStringSync(connString)
        log("Client ready")

        val receiver = client.createReceiverSync(p.hubConsumerGroup, p.partition.toString, p.start.toInstant)
        log(s"Receiver ready, partition ${p.partition}, start ${p.start.toInstant.toString}")

        log("Downloading messages")
        var attempts = attemptsOnEmpty
        var continue = true
        while (continue) {
          val messages = receiver.receiveSync(p.batchSize)

          val iterator = messages.asScala.map(e ⇒ MessageFromDevice(e, Some(p.partition))).toList
          if (iterator.size == 0) {
            log("No messages")
            Thread.sleep(sleepOnEmpty)
            attempts -= 1
            continue = attempts > 0
          } else {
            attempts = attemptsOnEmpty
            iterator.foreach(t ⇒ {
              println(s"${t.offset} - ${t.model} - ${t.created} - ${t.contentAsString}")
            })
          }
        }
      } catch {
        case e: Exception =>
          println("Ensure that either the application is configured or all params are passed.")
          println(e.getMessage)
          sys.exit(-1)
      }

      println("Done")

    case None =>
      sys.exit(-1)
  }

  def log(x: String): Unit = {
    if (verbose) println(x)
  }
}
