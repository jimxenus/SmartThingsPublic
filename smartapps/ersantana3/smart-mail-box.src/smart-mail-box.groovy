/**
 *  Smart Mail Box
 *
 *  Author: Edgar Santana
 *  Date: 2015-1-11
 */
definition(
    name: "Smart Mail Box",
    namespace: "ersantana3",
    author: "Edgar Santana",
    description: "Get a push notification or text message when your mail arrives.",
    category: "Convenience",
    iconUrl: "http://www.iconpng.com/png/city/mail.png",
    iconX2Url: "http://4.bp.blogspot.com/_bqCxQKDqifI/S9sp2PIWS4I/AAAAAAAADgE/mU7FFUtIm6k/s1600/mailbox.png"
)

preferences {
	section("Choose one or more, when..."){
		input "contact", "capability.contactSensor", title: "Contact Opens", required: false, multiple: true
	}
	section("Send this message (optional, sends standard status message if not specified)"){
		input "messageText", "Your mail was delivered", title: "Mail Delivered?", required: false
	}
	section("Via a push notification and/or an SMS message"){
		input "phone", "phone", title: "Phone Number (for SMS, optional)", required: false
		input "pushAndPhone", "enum", title: "Both Push and SMS?", required: false, options: ["Yes","No"]
	}
	section("Minimum time between messages (optional, defaults to every message)") {
		input "frequency", "decimal", title: "Minutes", required: false
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	subscribeToEvents()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	subscribeToEvents()
}

def subscribeToEvents() {
	subscribe(contact, "contact.open", eventHandler)
	subscribe(contactClosed, "contact.closed", eventHandler)
}

def eventHandler(evt) {
	if (frequency) {
		def lastTime = state[evt.deviceId]
		if (lastTime == null || now() - lastTime >= frequency * 60000) {
			sendMessage(evt)
		}
	}
	else {
		sendMessage(evt)
	}
}

private sendMessage(evt) {
	def msg = messageText ?: defaultText(evt)
	log.debug "$evt.name:$evt.value, pushAndPhone:$pushAndPhone, '$msg'"

	if (!phone || pushAndPhone != "No") {
		log.debug "sending push"
		sendPush(msg)
	}
	if (phone) {
		log.debug "sending SMS"
		sendSms(phone, msg)
	}
	if (frequency) {
		state[evt.deviceId] = now()
	}
}