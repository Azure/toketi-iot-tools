# Azure IoT Developer Command Line Interface

A **cross-platform** command line tool, to help **developing, testing and debugging** messaging 
from & to **Internet connected devices** and 
[**Azure IoT Hub**](https://azure.microsoft.com/en-us/services/iot-hub/).

Currently supported commands:

1. Send message from a Device to Azure IoT Hub
2. Receive messages sent by a Device to Azure IoT Hub
3. Send message to a Device
4. Receive and Accept/Reject a message sent to a Device
5. Check the status of a message sent to a Device

# Requirements, Download, Run

To run the command line interface, you'll need an environment with **Java 1.8+** and 
**SBT** (setup instructions:
[Windows](http://www.scala-sbt.org/release/docs/Installing-sbt-on-Windows.html),
[MacOS](http://www.scala-sbt.org/release/docs/Installing-sbt-on-Mac.html),
[Linux](http://www.scala-sbt.org/release/docs/Installing-sbt-on-Linux.html)
)

1. Check out the source code from GitHub
2. Execute `./build` (`build.bat` on Windows). This command generates a `azure-iot-cli.jar` file, 
   in the current folder, required by the `iot` command.
3. Run `./iot --help` (`iot.bat --help` on Windows)
 
# Authentication

All the required authentication parameters can be passed via command line parameters 
(see `iot --help`).
It is also possible to store the credentials in environment variables, to avoid exposing or
repeating the credentials for each command. Please note: depending on the command, some 
credentials are/are not required.

## Environment variables

1. `IOTCLI_HUB_NAME` : Azure IoT Hub name
2. `IOTCLI_ACCESS_POLICY` : access policy used by `c2d receive` / `c2d check` / `d2c receive`
3. `IOTCLI_ACCESS_KEY` : access key used by `c2d receive` / `c2d check` / `d2c receive`
4. `IOTCLI_DEVICE_ID` : device ID
5. `IOTCLI_DEVICE_KEY` : device authorization key used by `d2c send` / `c2d receive`
6. `IOTCLI_HUB_NAMESPACE` : namespace used by `d2c receive`

# Examples

## Send a typed message from a Device to Azure IoT Hub

```
# iot d2c send -t temperature 74
```

The value `temperature` passed to `-t` is user specific, i.e. Azure IoT Hub does not 
apply any logic to (not currently at least).

The *type* is currently stored in a user property with name `$$contentModel`. Similarly 
for the format, see next example.

## Send a JSON message from a Device to Azure IoT Hub

```
# iot d2c send -t temperature -f json '{"value":74}'
```

Note: as for `-t` also the value `json` passed to `-f` is a user value, up to the your application
to understand and process. The format value is stored in a user property with name `$$contentType`.

## Receive message sent by a Device to Azure IoT Hub

This command requires to know which partition holds the messages for a specific device. To receive
all the messages regardless, a future command 'd2c stream' based on 
[IoTHub React](https://github.com/Azure/toketi-iothubreact) is coming soon.

```
# iot d2c receive -s 3
1812509505056 - 2016-12-06T02:00:14.056Z - temperature - 74
... message 2 ...
... message 3 ...
...
```

## Send a text message to a Device

```
# iot c2d send reboot
Message '8ab83665-d2af-4d5d-bd18-64795f75d106' for device 'device1001' added to the queue.
```

## Start receiving and Reject messages sent to a Device

```
# iot c2d receive --action REJECT
Press enter to exit...
Received message:
  ID: 8ab83665-d2af-4d5d-bd18-64795f75d106
  Content: reboot
Replying to the message with `REJECT`
```

## Check the status of a message sent to a Device

```
# iot c2d check -i f4a7f441-e9ce-4213-9a8b-1dac1dd73870
Message 'f4a7f441-e9ce-4213-9a8b-1dac1dd73870' status:
  Enqueue time: 2016-12-06T02:07:06.110023600Z
  Description: Message rejected
  Status code: unknown
```

## Debugging

Add `-v` to any command to enable verbose logging and see how the tool interacts with Azure IoT Hub.

Example 1:

```
# iot d2c send -v -t temperature 74
2016-12-06T01:54:48.020Z: Correlation ID: e2e42789-11a0-4fb9-9435-c93ebcf1af25
2016-12-06T01:54:48.096Z: Retrieving Hub namespace from environment var `IOTCLI_HUB_NAMESPACE`
2016-12-06T01:54:48.098Z: Retrieving Hub name from environment var `IOTCLI_HUB_NAME`
2016-12-06T01:54:48.099Z: Retrieving Hub access policy from environment var `IOTCLI_ACCESS_POLICY`
2016-12-06T01:54:48.100Z: Retrieving Device ID from environment var `IOTCLI_DEVICE_ID`
2016-12-06T01:54:48.100Z: Retrieving Device Key from environment var `IOTCLI_DEVICE_KEY`
2016-12-06T01:54:48.101Z: Hub name: myAzureIoTHub
2016-12-06T01:54:48.106Z: Connecting...
2016-12-06T01:54:48.110Z: Preparing message...
2016-12-06T01:54:48.113Z: Sending message...
2016-12-06T01:54:48.114Z: Waiting for confirmation...
2016-12-06T01:54:48.752Z: Message sent.
2016-12-06T01:54:49.116Z: Message ID: 0a6ca4ab-96fb-4a20-892e-71ada615b6e3
2016-12-06T01:54:49.116Z: Disconnecting...
2016-12-06T01:54:49.116Z: Disconnected.
Done.
```

Example 2:

```
# iot d2c receive -s 3 -v
2016-12-06T01:55:18.383Z: Correlation ID: 05f06ec0-b9d3-4e0d-8588-8206df5b20ec
2016-12-06T01:55:18.457Z: Retrieving Hub namespace from environment var `IOTCLI_HUB_NAMESPACE`
2016-12-06T01:55:18.459Z: Retrieving Hub name from environment var `IOTCLI_HUB_NAME`
2016-12-06T01:55:18.460Z: Retrieving Hub access policy from environment var `IOTCLI_ACCESS_POLICY`
2016-12-06T01:55:18.461Z: Retrieving Device ID from environment var `IOTCLI_DEVICE_ID`
2016-12-06T01:55:18.462Z: Retrieving Access Key from environment var `IOTCLI_ACCESS_KEY`
2016-12-06T01:55:18.475Z: Hub name: myAzureIoTHub
2016-12-06T01:55:20.179Z: Connecting device `device1001`...
2016-12-06T01:55:20.346Z: Receiver ready, partition 3, start 2016-12-06T01:55:18.353Z
2016-12-06T01:55:20.346Z: Downloading messages
1812509505056 - 2016-12-06T02:00:14.056Z - temperature - 74
... message 2 ...
... message 3 ...
...
```

Example 3:

```
# iot c2d send -v reboot
2016-12-06T01:59:54.493Z: Correlation ID: 7aaf6dae-86fc-498c-95dc-29b73b8a7f87
2016-12-06T01:59:54.571Z: Retrieving Hub namespace from environment var `IOTCLI_HUB_NAMESPACE`
2016-12-06T01:59:54.572Z: Retrieving Hub name from environment var `IOTCLI_HUB_NAME`
2016-12-06T01:59:54.573Z: Retrieving Hub access policy from environment var `IOTCLI_ACCESS_POLICY`
2016-12-06T01:59:54.574Z: Retrieving Device ID from environment var `IOTCLI_DEVICE_ID`
2016-12-06T01:59:54.575Z: Retrieving Access Key from environment var `IOTCLI_ACCESS_KEY`
2016-12-06T01:59:54.575Z: Hub name: myAzureIoTHub
2016-12-06T01:59:54.702Z: Connecting...
2016-12-06T01:59:54.702Z: Opening service client connection...
2016-12-06T01:59:54.703Z: Opening feedback receiver connection...
2016-12-06T01:59:54.704Z: Preparing message, expires in 3600 seconds...
2016-12-06T01:59:54.708Z: Sending message...
2016-12-06T01:59:56.372Z: Message ID: 67b95019-4788-4a34-b2a9-637e9190dd0e
2016-12-06T01:59:56.372Z: Message '67b95019-4788-4a34-b2a9-637e9190dd0e' for device 'device1001' added to the queue.
Message '67b95019-4788-4a34-b2a9-637e9190dd0e' for device 'device1001' added to the queue.
Done.
```

# Known issues

The CLI tool is meant for **debugging/development purpose**. Some known issues that you might 
encounter:

* Message type cannot contain some special chars like `/`
  (SDK issue [#1017](https://github.com/Azure/azure-iot-sdks/issues/1017)).
* The status of enqueued C2D messages shows as "Successful" when not received yet
  (SDK issue [#993](https://github.com/Azure/azure-iot-sdks/issues/993)).
* The status of rejected C2D messages shows as "unknown".
  (SDK issue [#1015](https://github.com/Azure/azure-iot-sdks/issues/1015)).
* The status of abandoned C2D messages shows as "unknown".
  (SDK issue [#1016](https://github.com/Azure/azure-iot-sdks/issues/1016)).
* Concurrent executions of device commands can crash
  (SDK issue [#995](https://github.com/Azure/azure-iot-sdks/issues/995))
* Sometimes the internal client times out. The `c2d check` command in 
  particular can be very slow due to a feature under development. Try to increase the 
  timeout parameter.
  (SDK issue [#989](https://github.com/Azure/azure-iot-sdks/issues/995))
* Sending messages containing blank spaces is not fully supported (yet).

Please report any other issue you encounter, to help us improve!

# Future work

* Streaming from all partitions
* Support Device Twins
* Support Device Methods
* ...and more... let us know what you would like to see!

# Contribute Code

If you want/plan to contribute, we ask you to sign a [CLA](https://cla.microsoft.com/) 
(Contribution license Agreement). A friendly bot will remind you about it when you submit 
a pull-request.
