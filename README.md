# Hubitat Drivers for HA7Net and 1-Wire Devices

The `ha7net-temp-humidity.groovy` driver is in a working state.

### Usage

1. Deploy the `ha7net-temp-humidity.groovy` driver to Hubitat.
1. For each 1-wire sensor of interest:
    1. Create a virtual device while specifying this driver.
    1. Set the HA7Net IP address and the 1-Wire sensor ID of interest.
    1. Optionally set offsets for temperature and humidity in the form of "-n", "n", or "+n".
    1. Press the "Refresh" button on the virtual device to obtain the current humidity and temperature.

Since the HA7Net is not actively sending sensor data to Hubitat, you'll typically want to set up a rule in Rules Manager (RM) to periodically issue a refresh of each sensor device.

## Enhancements in Progress
A replacement set of drivers is under development to take advantage of the parent-child device capabilities of Hubitat so that each HA7Net will be represented as a parent device while each 1-Wire sensor known to the HA7Net will be automatically created as a child of the parent HA7Net device.

After the automatic registration of child devices occurs, you will be able to go into each child and override the default name of the sensor so that it represents the sensor's purpose and/or location.

The driver `ha7net-parent.groovy` is an example of this newer set of drivers, but it is not yet feature complete.