def version() {'v0.1.2'}

metadata {
    definition (name: 'HA7Net 1-Wire - Child - Temperature (H)',
                namespace: 'ckamps', 
                author: 'Christopher Kampmeier',
                importUrl: 'https://raw.githubusercontent.com/ckamps/hubitat-drivers-ha7net/master/ha7net-child-temperature-h.groovy') {
        
        capability 'TemperatureMeasurement'

        capability 'Refresh'
    }

    preferences {
        input name: 'offset',    type: 'decimal', title: 'Temperature Offset',   description: '-n, +n or n to adjust sensor reading', range:'*..*'
        input name: 'logEnable', type: 'bool',    title: 'Enable debug logging', defaultValue: false
    }
}

def installed() {
    initialize()
}

def updated() {
    initialize()   
}

def initialize() {
    state.version = version()
}

def poll() {
    refresh()   
}

def refresh() {
    // Since AAG TAI-8540 sensors can have the same 1-Wire ID for both humidity and temp, by convention, we appended
    // a trailing ".1" to the 1-Wire ID when we registered the temperature device.
    sensorId = device.deviceNetworkId.substring(0, device.deviceNetworkId.length() - 2)
    if (logEnable) log.debug("Getting temperature for sensor: ${sensorId}")

    try {
        temp = parent.getTemperatureH(sensorId)
    }
    catch (Exception e) {
        log.warn("Can't obtain temperature for sensor ${sensorId}")
        return
    }

    if (logEnable) log.debug("Temperature - C: ${temp}")
    temp = (location.temperatureScale == "F") ? ((temp * 1.8) + 32) : temp
    temp = offset ? (temp + offset) : temp
    temp = temp.round(2)

    sendEvent(
        name: 'temperature',
        value: temp,
        unit: "°${location.temperatureScale}",
        descriptionText: "Temperature is ${temp}°${location.temperatureScale}",
        translatable: true
    )
  
    def nowDay = new Date().format('MMM dd', location.timeZone)
    def nowTime = new Date().format('h:mm a', location.timeZone)

    sendEvent(
        name: 'lastUpdated',
        value: nowDay + " at " + nowTime,
        displayed: false
    )
}