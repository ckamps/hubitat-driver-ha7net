# Hubitat Drivers for HA7Net and 1-Wire Devices

The `ha7net-temp-humidity.groovy` driver is in a working state.

## Usage

1. Deploy the following drivers to Hubitat:
    * `ha7net-child-temperature.groovy` - Supports DS18B20 1-Wire temperature sensors.
    * `ha7net-child-humidity.groovy` - Supports humidity readings from the combined AAG TAI-8540 humidity + temperature sensor.
    * `ha7net-child-temperature-h.groovy` - Supports temperature readings from the combined AAG TAI-8540 humidity + temperature sensor.
    * `ha7net-parent.groovy` - Auto discovers 1-Wire sensors via HA7Net and creates child devices.
1. Create a virtual device to represent an HA7Net appliance.
    1. Select the driver "HA7Net 1-Wire - Parent".
    1. Set the HA7Net IP address.
    1. Press the "Refresh" button on the virtual device to discover the current set of 1-Wire sensors known to the HA7Net and to create child devices for each sensor.
    1. Optionally, in each child device change the Device Name and/or Device Label to represent the function and/or location of the associated sensor.

## Auto Refreshing Child Device Readings

Since the HA7Net is not actively sending sensor data to Hubitat, you'll typically want to set up a rule in Rules Manager (RM) to periodically issue a refresh of the sensors.  In a timer-based rule, you can specify the command `refreshChildDevices` to force a refresh of all child devices.