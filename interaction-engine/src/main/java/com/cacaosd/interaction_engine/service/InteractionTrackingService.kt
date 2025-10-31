package com.cacaosd.interaction_engine.service

import android.annotation.SuppressLint
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.lifecycle.lifecycleScope
import com.cacaosd.interaction_engine.UiHierarchyDumper
import com.cacaosd.interaction_engine.model.AccessibilityEventModel
import com.cacaosd.interaction_engine.model.contentChangeTypeToString
import com.cacaosd.interaction_engine.model.eventTypeToText
import com.cacaosd.interaction_engine.model.windowChangeToText
import com.cacaosd.interaction_engine.receiver.InteractionEvent
import com.cacaosd.interaction_engine.receiver.InteractionEventData
import com.cacaosd.interaction_engine.receiver.InteractionListener
import com.cacaosd.interaction_engine.receiver.InteractionReceiver
import com.cacaosd.interaction_engine.util.json
import com.cacaosd.interaction_engine.util.saveStringToDownloads
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.toKotlinLocalDateTime
import java.time.Clock
import java.time.ZoneId
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * Accessibility-based tracking service.
 * Activate it over adb with: `adb shell settings put secure enabled_accessibility_services com.cacaosd.interaction_engine/com.cacaosd.interaction_engine.service.InteractionTrackingService`
 * Deactivate it with: `adb shell am force-stop com.cacaosd.interaction_engine`
 */
class InteractionTrackingService : LifecycleAccessibilityService() {

    @OptIn(ExperimentalAtomicApi::class)
    private val eventFlowLock = AtomicBoolean(true)
    private val filter = IntentFilter(InteractionReceiver.INTERACTION_EVENT_ACTION)
    private val interactionReceiver = InteractionReceiver().apply {
        addListener(interactionListener())
    }
    private val uiHierarchyDumper = UiHierarchyDumper()
    private val clock = Clock.systemUTC()

    private val accessibilityEventFlow = MutableSharedFlow<AccessibilityEvent>()
    private val eventCollection = mutableListOf<AccessibilityEventModel>()

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i(SERVICE_NAME, "InteractionTrackingService is connected")

        registerInteractionEngineInteractionReceiver()
    }

    override fun onDestroy() {
        unregisterReceiver(interactionReceiver)
        Log.i(SERVICE_NAME, "InteractionTrackingService is destroyed")
        super.onDestroy()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || eventFlowLock.get()) return

        lifecycleScope.launch {
            accessibilityEventFlow.emit(event)
        }

        Log.d(SERVICE_NAME, "Logging event ${event.eventType} from package ${event.packageName}")
    }

    override fun onInterrupt() = Unit

    private fun registerInteractionEngineInteractionReceiver() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(interactionReceiver, filter, RECEIVER_EXPORTED)
        } else {
            @SuppressLint("UnspecifiedRegisterReceiverFlag")
            registerReceiver(interactionReceiver, filter)
        }
    }

    private fun interactionListener() = object : InteractionListener {
        override fun onInteraction(interactionEventData: InteractionEventData) {
            Log.d(
                SERVICE_NAME,
                "Received interaction event: ${interactionEventData.interactionEvent} for app ${interactionEventData.appPackage}"
            )
            when (interactionEventData.interactionEvent) {
                InteractionEvent.StartRecording -> {
                    accessibilityEventFlow
                        .onStart {
                            eventFlowLock.set(false)
                            Log.i(SERVICE_NAME, "Started recording accessibility events")
                        }
                        .map(::mapAccessibilityEvent)
                        .filter { it.applicationPackage == null || it.applicationPackage == interactionEventData.appPackage }
                        .onEach(eventCollection::add)
                        .launchIn(lifecycleScope)
                }

                InteractionEvent.StopRecording -> {
                    eventFlowLock.set(true)
                    Log.i(SERVICE_NAME, "Stopped recording accessibility events")
                    val applicationPackage = interactionEventData.appPackage
                    saveStringToDownloads(
                        fileName = "${applicationPackage}_agent_events_${clock.millis()}.log",
                        content = json.encodeToString(eventCollection)
                    )
                    eventCollection.clear()
                }

                InteractionEvent.DumpUiHierarchy -> {
                    uiHierarchyDumper.dump(
                        rootInActiveWindow,
                        getScreenRotation(),
                        this@InteractionTrackingService,
                        interactionEventData.fileName!!
                    )
                }
            }
        }
    }

    fun mapAccessibilityEvent(accessibilityEvent: AccessibilityEvent): AccessibilityEventModel =
        AccessibilityEventModel(
            timestamp = clock.instant().atZone(ZoneId.systemDefault()).toLocalDateTime().toKotlinLocalDateTime(),
            eventType = eventTypeToText(accessibilityEvent.eventType),
            applicationPackage = accessibilityEvent.packageName?.toString(),
            viewClass = accessibilityEvent.className?.toString(),
            text = accessibilityEvent.text.joinToString(separator = " ") { it.toString() },
            source = AccessibilityEventModel.Source(
                id = accessibilityEvent.source?.viewIdResourceName,
                contentDescription = accessibilityEvent.source?.contentDescription?.toString(),
                isEnabled = accessibilityEvent.source?.isEnabled ?: false,
                classname = accessibilityEvent.source?.className.toString(),
                text = accessibilityEvent.source?.text.toString()
            ),
            windowChanges = windowChangeToText(accessibilityEvent.windowChanges),
            contentChangeType = contentChangeTypeToString(accessibilityEvent.contentChangeTypes)
        )

    @Suppress("DEPRECATION")
    private fun getScreenRotation(): Int {
        return try {
            if (Build.VERSION.SDK_INT >= 30) {
                // API 30+: AccessibilityService.display is available
                display?.rotation ?: 0
            } else {
                // Older APIs: use WindowManager
                val wm = getSystemService(WINDOW_SERVICE) as android.view.WindowManager
                wm.defaultDisplay.rotation
            }
        } catch (e: Exception) {
            0
        }
    }

    companion object {
        const val SERVICE_NAME = "InteractionTrackingService"
    }
}
