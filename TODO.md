# To Do Items

## Tactical

* Finish adding temperature support to humidity and temperature driver.
* Add celsius vs temperature support based on `location.temperatureScale`.
* Add humity and temperature offset preferences.
* Enhance exception handling.
* Align logging with device driver best practices.
* Finish README.md.
* Exercise standalone testing - https://community.hubitat.com/t/unit-testing-groovy-apps-and-drivers/3691/14

## Examples of Rules Using Temperature and Humidity

* https://community.hubitat.com/t/question-re-else-event-not-triggering/19455/21

## References

* Look to the following example for inspiration:
  * https://github.com/muxa/hubitat/blob/master/drivers/konke-zigbee-temp-humidity-sensor.groovy

* Device Capabilities
  * https://docs.hubitat.com/index.php?title=Driver_Capability_List
    * RelativeHumidityMeasurement - https://docs.hubitat.com/index.php?title=Driver_Capability_List#RelativeHumidityMeasurement
    * TemperatureMeasurement - https://docs.hubitat.com/index.php?title=Driver_Capability_List#TemperatureMeasurement