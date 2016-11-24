// Copyright (c) Microsoft. All rights reserved.

import com.typesafe.config.{Config, ConfigFactory}

import scala.language.postfixOps

object Configuration {

  private[this] val confPath = "iothub-send."

  private[this] val conf: Config = ConfigFactory.load()

  // IoT hub storage details
  val accessHostName: String = try {
    conf.getString(confPath + "accessHostName")
  } catch {
    case e: Exception => ""
  }

  val accessPolicy: String = try {
    conf.getString(confPath + "accessPolicy")
  } catch {
    case e: Exception => ""
  }

  val accessKey: String = try {
    conf.getString(confPath + "accessKey")
  } catch {
    case e: Exception => ""
  }
}
