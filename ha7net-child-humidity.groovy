def version() {'v0.1.2'}

metadata {
    definition (name: 'HA7Net 1-Wire - Child - Humidity',
                namespace: 'ckamps', 
                author: 'Christopher Kampmeier',
                importUrl: 'https://raw.githubusercontent.com/ckamps/hubitat-drivers-ha7net/master/ha7net-child-humidity.groovy') {
        
        capability 'RelativeHumidityMeasurement'

        capability 'Refresh'
    }

    preferences {
        input name: 'offset',    type: 'decimal', title: 'Humidity Offset',   description: '-n, +n or n to adjust sensor reading', range:'*..*'
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
    sensorId = device.deviceNetworkId
    if (logEnable) log.debug("Getting humidity for sensor: ${sensorId}")

    try {
        humidity = parent.getHumidity(sensorId)
    }
    catch (Exception e) {
        log.warn("Can't obtain humidity for sensor ${sensorId}")
        return
    }

    if (logEnable) log.debug("Humidity: ${humidity}")
    humidity = humidityOffset ? (humidity + humidityOffset) : humidity
    humidity = humidity.round(1)

    sendEvent(
        name: 'humidity',
        value: humidity,
        unit: "%",
        descriptionText: "Humidity is ${humidity}%",
    )
  
    def nowDay = new Date().format('MMM dd', location.timeZone)
    def nowTime = new Date().format('h:mm a', location.timeZone)

    sendEvent(
        name: 'lastUpdated',
        value: nowDay + " at " + nowTime,
        displayed: false
    )
}