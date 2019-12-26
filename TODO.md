# To Do Items

* Standalone testing - https://community.hubitat.com/t/unit-testing-groovy-apps-and-drivers/3691/14

* Device Capabilities
  * https://docs.hubitat.com/index.php?title=Driver_Capability_List
    * RelativeHumidityMeasurement - https://docs.hubitat.com/index.php?title=Driver_Capability_List#RelativeHumidityMeasurement
    * TemperatureMeasurement - https://docs.hubitat.com/index.php?title=Driver_Capability_List#TemperatureMeasurement


* Using `sendEvent()`
```

def parse(String description) {
    if (logEnable) log.debug "parse(${description}) called"
    def parts = description.split(" ")
    def name  = parts.length>0?parts[0].trim():null
    def value = parts.length>1?parts[1].trim():null
    if (name && value) {
        // Offset the humidity based on preference
        float tmpValue = Float.parseFloat(value)
        if (humidityOffset) {
            tmpValue = tmpValue + humidityOffset.toFloat()
        }
        // Update device
        tmpValue = tmpValue.round(1)
        sendEvent(name: name, value: tmpValue)
        // Update lastUpdated date and time
        def nowDay = new Date().format("MMM dd", location.timeZone)
        def nowTime = new Date().format("h:mm a", location.timeZone)
        sendEvent(name: "lastUpdated", value: nowDay + " at " + nowTime, displayed: false)
    }
    else {
    	log.error "Missing either name or value.  Cannot parse!"
    }
}

```