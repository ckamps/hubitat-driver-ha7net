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

def parser = new XmlSlurper(new org.ccil.cowan.tagsoup.Parser())

def ipAddress = '192.168.2.242'
def sensorId = '580008014D22AF10'

def post = new URL("http://${ipAddress}/1Wire/ReadTemperature.html").openConnection()

def message = "Address_Array=${sensorId}"
post.setRequestMethod("POST")
post.setDoOutput(true)
post.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
post.getOutputStream().write(message.getBytes("UTF-8"))
def postRC = post.getResponseCode()
if(postRC.equals(200)) {
    resultText = post.getInputStream().getText()
}

println(resultText)

document = parser.parseText(resultText)

element = document.'**'.find{ it.@name == 'Temperature_0' }
temp_c = element.@value.toFloat().round(1)
println("Temp: ${temp_c} C")
temp_f = ((9.0/5.0)*temp_c + 32).round(1)
println("Temp: ${temp_f} F")