def version() {"v0.1.20191226"}

import groovy.util.XmlSlurper

metadata {
    definition (name: "HA7Net 1-Wire Humidity and Temperature Sensor", namespace: "ckamps", author: "Christopher Kampmeier", importUrl: "https://raw.githubusercontent.com/ckamps/hubitat-drivers-ha7net/master/ha7net-humidity-temperature.groovy") {
        capability "RelativeHumidityMeasurement"
        capability "TemperatureMeasurement"
        capability "Refresh"
    }

    preferences {
        input name: "ipAddress", type: "text", title: "IP Address", description: "IP Address of your HA7Net device", required: true
        input name: "sensorId",  type: "text", title: "1-Wire Sensor ID", description: "Hexadecimal identifier", required: true
        input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: true
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
    try {
        httpPost( [uri: uri, path: '/1Wire/ReadHumidity.html', body: body, requestContentType: 'application/x-www-form-urlencoded'] ) { resp ->
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
    //def parser = new XmlSlurper()

    //try { 
    //    document = parser.parseText(response)
    //} catch(Exception e) {
    //    log.debug "error occured when parsing response data: ${e}"
    //    log.debug(e.toString());
    //    log.debug(e.getMessage());
    //    log.debug(e.getStackTrace()); 
    //}

    element = response.'**'.find{ it.@name == 'Humidity_0' };
    humidity = element.@value.toFloat().round(1)
    log.debug("Humidity: ${humidity}");
    sendEvent(name: "humidity", value: humidity)
    // TO DO: Also send "unit"

    // TODO: Add logic to take into account location.temperatureScale
    element = response.'**'.find{ it.@name == 'Temperature_0' };
    temp_c = element.@value.toFloat().round(1)
    temp_f = ((9.0/5.0)*temp_c + 32).round(1);
    log.debug("Temperature - F: ${temp_f}");
    log.debug("Temperature - C: ${temp_c}");
    sendEvent(name: "temperature", value: temp_f)
    // TO DO: Also send "unit"
  
    def nowDay = new Date().format("MMM dd", location.timeZone)
    def nowTime = new Date().format("h:mm a", location.timeZone)
    sendEvent(name: "lastUpdated", value: nowDay + " at " + nowTime, displayed: false)
}