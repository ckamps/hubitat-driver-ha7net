def version() {'v0.1.6'}

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
    if (logEnable) log.debug("Getting temperature for sensor: ${sensorId}")

    try {
        temp = getTemperature(sensorId)
    }
    catch (Exception e) {
        log.warn("Can't obtain temperature for sensor ${sensorId}: ${e}")
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

private float getTemperature(sensorId) {
    def uri = "http://${parent.getHa7netAddress()}"
    def body = [Address_Array: "${sensorId}"]
    def path = '/1Wire/ReadTemperature.html'

    response = parent.doHttpPost(uri, path, body)

    if (!response) throw new Exception("doHttpPost to get temperature returned empty response ${sensorId}")

    element = response.'**'.find{ it.@name == 'Temperature_0' }
    
    if (!element) throw new Exception("Can't find Temperature_0 element in response from HA7Net for sensor ${sensorId}")

    if (!element.@value) throw new Exception("Empty value in Temperature_0 element in response from HA7Net for sensor ${sensorId}")

    return(element.@value.toFloat())
}