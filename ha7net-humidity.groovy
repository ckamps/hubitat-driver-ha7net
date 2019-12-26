def version() {"v0.1.20191225"}

@Grab(group='org.ccil.cowan.tagsoup', module='tagsoup', version='1.2')
import groovy.util.XmlSlurper

metadata {
    definition (name: "HA7Net 1-Wire Humidity Sensor", namespace: "ckamps", author: "Christopher Kampmeier", importUrl: "https://raw.githubusercontent.com/ckamps/hubitat-driver-ha7net/master/ha7net-humidity.groovy") {
	    capability "RelativeHumidityMeasurement"
        capability "Refresh"
    }

    preferences {
        input("ipAddress", "text", title: "IP Address", description: "[IP Address of your HA7Net device]", required: true)
        input("sensorId", "text", title: "1-Wire Sensor ID", description: "[Enter 1, 2 or 3]", required: true)
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
    humidity = element.@value.toString()
    log.debug("Humidity: ${humidity}");
	
    //float tmpValue = Float.parseFloat(value)
    //tmpValue = tmpValue.round(1)
    sendEvent(name: "humidity", value: humidity)
  
    def nowDay = new Date().format("MMM dd", location.timeZone)
    def nowTime = new Date().format("h:mm a", location.timeZone)
    sendEvent(name: "lastUpdated", value: nowDay + " at " + nowTime, displayed: false)
}