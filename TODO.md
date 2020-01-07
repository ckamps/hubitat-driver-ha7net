# To Do Items

## Near Term Enhancements

* Support concurrency management by requesting a lock of the 1-Wire bus from the HA7Net at the start of each operation. See the [HA7Net User's Manual and Programmer's Guide](https://www.embeddeddatasystems.com/assets/images/supportFiles/manuals/UsersMan-HA7Net.pdf) for details.

* When recreating child devices, support an option to retain existing Device Name and Device Label values.

* Use published 1-Wire device family codes to discover supported 1-Wire sensors to request lists of specific sensor types from the HA7Net vs the current approach of getting all sensors from the HA7Net and blindly trying to determine if a sensor is a humidity sensor and then falling back to a temperature sensor.

## Longer Term Enhancements

* Exercise standalone testing - https://community.hubitat.com/t/unit-testing-groovy-apps-and-drivers/3691/14

* Obtain an OW-Server device and extend these drivers to support it.
