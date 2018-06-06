def smartAppNameFull() {
    return  "BatteryMonitor SmartApp for SmartThings"
}

def smartAppNameShort() {
    return  "BatteryMonitor"
}

def smartAppVersion() {
    return  "Version 0.0.5"
}

def smartAppAuthor() {
    return  "Author Brandon Gordon"
}

def smartAppCopyright() {
    return  "Copyright (c) 2014 Brandon Gordon"
}

def smartAppSource() {
    return  "https://github.com/notoriousbdg/SmartThings.BatteryMonitor"
}

def smartAppDescription() {
    return  "This SmartApp helps you monitor the status of your SmartThings devices with batteries."
}

def smartAppRevision () {
    return  '2014-11-14  v0.0.1\n' +
            ' * Initial release\n\n' +
            '2014-11-15  v0.0.2\n' +
            ' * Moved status to main page\n' +
            ' * Removed status page\n' +
            ' * Improved formatting of status page\n' +
            ' * Added low, medium, high thresholds\n' +
            ' * Handle battery status strings of OK and Low\n\n' +
            '2014-11-15  v0.0.3\n' +
            ' * Added push notifications\n\n' +
            '2014-11-20  v0.0.4\n' +
            ' * Added error handling for batteries that return strings\n\n' +
            '2014-12-26  v0.0.5\n' +
            ' * Move app metadata to a new about page\n' +
            ' * Changed notifications to only send at specified time daily\n'
}

def smartAppLicense() {
    return  'Licensed under the Apache License, Version 2.0 (the "License"); you ' +
            'may not use this file except in compliance with the License. You ' +
            'may obtain a copy of the License at:' +
            '\n\n' +
            'http://www.apache.org/licenses/LICENSE-2.0' +
            '\n\n' +
            'Unless required by applicable law or agreed to in writing, software ' +
            'distributed under the License is distributed on an "AS IS" BASIS, ' +
            'WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or ' +
            'implied. See the License for the specific language governing ' +
            'permissions and limitations under the License.'
}



