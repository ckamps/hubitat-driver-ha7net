def version() {'v0.2.0'}

import groovy.xml.*

metadata {
    definition (name: 'HA7Net 1-Wire - Parent',
                namespace: 'ckamps', 
                author: 'Christopher Kampmeier',
                importUrl: 'https://raw.githubusercontent.com/ckamps/hubitat-drivers-ha7net/master/ha7net-parent.groovy') {
        
        capability 'Refresh'

        command 'createChildren'
        command 'deleteChildren'
        command 'deleteUnmatchedChildren'
        command 'recreateChildren'
        command 'refreshChildren'
    }

    preferences {
        input name: 'address',   type: 'text', title: 'HA7Net Address',       description: 'FQDN or IP address', required: true
        input name: 'logEnable', type: 'bool', title: 'Enable debug logging', defaultValue: false
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
    refreshChildren()
}

def createChildren() {
    if (logEnable) log.debug("Creating children devices")
    
    getSensorsDs18S20()
    getSensorsDs18B20()
    getSensorsDs2438()
}

def refreshChildren(){
    if (logEnable) log.info "Refreshing children devices"
    def children = getChildDevices()
    children.each {child->
        child.refresh()
    }
}

def recreateChildren(){
    if (logEnable) log.info "Recreating children devices"
    // To Do: Based on a new preference, capture the name and label of each child device and reapply those names and labels
    // for all discovered sensors that were previously known.
    deleteChildren()
    createChildren()
}

def deleteChildren() {
    if (logEnable) log.info "Deleting children devices"
    def children = getChildDevices()
    children.each {child->
        deleteChildDevice(child.deviceNetworkId)
    }
}

def deleteUnmatchedChildren() {
    if (logEnable) log.info "Deleting unmatched children devices"
    // To Do: Not yet implemnted.
    discoveredSensors = getSensors()
    getChildDevices().each { device ->
        if (logEnable) log.debug("Found an existing child device")
    }
}

private def getSensorsDs18S20() {
    if (logEnable) log.info "Getting DS18S20 family sensors"

    def sensors = []
    
    sensors = getSensors('10')

    sensors.each { sensorId ->
        if (getChildDevice(sensorId) == null) {
            if (logEnable) log.debug("Child device does not yet exist for DS18S20 sensor: ${sensorId}")
            child = addChildDevice("ckamps", "HA7Net 1-Wire - Child - Temperature", sensorId, [name: sensorId, label: "${sensorId} - DS18S20 Temperature", isComponent: false])
            child.refresh()
        } else {
            if (logEnable) log.debug("Child device already exists for DS18S20 sensor: ${sensorId}")
        }
    }

}

private def getSensorsDs18B20() {
    if (logEnable) log.info "Getting DS18B20 family sensors"

    def sensors = []
    
    sensors = getSensors('28')

    sensors.each { sensorId ->
        if (getChildDevice(sensorId) == null) {
            if (logEnable) log.debug("Child device does not yet exist for DS18B20 sensor: ${sensorId}")
            child = addChildDevice("ckamps", "HA7Net 1-Wire - Child - Temperature", sensorId, [name: sensorId, label: "${sensorId} - DS18B20 Temperature", isComponent: false])
            child.refresh()
        } else {
            if (logEnable) log.debug("Child device already exists for DS18B20 sensor: ${sensorId}")
        }
    }

}

private def getSensorsDs2438() {
    if (logEnable) log.info "Getting DS2438 family sensors"

    def sensors = []

    sensors = getSensors('26')

    sensors.each { sensorId ->
        if (getChildDevice(sensorId) == null) {
            if (logEnable) log.debug("Child device does not yet exist for DS2438 sensor: ${sensorId}")
            if (isDs2438TempOnly(sensorId)) {
                if (logEnable) log.debug "Discovered DS2438 temperature sensor: ${sensorId}"
                child = addChildDevice("ckamps", "HA7Net 1-Wire - Child - Temperature", sensorId, [name: sensorId, label: "${sensorId} - DS2438 Temperature", isComponent: false])
                child.refresh()
            } else {
                if (logEnable) log.debug "Discovered DS2438 temperature + humidity sensor: ${sensorId}"
                child = addChildDevice("ckamps", "HA7Net 1-Wire - Child - Humidity", sensorId, [name: sensorId, label: "${sensorId} - DS2438 Humidity + Temperature" , isComponent: false])
                child.refresh()
		    }
        } else {
            if (logEnable) log.debug("Child device already exists for DS2438 sensor: ${sensorId}")
        }
    }
}

private def isDs2438TempOnly(sensorId) {
    // The following logic is still a kludge in that the current basis for identifying standalone
    // DS2438 sensors is not foolproof. We'll likely need to send lower level commands to the sensor
    // and inspect how it's configured.

    if (logEnable) log.info "Determining if DS2438 sensor is temperature only: ${sensorId}"

    def uri = "http://${address}"
    def path = '/1Wire/ReadHumidity.html'
    def body = [Address_Array: "${sensorId}"]

    response = doHttpPost(uri, path, body)

    if (!response) throw new Exception("doHttpPost to get humidity from sensor returned empty response ${sensorId}")

    element = response.'**'.find{ it.@name == 'Humidity_0' }
    
    if (!element) throw new Exception("Can't find Humidity_0 element in response from HA7Net for sensor ${sensorId}")

    if (!element.@value) throw new Exception("Empty value in Humidity_0 element in response from HA7Net for sensor ${sensorId}")
    
    humidity = element.@value.toFloat()
    if (logEnable) log.info "Humidity from DS2438 sensor is: ${humidity}"

    // We've seen the HA7Net return RH readings far in excess of 100% RH for standalone DS2438 sensors.
    if (humidity > 150) {
        return true
    } else {
        return false
    }
}

private def getSensors(familyCode) {
    if (logEnable) log.info "Getting list of sensors known to HA7Net for family code: ${familyCode}"

    lockId = getLock()

    def uri = "http://${address}"
    def path = '/1Wire/Search.html'
    def body = [LockID: lockId, FamilyCode: familyCode]

    response = doHttpPost(uri, path, body)

    relLock(lockId)

    def discoveredSensors = []
    def sensorElements = []

    // We should be able to modify this findAll statement to construct an array of sensor IDs
    // as opposed to depending on the each loop.
    //
    // Something like the following but I have yet to get the right hand side correct:
    //
    // discoveredSensors = response.'**'.findAll{ it.@name.text().startsWith('Address_') }.*.value.text()

    sensorElements = response.'**'.findAll{ it.@name.text().startsWith('Address_') }
    
    if (logEnable) log.debug("number of sensor elements found: ${sensorElements.size()}")
    
    sensorElements.each {
        def sensorId = it.@value.text()
        if (logEnable) log.debug("Sensor discovered - value: ${sensorId}")
        discoveredSensors.add(sensorId)
    }

    return(discoveredSensors)
}

def doHttpPost(uri, path, body) {
    if (logEnable) log.debug("doHttpPost called: uri: ${uri} path: ${path} body: ${body}")
    def response = []
    int retries = 0
    def cmds = []
    cmds << 'delay 1'

    // Attempt a max of 3 retries to address cases in which transient read errors can occur when 
    // interacting with the HA7Net.
    while(retries++ < 3) {
        try {
            httpPost( [uri: uri, path: path, body: body, requestContentType: 'application/x-www-form-urlencoded'] ) { resp ->
                if (resp.success) {
                    response = resp.data
                    if ((logEnable) && (response.data)) {
                        serializedDocument = XmlUtil.serialize(response)
                        log.debug(serializedDocument.replace('\n', '').replace('\r', ''))
                    }
                } else {
                    throw new Exception("httpPost() not successful for: ${uri} ${path}") 
                }
            }
            return(response)
        } catch (Exception e) {
            log.warn "httpPost() of ${path} to HA7Net failed: ${e.message}"
            // When read time out error occurs, retry the operation. Otherwise, throw
            // an exception.
            if (!e.message.contains('Read timed out')) throw new Exception("httpPost() failed for: ${uri} ${path}")
        }
        log.warn('Delaying 1 second before next httpPost() retry')
        cmds
    }
    throw new Exception("httpPost() exceeded max retries for: ${uri} ${path}")
}

// To Do: Is there a more direct means for child devices to access parent preferences/settings?

def getHa7netAddress() {
    return(address)   
}

// HA7Net and Locking: getLock() and relLock()
//
// The following methods are currently used when accessing the /1Wire/Search.html interface to
// avoid conflicts when multiple clients and/or users attempt to access the HA7Net and 1-Wire
// bus at the same time.
//
// Note that these methods are not used when accessing the higher level interfaces of
// the HA7Net such as /1Wire/ReadHumidity.html and /1Wire/ReadTemperature.html.  It is believed
// that for these higher level interfaces, the HA7Net performs its own concurrency management
// as it carries out multiple actions on the 1-Wire bus for each invocation of the higher level
// interface.  It is believed that when multiple requests for higher level interfaces occur, the
// HA7Net will queue them until either the currently executing higher level action has completed or
// a lock obtained by a client is either released or expires.
//
// See the HA7Net User's Manual for an overview of concurrency management:
//
// https://www.embeddeddatasystems.com/assets/images/supportFiles/manuals/UsersMan-HA7Net.pdf

private def getLock() {
    if (logEnable) log.debug("Attempting to obtain 1-Wire network lock")
    def response = []
    def uri = "http://${address}/1Wire/GetLock.html"

    httpGet(uri) { resp ->
        if (resp.success) {
            response = resp.data
            if ((logEnable) && (response.data)) {
                serializedDocument = XmlUtil.serialize(response)
                log.debug(serializedDocument.replace('\n', '').replace('\r', ''))
            }
        } else {
            throw new Exception("httpGet() not successful for: ${uri} ${path}") 
        }
    }
    element = response.'**'.find{ it.@name == 'LockID_0' }
    
    if (!element.@value) throw new Exception("Empty value in LockID_0 element in response from HA7Net")

    lockId = element.@value.text()

    if (logEnable) log.debug("1-Wire network lock obtained successfully: ${lockId}")

    return(lockId)
}

private def relLock(lockId) {
    if (logEnable) log.debug("Attempting to release 1-Wire network lock: ${lockId}")
    def response = []
    def uri = "http://${address}"
    def path = '/1Wire/ReleaseLock.html'

    def body = [LockID: lockId]

    response = doHttpPost(uri, path, body)

    if (!response) throw new Exception("doHttpPost to release lock returned empty response")

    element = response.'**'.find{ it.@name == 'Exception_Code_0' }
    
    if (!element.@value) throw new Exception("Empty value in Exception_Code_0 element in release lock response from HA7Net")

    if (element.@value != '0') throw new Exception("Non zero value in Exception_Code_0 element in release lock response from HA7Net: ${element.@value}")

    if (logEnable) log.debug("1-Wire network lock released successfully: ${lockId}")
}
