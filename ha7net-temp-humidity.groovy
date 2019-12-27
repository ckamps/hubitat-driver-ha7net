def version() {"v0.1.20191226"}

metadata {
    definition (name: 'HA7Net 1-Wire Temperature and Humidity Sensor',
                namespace: 'ckamps', 
                author: 'Christopher Kampmeier',
                importUrl: 'https://raw.githubusercontent.com/ckamps/hubitat-drivers-ha7net/master/ha7net-temp-humidity.groovy') {
        capability 'RelativeHumidityMeasurement'
        capability 'TemperatureMeasurement'
        capability 'Refresh'
    }

    preferences {
        input name: 'ipAddress',      type: 'text',    title: 'HA7Net Address',            description: 'Either FQDN or IP Address of an HA7Net device', required: true
        input name: 'sensorId',       type: 'text',    title: '1-Wire Sensor ID',          description: '', required: true
        input name: 'humiditySensor', type: 'bool',    title: 'Includes humidity sensor?', description: ''
        input name: 'tempOffset',     type: 'decimal', title: 'Temperature Offset',        description: '-n, +n or n to adjust sensor reading', range:'*..*'
        input name: 'humidityOffset', type: 'decimal', title: 'Humidity Offset',           description: '-n, +n or n to adjust sensor reading', range: '*..*'
        input name: 'logEnable',      type: 'bool',    title: 'Enable debug logging',      defaultValue: true
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
    def uri = "http://${ipAddress}";
    def body = [Address_Array: "${sensorId}"];
    def path = humiditySensor ? '/1Wire/ReadHumidity.html' : '/1Wire/ReadTemperature.html'
    try {
        httpPost( [uri: uri, path: path, body: body, requestContentType: 'application/x-www-form-urlencoded'] ) { resp ->
            if (resp.success) {
                processResponse(resp.data)
            }
            if (logEnable)
                if (resp.data) log.debug "${resp.data}"
        } 
    } catch (Exception e) {
        log.warn "Call to refresh() failed: ${e.message}"
    }
}

private def processResponse(response) {

    if (humiditySensor) {
        element = response.'**'.find{ it.@name == 'Humidity_0' };
        humidity = element.@value.toFloat()
        log.debug("Humidity: ${humidity}");
        humidity = humidityOffset ? (humidity + humidityOffset) : humidity
        humidity = humidity.round(1)

	    sendEvent([
		    name: 'humidity',
		    value: humidity,
		    unit: "%",
		    descriptionText: "Humidity is ${humidity}%",
	    ])
    }

    element = response.'**'.find{ it.@name == 'Temperature_0' };
    temp = element.@value.toFloat()
    log.debug("Temperature - C: ${temp}");
    temp = (location.temperatureScale == "F") ? ((temp * 1.8) + 32) : temp
    temp = tempOffset ? (temp + tempOffset) : temp
    temp = temp.round(2)

    sendEvent([
		name: 'temperature',
		value: temp,
		unit: "°${location.temperatureScale}",
		descriptionText: "Temperature is ${temp}°${location.temperatureScale}",
		translatable:true
	])
  
    def nowDay = new Date().format("MMM dd", location.timeZone)
    def nowTime = new Date().format("h:mm a", location.timeZone)
    sendEvent(name: "lastUpdated", value: nowDay + " at " + nowTime, displayed: false)
}