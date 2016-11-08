// Copyright (c) Microsoft. All rights reserved.

import java.util.concurrent.TimeUnit

import com.microsoft.azure.eventhubs.EventHubClient
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.duration._
import scala.language.postfixOps

object Configuration {

  private[this] val confPath = "iothub-receive."

  // Maximum size supported by the client
  private[this] val MaxBatchSize = 999

  // Default IoThub client timeout
  private[this] val DefaultReceiverTimeout = 3 seconds

  private[this] val conf: Config = ConfigFactory.load()

  // IoT hub storage details
  val iotHubName     : String = try {
    conf.getString(confPath + "name")
  } catch {
    case e: Exception => ""
  }
  val iotHubNamespace: String = try {
    conf.getString(confPath + "namespace")
  } catch {
    case e: Exception => ""
  }
  val iotHubKeyName  : String = try {
    conf.getString(confPath + "keyName")
  } catch {
    case e: Exception => ""
  }
  val iotHubKey      : String = try {
    conf.getString(confPath + "key")
  } catch {
    case e: Exception => ""
  }

  // Consumer group used to retrieve messages
  // @see https://azure.microsoft.com/en-us/documentation/articles/event-hubs-overview
  private[this] val tmpCG = try {
    conf.getString(confPath + "consumerGroup")
  } catch {
    case e: Exception => ""
  }
  val receiverConsumerGroup: String =
    tmpCG.toUpperCase match {
      case "$DEFAULT" ⇒ EventHubClient.DEFAULT_CONSUMER_GROUP_NAME
      case "DEFAULT"  ⇒ EventHubClient.DEFAULT_CONSUMER_GROUP_NAME
      case _          ⇒ tmpCG
    }

  // Message retrieval timeout in milliseconds
  private[this] val tmpRTO = try {
    conf.getDuration(confPath + "receiverTimeout").toMillis
  } catch {
    case e: Exception => DefaultReceiverTimeout.toMillis
  }
  val receiverTimeout: FiniteDuration =
    if (tmpRTO > 0)
      FiniteDuration(tmpRTO, TimeUnit.MILLISECONDS)
    else
      DefaultReceiverTimeout

  // How many messages to retrieve on each call to the storage
  private[this] val tmpRBS = try {
    conf.getInt(confPath + "receiverBatchSize")
  } catch {
    case e: Exception => MaxBatchSize
  }
  val receiverBatchSize: Int =
    if (tmpRBS > 0 && tmpRBS <= MaxBatchSize)
      tmpRBS
    else
      MaxBatchSize
}
