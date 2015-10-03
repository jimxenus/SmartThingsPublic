/**
 *  Rise and Shine - Modifed to turn off light
 *
 *  Author: SmartThings
 *  Date: 2013-03-07
 */

 
// Automatically generated. Make future change here.
definition(
    name: "Rise & Shine - Light off",
    namespace: "",
    author: "jimxenus@gmail.com",
    description: "Modified version of Rise & Shine but turns light off instead of on",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png",
    oauth: true)

preferences {
	 section("When there's motion on any of these sensors") {
		 input "motionSensors", "capability.motionSensor", title: "Intrusion", multiple: true
	 }
	 section("after this time of day") {
		 input "timeOfDay", "time", title: "Time?"
	 }
	 section("Change to this mode") {
		 input "newMode", "mode", title: "Mode?"
	 }
	 section("and (optionally) turn off these appliances") {
		 input "switches", "capability.switch", multiple: false, required: true
	 }
}

def installed() {
	log.debug "installed, current mode = ${location.mode}, state.actionTakenOn = ${state.actionTakenOn}"
	initialize()
}

def updated() {
	log.debug "updated, current mode = ${location.mode}, state.actionTakenOn = ${state.actionTakenOn}"
	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(motionSensors, "motion.active", motionActiveHandler)
	subscribe(location, modeChangeHandler)
	if (!(state.modeStartTime)) {
		state.modeStartTime = now()
	}
}

def modeChangeHandler(evt) {
	state.modeStartTime = now()
}

def motionActiveHandler(evt)
{
	def t0 = now()
	def modeStartTime = new Date(state.modeStartTime)
	def startTime = timeTodayAfter(modeStartTime, timeOfDay, location.timeZone)
	log.debug "startTime: $startTime, t0: ${new Date(t0)}, modeStartTime: ${modeStartTime},  actionTakenOn: $state.actionTakenOn, currentMode: $location.mode, newMode: $newMode "

	if (t0 >= startTime.time && location.mode != newMode) {
		def message = "Good morning! SmartThings changed the mode to '$newMode'"
		sendPush(message)
		setLocationMode(newMode)
		log.debug message

		def dateString = new Date().format("yyyy-MM-dd")
		log.debug "last turned on switches on ${state.actionTakenOn}, today is ${dateString}"
		if (state.actionTakenOn != dateString) {
			log.debug "turning off switches"
			state.actionTakenOn = dateString
			switches?.off()
		}

	}
	else {
		log.debug "not in time window, or mode is already set, currentMode = ${location.mode}, newMode = $newMode"
	}
}
