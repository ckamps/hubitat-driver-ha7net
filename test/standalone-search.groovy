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

// Change these settings for your HA7Net and particular 1-Wire sensor:
def ipAddress = '192.168.2.242'
// 10 - DS18S20
// 26 - DS2438
// 28 - DS18B20
def familyCode = '10'
lockId = getLock(ipAddress)

// Search for all sensors
def getList = new URL("http://${ipAddress}/1Wire/Search.html").openConnection()
def getListMessage = "FamilyCode=${familyCode}&LockID=${lockId}"
getList.setRequestMethod("POST")
getList.setDoOutput(true)
getList.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
getList.getOutputStream().write(getListMessage.getBytes("UTF-8"))
getListRC = getList.getResponseCode()
if(getListRC.equals(200)) {
    getListResult = getList.getInputStream().getText()
    println(getListResult)
}

document = parser.parseText(getListResult)
serializedDocument = XmlUtil.serialize(document)
println serializedDocument.replace("\n", "").replace("\r", "")
sensorElements = document.'**'.findAll{ it.@name.text().startsWith('Address_') }
println sensorElements.size()
sensorElements.each {
    println it.@value.text()
}

releaseLock(ipAddress, lockId)

def getLock(ipAddress) {
    def parser = new XmlSlurper(new org.ccil.cowan.tagsoup.Parser())

    def getLock = new URL("http://${ipAddress}/1Wire/GetLock.html").openConnection()
    def getLockResult = ''
    def lockId = ''
    getLock.setRequestMethod("GET")
    getLockRC = getLock.getResponseCode()
    println("getLockRC = ${getLockRC}")
    if(getLockRC.equals(200)) {
        getLockResult = getLock.getInputStream().getText()
        println(getLockResult)
    
        document = parser.parseText(getLockResult)
        serializedDocument = XmlUtil.serialize(document)
        println serializedDocument.replace("\n", "").replace("\r", "")
        element = document.'**'.find{ it.@name == 'LockID_0' }
        lockId = element.@value.text()
        println("Lock: ${lockId}")
        return(lockId)
    }
}

def releaseLock(ipAddress, lockId) {
    def parser = new XmlSlurper(new org.ccil.cowan.tagsoup.Parser())

    def relLock = new URL("http://${ipAddress}/1Wire/ReleaseLock.html").openConnection()
    def relLockMessage = "LockID=${lockId}"
    relLock.setRequestMethod("POST")
    relLock.setDoOutput(true)
    relLock.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
    relLock.getOutputStream().write(relLockMessage.getBytes("UTF-8"))
    relLockRC = relLock.getResponseCode()
    if(relLockRC.equals(200)) {
        relLockResult = relLock.getInputStream().getText()
        println(relLockResult)
    }
}