// Copyright (c) Microsoft. All rights reserved.

/** Send messages from IoT Devices to Azure IoT Hub
  */
object Main extends App {

  Parameters().build.parse(args, Parameters()) match {
    case Some(p) =>
      val device = new Device(p.hubName, p.deviceId, p.accessKey, p.verbose)

      device.sendMessage(p.content, p.contentFormat, p.contentType)

      while (!device.isReady) {
        if (p.verbose) println("Waiting for confirmation...")
        Thread.sleep(1000)
      }

      device.disconnect()

    case None =>
      sys.exit(-1)
  }
}
