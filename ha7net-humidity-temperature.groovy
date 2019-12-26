def version() {"v0.1.20191226"}

// QUESTION: Is grabbing external modules supported in Hubitat device drivers?
@Grab(group='org.ccil.cowan.tagsoup', module='tagsoup', version='1.2')
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

// QUESTION: Given that the HA7Net needs to be polled in order to obtain sensor data, which
// functions/events need to be supported in this device driver? Is the refresh() function
// sufficient?
def refresh() {
    def post = new URL('http://${ipAddress}/1Wire/ReadHumidity.html').openConnection();

    def message = 'Address_Array=${sensorId}'
    post.setRequestMethod("POST")
    post.setDoOutput(true)
    post.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
    post.getOutputStream().write(message.getBytes("UTF-8"));
    def postRC = post.getResponseCode();
    if(postRC.equals(200)) {
        resultText = post.getInputStream().getText()
    }

    def parser = new XmlSlurper(new org.ccil.cowan.tagsoup.Parser())

    try { 
        document = parser.parseText(resultText)
    } catch(Exception e) {
        log.debug "error occured calling httpget ${e}"
        log.debug(e.toString());
        log.debug(e.getMessage());
        log.debug(e.getStackTrace()); 
    }

    element = document.'**'.find{ it.@name == 'Humidity_0' };
    humidity = element.@value.toFloat().round(1)
    log.debug("Humidity: ${humidity}");
    sendEvent(name: "humidity", value: humidity)

    // TO DO: Also send "unit"

    // TODO: Add logic to take into account location.temperatureScale
    element = document.'**'.find{ it.@name == 'Temperature_0' };
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