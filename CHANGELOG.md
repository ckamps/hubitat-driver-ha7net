# Changes for Hubitat HA7Net Drivers

## `ha7net-parent.groovy`

### 1.9

* Implemented family code based discovery of 1-Wire sensors.
  * [Issue #5](https://github.com/ckamps/hubitat-driver-ha7net/issues/5)
* When DS2438 sensors are found, a refined, but still less than ideal check has been implemented to distinguish between standalone temperature only sensors and cases in which a DS2438 is used as part of a combined temperature + humidity sensor. e.g. AAG TAI-8540 sensors.
  * [Issue #10](https://github.com/ckamps/hubitat-driver-ha7net/issues/10)

### 1.8

* Implemented first phase of concurrency management via locking for `Search.html` calls.
  * [Issue #3](https://github.com/ckamps/hubitat-driver-ha7net/issues/3)
* Enhanced debug logging.


## `ha7net-child-humidity.groovy`

### 0.1.8

* Fixed bug that was inhibiting the humidity offset parameter from taking effect.
  * [Issue #17](https://github.com/ckamps/hubitat-driver-ha7net/issues/17)
