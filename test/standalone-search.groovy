#!/usr/local/bin/groovy

// This code runs standalone outside of Hubitat as a means to experiment with interacting
// with an HA7Net.
//
// Notes:
//    * Since Hubitat provides drivers with a built-in `httpPost()` method to both
//      issue a post and automatically parse the resulting response, the driver
//      code is simpler than the following standalone code.
//    * Once we integrate a standalone test harness for Hubitat drivers, we can delete
//      this separate standlone test program.

@Grab(group='org.ccil.cowan.tagsoup', module='tagsoup', version='1.2')
import groovy.util.XmlSlurper
import groovy.xml.*

// Change these settings for your HA7Net and particular 1-Wire sensor:
def ipAddress = '192.168.2.242'
def sensorId = 'C1000000A68BCF26'

def post = new URL("http://${ipAddress}/1Wire/Search.html").openConnection();

def message = 'LockID=0';
post.setRequestMethod("POST")
post.setDoOutput(true)
post.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
post.getOutputStream().write(message.getBytes("UTF-8"));
def postRC = post.getResponseCode();
if(postRC.equals(200)) {
    resultText = post.getInputStream().getText()
}

println(resultText);

def parser = new XmlSlurper(new org.ccil.cowan.tagsoup.Parser())

document = parser.parseText(resultText)

serializedDocument = XmlUtil.serialize(document)

println serializedDocument.replace("\n", "").replace("\r", "")

sensorElements = document.'**'.findAll{ it.@name.text().startsWith('Address_') };

println sensorElements.size()

sensorElements.each {
    println it.@value.text();
}

//document.'**'.findAll{ it.@class == 'HA7Value' && it.@name.text().startsWith('Address_') }.each {
//    println it.@value.text();
//}

//element = document.'**'.find{ it.@name == 'Humidity_0' };
//humidity = element.@value.toFloat().round(1)
//println("Humidity: ${humidity}");

//element = document.'**'.find{ it.@name == 'Temperature_0' };
//temp_c = element.@value.toFloat().round(1)
//temp_f = ((9.0/5.0)*temp_c + 32).round(1);
//println("Temp: ${temp_f}");