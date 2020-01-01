def version() {'v0.1.1'}

metadata {
    definition (name: 'HA7Net 1-Wire - Child - Temperature',
                namespace: 'ckamps', 
                author: 'Christopher Kampmeier',
                importUrl: 'https://raw.githubusercontent.com/ckamps/hubitat-drivers-ha7net/master/ha7net-child-temperature.groovy') {
        
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
    sensorId = device.deviceNetworkId
    log.debug("Getting temperature for sensor: ${sensorId}")
    temp = parent.getTemperature(sensorId)

    log.debug("Temperature - C: ${temp}")
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