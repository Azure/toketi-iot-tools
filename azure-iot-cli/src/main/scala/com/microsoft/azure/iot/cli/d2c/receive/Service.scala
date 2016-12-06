// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.cli.d2c.receive

import java.time.Instant
import scala.collection.JavaConverters._
import scala.language.{implicitConversions, postfixOps}

import com.microsoft.azure.eventhubs.{EventHubClient, PartitionReceiver}

class Service(
    deviceId: String,
    connectionString: String,
    hubConsumerGroup: String,
    partition: Int,
    startTime: Instant,
    batchSize: Int,
    verbose: Boolean) {

  val attemptsOnEmpty = 3
  val sleepOnEmpty    = 2000

  val client: EventHubClient = EventHubClient.createFromConnectionStringSync(connectionString)
  private var receiver: PartitionReceiver = null

  def connect(): Unit = {
    log(s"Connecting device `${deviceId}`...")
    receiver = client.createReceiverSync(hubConsumerGroup, partition.toString, startTime)
    log(s"Receiver ready, partition ${partition}, start ${startTime}")
  }

  def receive(): Unit = {
    log("Downloading messages")
    var attempts = attemptsOnEmpty
    var continue = true
    while (continue) {
      val messages = receiver.receiveSync(batchSize)

      val iterator = messages.asScala.map(e ⇒ MessageFromDevice(e, Some(partition))).toList
      if (iterator.size == 0) {
        log("No messages")
        Thread.sleep(sleepOnEmpty)
        attempts -= 1
        continue = attempts > 0
      } else {
        attempts = attemptsOnEmpty
        iterator.foreach(t ⇒ {
          if (t.messageType.isEmpty)
            println(s"${t.offset} - ${t.created} - ${t.contentAsString}")
          else
            println(s"${t.offset} - ${t.created} - ${t.messageType} - ${t.contentAsString}")
        })
      }
    }

    log("No more messages to receive")
  }

  def disconnect(): Unit = {
    log("Disconnecting...")
    client.close()
    log(s"Disconnected.")
  }

  private[this] def log(x: String, outputToUser: Boolean = false): Unit = {
    if (verbose) println(Instant.now + ": " + x)
    if (outputToUser) println(x)
  }
}