/**
 *  Battery Monitor
 *
 *  Copyright 2018 Wyatt Neal
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "Battery Monitor",
    namespace: "wyattearp",
    author: "Wyatt Neal",
    description: "Monitor the battery information on a schedule",
    category: "Safety & Security",
    iconUrl: "http://cdn.device-icons.smartthings.com/Health%20&%20Wellness/health9-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Health%20&%20Wellness/health9-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Health%20&%20Wellness/health9-icn@2x.png")


preferences {
    page name:"pageStatus"
    page name:"pageConfigure"
    page name:"pageAbout"
}

// Show About Page
def pageAbout() {
    def pageProperties = [
        name:           "pageAbout",
        title:          smartAppNameFull(),
        nextPage:       "pageConfigure",
        uninstall:      true
    ]

    return dynamicPage(pageProperties) {
        section() {
            paragraph smartAppVersion() + "\n" +
                      smartAppAuthor() + "\n" +
                      smartAppCopyright()
        }
        
        section() {
            paragraph smartAppDescription()
        }
        
        section() {
            href(
                name: "sourceCode",
                title: "Source Code (Tap to view)",
                required: false,
                external: true,
                style: "external",
                url: smartAppSource(),
                description: smartAppSource()
            )
        }

        section() {
            paragraph title: "Revision History",
                      smartAppRevision()
        }
        
        section() {
            paragraph title: "License",
                      smartAppLicense()
        }  
    }
}

// Show Status page
def pageStatus() {
    def pageProperties = [
        name:       "pageStatus",
        title:      smartAppNameShort() + " Status",
        nextPage:   null,
        install:    true,
        uninstall:  true
    ]

    if (settings.devices == null) {
        return pageAbout()
    }
    
    def listLevel0_NoCharge = ""
    def listLevel1_LowCharge = ""
    def listLevel2_MediumCharge = ""
    def listLevel3_HighCharge = ""
    def listLevel4_FullCharge = ""

    if (settings.level1 == null) { settings.level1 = 33 }
    if (settings.level3 == null) { settings.level3 = 67 }
    if (settings.pushMessage) { settings.pushMessage = true }
    
    return dynamicPage(pageProperties) {
        settings.devices.each() {
            try {
                if (it.currentBattery == null) {
                    listLevel0_NoCharge += "$it.displayName\n"
                } else if (it.currentBattery >= 0 && it.currentBattery <  settings.level1.toInteger()) {
                    listLevel1_LowCharge += "$it.currentBattery  $it.displayName\n"
                } else if (it.currentBattery >= settings.level1.toInteger() && it.currentBattery <= settings.level3.toInteger()) {
                    listLevel2_MediumCharge += "$it.currentBattery  $it.displayName\n"
                } else if (it.currentBattery >  settings.level3.toInteger() && it.currentBattery < 100) {
                    listLevel3_HighCharge += "$it.currentBattery  $it.displayName\n"
                } else if (it.currentBattery == 100) {
                    listLevel4_FullCharge += "$it.displayName\n"
                } else {
                    listLevel0_NoCharge += "$it.currentBattery  $it.displayName\n"
                }
            } catch (e) {
                log.trace "Caught error checking battery status."
                log.trace e
                listLevel0_NoCharge += "$it.displayName\n"
            }
        }

        if (listLevel0_NoCharge) {
            section("Batteries with errors or no status") {
                paragraph listLevel0_NoCharge.trim()
            }
        }
        
        if (listLevel1_LowCharge) {
            section("Batteries with low charge (less than $settings.level1)") {
                paragraph listLevel1_LowCharge_LowCharge.trim()
            }
        }

        if (listLevel2_MediumCharge) {
            section("Batteries with medium charge (between $settings.level1 and $settings.level3)") {
                paragraph listLevel2_MediumCharge.trim()
            }
        }

        if (listLevel3_HighCharge) {
            section("Batteries with high charge (more than $settings.level3)") {
                paragraph listLevel3_HighCharge.trim()
            }
        }

        if (listLevel4_FullCharge) {
            section("Batteries with full charge") {
                paragraph listLevel4_FullCharge.trim()
            }
        }

        section("Menu") {
            href "pageStatus", title:"Refresh", description:""
            href "pageConfigure", title:"Configure", description:""
            href "pageAbout", title:"About", description: ""
        }
    }
}

// Show Configure Page
def pageConfigure() {
    def helpPage =
        "Select devices with batteries that you wish to monitor."

    def inputBattery   = [
        name:           "devices",
        type:           "capability.battery",
        title:          "Which devices with batteries?",
        multiple:       true,
        required:       true
    ]

    def inputLevel1    = [
        name:           "level1",
        type:           "number",
        title:          "Low battery threshold?",
        defaultValue:   "20",
        required:       true
    ]

    def inputLevel3    = [
        name:           "level3",
        type:           "number",
        title:          "Medium battery threshold?",
        defaultValue:   "70",
        required:       true
    ]

    def inputPush      = [
        name:           "pushMessage",
        type:           "bool",
        title:          "Send push notifications?",
        defaultValue:   true
    ]

    def inputSMS       = [
        name:           "phoneNumber",
        type:           "phone",
        title:          "Send SMS notifications to?",
        required:       false
    ]

    def pageProperties = [
        name:           "pageConfigure",
        title:          smartAppNameShort() + " Configuration",
        nextPage:       "pageStatus",
        uninstall:      true
    ]

    return dynamicPage(pageProperties) {
        section("About") {
            paragraph helpPage
        }

        section("Devices") {
            input inputBattery
        }
        
        section("Settings") {
            input inputLevel1
            input inputLevel3
        }
        
        section("Notification") {
            input inputPush
            input inputSMS
        }

        section([title:"Options", mobileOnly:true]) {
            label title:"Assign a name", required:false
        }
    }
}

def installed() {
    log.debug("Initialized with settings: ${settings}")

    initialize()
}

def updated() {
    unschedule()
    unsubscribe()
    initialize()
}

def initialize() {
    // run the first of every month, at 09:00:00
    def cronTime = "0 0 9 1 * ? ?"
    schedule(crontab, updateStatus)
}

def send(msg) {
    log.debug(msg)

    if (settings.pushMessage) {
        sendPush(msg)
    } else {
        sendNotificationEvent(msg)
    }

    if (settings.phoneNumber != null) {
        sendSms(phoneNumber, msg) 
    }
}

def updateStatus() {
    settings.devices.each() {
        try {
            if (it.currentBattery == null) {
                send("${it.displayName} battery is not reporting.")
            } else if (it.currentBattery > 100) {
                send("${it.displayName} battery is ${it.currentBattery}, which is over 100.")
            } else if (it.currentBattery < settings.level1) {
                send("${it.displayName} battery is ${it.currentBattery} (threshold ${settings.level1}.)")
            }
        } catch (e) {
            log.trace("Caught error checking battery status.")
            log.trace(e)
            send("${it.displayName} battery reported a non-integer level.")
        }
    }
}