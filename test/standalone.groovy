#!/usr/local/bin/groovy

@Grab(group='org.ccil.cowan.tagsoup', module='tagsoup', version='1.2')
import groovy.util.XmlSlurper

def ipAddress = '192.168.2.242'
def sensorId = 'B000000083E09526'

    //log.debug "Params:  ${getParams()}"
    def post = new URL("http://${ipAddress}/1Wire/ReadHumidity.html").openConnection();

    def message = "Address_Array=${sensorId}"
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
	    //log.debug "error occured calling httpget ${e}"
      //log.debug(e.toString());
      //log.debug(e.getMessage());
      //log.debug(e.getStackTrace()); 
    }

    element = document.'**'.find{ it.@name == 'Humidity_0' };
    humidity = element.@value.toFloat().round(1)
    //tmpValue = tmpValue.round(1)
    println("Humidity: ${humidity}");

    element = document.'**'.find{ it.@name == 'Temperature_0' };
    temp_c = element.@value.toFloat().round(1)
    temp_f = ((9.0/5.0)*temp_c + 32).round(1);
    println("Temp: ${temp_f}");
	
    //float tmpValue = Float.parseFloat(value)
    //tmpValue = tmpValue.round(1)
    //sendEvent(name: "humidity", value: humidity)
  
    //def nowDay = new Date().format("MMM dd", location.timeZone)
    //def nowTime = new Date().format("h:mm a", location.timeZone)
    //sendEvent(name: "lastUpdated", value: nowDay + " at " + nowTime, displayed: false)