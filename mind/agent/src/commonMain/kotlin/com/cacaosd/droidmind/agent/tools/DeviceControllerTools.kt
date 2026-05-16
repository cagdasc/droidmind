package com.cacaosd.droidmind.agent.tools

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import com.cacaosd.droidmind.domain.tools.DeviceControllerToolsConstant
import com.cacaosd.droidmind.mind.device.controller.DeviceController

@LLMDescription(DeviceControllerToolsConstant.TOOLSET_DESCRIPTION)
class DeviceControllerTools(private val deviceController: DeviceController) : ToolSet {

    @Tool(DeviceControllerToolsConstant.LIST_CONNECTED_DEVICES_TOOL)
    @LLMDescription(DeviceControllerToolsConstant.LIST_CONNECTED_DEVICES_TOOL_DESC)
    suspend fun listConnectedDevices(): List<String> {
        return deviceController.getDevices().map { "Device name: ${it.name}, serial: ${it.serial}" }
    }

    @Tool(DeviceControllerToolsConstant.LIST_INSTALLED_PACKAGES_TOOL)
    @LLMDescription(DeviceControllerToolsConstant.LIST_INSTALLED_PACKAGES_TOOL_DESC)
    suspend fun listInstalledPackages(serial: String?): List<String> =
        deviceController.listInstalledPackages(serial = serial)

    @Tool(DeviceControllerToolsConstant.LAUNCH_APP_TOOL)
    @LLMDescription(DeviceControllerToolsConstant.LAUNCH_APP_TOOL_DESC)
    suspend fun launchApp(packageName: String, serial: String?): String = deviceController.launchApp(
        packageName = packageName,
        serial = serial
    )

    @Tool(DeviceControllerToolsConstant.DEVICE_SIZE_TOOL)
    @LLMDescription(DeviceControllerToolsConstant.DEVICE_SIZE_TOOL_DESC)
    suspend fun deviceSize(serial: String?): String = deviceController.deviceSize(serial = serial)

    @Tool(DeviceControllerToolsConstant.SCREENSHOT_TOOL)
    @LLMDescription(DeviceControllerToolsConstant.SCREENSHOT_TOOL_DESC)
    suspend fun screenshot(serial: String?): String = deviceController.screenshot(serial = serial)
}
