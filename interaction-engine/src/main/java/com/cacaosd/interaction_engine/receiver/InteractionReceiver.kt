package com.cacaosd.interaction_engine.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.serialization.Serializable

/**
 * For start recording over commandline:
 * `adb shell am broadcast -a com.cacaosd.interaction_engine.INTERACTION_EVENT --es "INTERACTION_EVENT" "start_recording" --es "APP_PACKAGE" "com.google.android.youtube"`
 * For stop recording:
 * `adb shell am broadcast -a com.cacaosd.interaction_engine.INTERACTION_EVENT --es "INTERACTION_EVENT" "stop_recording" --es "APP_PACKAGE" "com.google.android.youtube"`
 * For dump ui hierarchy:
 * `adb shell am broadcast -a com.cacaosd.interaction_engine.INTERACTION_EVENT --es "INTERACTION_EVENT" "dump_ui_hierarchy" --es "APP_PACKAGE" "com.google.android.youtube" --es "FILENAME" caca.xml`
 */
class InteractionReceiver : BroadcastReceiver() {

    private val listeners = mutableListOf<InteractionListener>()

    fun addListener(listener: InteractionListener) {
        listeners.add(listener)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null) return

        val interactionExtra =
            intent.getStringExtra(INTERACTION_EVENT_EXTRA) ?: error("Interaction event data is missing")

        val applicationExtra = intent.getStringExtra(APP_PACKAGE_EXTRA) ?: error("Application package is missing")
        val fileName = intent.getStringExtra(FILENAME)

        val interactionEvent = InteractionEvent.fromCommand(command = interactionExtra) ?: return

        listeners.forEach {
            it.onInteraction(
                InteractionEventData(
                    interactionEvent = interactionEvent, appPackage = applicationExtra, fileName = fileName
                )
            )
        }
    }

    companion object {
        const val INTERACTION_EVENT_ACTION = "com.cacaosd.interaction_engine.INTERACTION_EVENT"
        private const val INTERACTION_EVENT_EXTRA = "INTERACTION_EVENT"
        private const val APP_PACKAGE_EXTRA = "APP_PACKAGE"
        private const val FILENAME = "FILENAME"
    }
}

interface InteractionListener {
    fun onInteraction(interactionEventData: InteractionEventData)
}

@Serializable
data class InteractionEventData(
    val interactionEvent: InteractionEvent,
    val appPackage: String,
    val fileName: String?,
)

@Serializable
enum class InteractionEvent(val command: String) {
    StartRecording("start_recording"),
    StopRecording("stop_recording"),
    DumpUiHierarchy("dump_ui_hierarchy");

    companion object {
        fun fromCommand(command: String?): InteractionEvent? {
            return entries.find { it.command == command }
        }
    }
}
