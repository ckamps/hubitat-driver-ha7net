def version() {"v0.1.20191226"}

metadata {
    definition (name: 'HA7Net 1-Wire Parent',
                namespace: 'ckamps', 
                author: 'Christopher Kampmeier',
                importUrl: 'https://raw.githubusercontent.com/ckamps/hubitat-drivers-ha7net/master/ha7net-parent.groovy') {
        capability 'Refresh'
    }

    preferences {
        input name: 'address',        type: 'text',    title: 'HA7Net Address',            description: 'FQDN or IP address', required: true
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

def refresh() {
    def uri = "http://${address}";
    def body = [LockID: '0'];
    try {
        httpPost( [uri: uri, path: '/1Wire/Search.html', body: body, requestContentType: 'application/x-www-form-urlencoded'] ) { resp ->
            if (resp.success) {
                processSensors(resp.data)
            }
            if (logEnable)
                if (resp.data) log.debug "${resp.data}"
        } 
    } catch (Exception e) {
        log.warn "Call to refresh() failed: ${e.message}"
    }
}

private def processSensors(response) {
    response.'**'.findAll{ it.@class == 'HA7Value' && it.@name.text().startsWith('Address_') }.each { element ->
        log.debug element.@value
    }
}