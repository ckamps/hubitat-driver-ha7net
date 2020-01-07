# Hubitat Drivers for HA7Net and 1-Wire Devices

This set of [Hubitat Elevation](https://hubitat.com/) device drivers enable you to integrate an [HA7Net](https://www.embeddeddatasystems.com/HA7Net--Ethernet-1-Wire-Host-Adapter_p_22.html) appliance with Hubitat.  It consists of a parent driver that manages the overall interaction with an HA7Net and set of child drivers to represent different types of 1-Wire sensors supported by the HA7Net.

## About the HA7Net
An HA7Net provides a simple interactive web interface over an ethernet interface to interact with a set of 1-Wire sensors attached to the 1-Wire network ports. In order for sensor data to be made available in Hubitat, we use a set of device drivers to issue POST requests to the HA7Net to obtain sensor data. Typically, you would set up a Hubitat Rule Machine (RM) rule to periodically poll all of your 1-Wire sensors via the HA7Net.

Although firmware updates for the HA7Net are no longer available, the device supports a decent number of 1-Wire sensors (100), is pretty simple to use, and, in my experience, has been very stable over may years of use.  Since 1-Wire sensors are typically inexpensive, people who already have the HA7Net might want to keep using it as a gateway between their 1-Wire sensors and modern home automation platforms such as Hubitat.

## Support for Similar Devices

It should not be difficult to extend these drivers to support similar 1-Wire gateway appliances such as the [OW-Server](https://www.embeddeddatasystems.com/OW-SERVER-1-Wire-to-Ethernet-Server-Revision-2_p_152.html). This device supports just 22 sensors, but appears to offer a richer API as compared to the screen scrapping approach that is required to integrate with the HA7Net.

Similarly, use of [owserver](https://manpages.debian.org/testing/owserver/owserver.1.en.html) should also be failrly straightforward for those people who have deployed `owserver` on their own computers.

## Current State

Beta quality. Basic testing of these drivers has been done. See [TODO](TODO.md) for outstanding work.

## Supported 1-Wire Sensors

Although the HA7Net supports a broader set of 1-Wire sensors, these drivers have been tested with and currently support only these sensors:

|Sensor|Type|
|------|----|
|DS18B20|Temperature|
|DS18S20|Temperature|
|DS2438|Temperature|
|AAG TAI-8540|Humidity + temperature|

## Usage

### Deploy Drivers to Hubitat

|Driver File|Description|
|-----------|-----------|
|`ha7net-child-temperature.groovy`|Supports DS18B20 1-Wire temperature sensors.|
|`ha7net-child-humidity.groovy`|Supports humidity readings from the combined AAG TAI-8540 humidity + temperature sensor.|
|`ha7net-child-temperature-h.groovy`|Supports temperature readings from the combined AAG TAI-8540 humidity + temperature sensor.|
|`ha7net-parent.groovy`|Auto discovers 1-Wire sensors via HA7Net and creates child devices.|

### Create Virtual Device for the HA7Net

1. Select the driver "HA7Net 1-Wire - Parent".
1. Set the HA7Net IP address.
1. Press the "Refresh" button on the virtual device to discover the current set of 1-Wire sensors known to the HA7Net and to create child devices for each sensor.
1. Optionally, in each child device change the Device Name and/or Device Label to represent the function and/or location of the associated sensor.

### Parent Driver Commands

Within a virtual device associated with the parent driver, you can execute the following commands:

|Command|Description|
|-------|-----------|
|`createChildren`|Discover all sensors known to the HA7Net and create child devices as appropriate.  Loads current sensor reading into each child device.|
|`deleteChildren`|Delete all children.|
|`deleteUnmatchedChildren`|Not yet implemented, but its intent will be to delete child devices that are not known to the HA7Net.|
|`recreateChildren`|Deletes all children and create new children based on newly discovered sensors.|
|`refresh`|See `refreshChildren`.|
|`refreshChildren`|Updates the sensor reading for each child device by calling the `refresh()` method of each child.|

## Auto Refreshing Child Device Readings

Since the HA7Net is not actively sending sensor data to Hubitat, you'll typically want to set up a rule in Rule Machine (RM) to periodically trigger a refresh of the sensors by selecting an every n minutes (or whatever) trigger and an action of "refresh" on the parent device. Doing so will result in the `refreshChildren` command being sent to the parent.

## References

[HA7Net User's Manual and Programmer's Guide](https://www.embeddeddatasystems.com/assets/images/supportFiles/manuals/UsersMan-HA7Net.pdf)