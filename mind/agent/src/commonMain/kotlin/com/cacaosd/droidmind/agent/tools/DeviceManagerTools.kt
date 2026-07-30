package com.cacaosd.droidmind.agent.tools

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import com.cacaosd.droidmind.domain.tools.DeviceManagerToolsConstant
import com.cacaosd.droidmind.mind.device.controller.DeviceController

@LLMDescription(DeviceManagerToolsConstant.TOOLSET_DESCRIPTION)
class DeviceManagerTools(private val deviceController: DeviceController) : ToolSet {

    @Tool(DeviceManagerToolsConstant.LIST_CONNECTED_DEVICES_TOOL)
    @LLMDescription(DeviceManagerToolsConstant.LIST_CONNECTED_DEVICES_TOOL_DESC)
    suspend fun listConnectedDevices(): List<String> {
        return deviceController.getDevices().map { "Device name: ${it.name}, serial: ${it.serial}" }
    }

    @Tool(DeviceManagerToolsConstant.LIST_INSTALLED_PACKAGES_TOOL)
    @LLMDescription(DeviceManagerToolsConstant.LIST_INSTALLED_PACKAGES_TOOL_DESC)
    suspend fun listInstalledPackages(serial: String?): List<String> =
        deviceController.listInstalledPackages(serial = serial)

    @Tool(DeviceManagerToolsConstant.LAUNCH_APP_TOOL)
    @LLMDescription(DeviceManagerToolsConstant.LAUNCH_APP_TOOL_DESC)
    suspend fun launchApp(packageName: String, serial: String?): String = deviceController.launchApp(
        packageName = packageName,
        serial = serial
    )
}
