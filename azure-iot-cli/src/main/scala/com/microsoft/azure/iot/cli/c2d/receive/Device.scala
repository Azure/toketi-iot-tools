// Copyright (c) Microsoft. All rights reserved.

package com.microsoft.azure.iot.cli.c2d.receive

import java.time.Instant

import com.microsoft.azure.iothub.{DeviceClient, IotHubClientProtocol}

class Device(connectionString: String, verbose: Boolean) {

  private val protocol = IotHubClientProtocol.AMQPS

  val client = new DeviceClient(connectionString, protocol)

  def connect(): Unit = {
  }

  def receive(action: String): Unit = {
    client.setMessageCallback(new EventCallback(action, verbose), AnyRef)
    client.open()
  }

  def disconnect(): Unit = {
    client.close()
  }

  private[this] def log(x: String, outputToUser: Boolean = false): Unit = {
    if (verbose) println(Instant.now + ": " + x)
    if (outputToUser) println(x)
  }
}
