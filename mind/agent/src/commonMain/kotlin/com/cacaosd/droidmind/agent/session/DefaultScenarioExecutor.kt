package com.cacaosd.droidmind.agent.session

import com.cacaosd.droidmind.domain.AgentClient
import com.cacaosd.droidmind.domain.session.ScenarioExecutor
import com.cacaosd.droidmind.mind.device.controller.DeviceController
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

class DefaultScenarioExecutor(private val deviceController: DeviceController) :
    ScenarioExecutor {

    override suspend fun execute(agentClient: AgentClient, deviceSerial: String?, packageName: String, prompt: String) {
        deviceController.enableAccessibilityService(serial = deviceSerial)
        delay(250.milliseconds)
        deviceController.sendData(
            deviceSerial, mapOf(
                "INTERACTION_EVENT" to "start_recording",
                "APP_PACKAGE" to packageName
            )
        )
        delay(250.milliseconds)

        agentClient.executePrompt(prompt = prompt)

        delay(250.milliseconds)

        deviceController.sendData(
            deviceSerial, mapOf(
                "INTERACTION_EVENT" to "stop_recording",
                "APP_PACKAGE" to packageName
            )
        )
        delay(2000.milliseconds)
        deviceController.disableAccessibilityService(serial = deviceSerial)
    }
}