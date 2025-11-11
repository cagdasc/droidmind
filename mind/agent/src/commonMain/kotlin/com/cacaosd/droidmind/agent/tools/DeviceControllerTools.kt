package com.cacaosd.droidmind.agent.tools

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import com.cacaosd.droidmind.adb.device_controller.DeviceController
import com.cacaosd.droidmind.adb.layout_optimizer.OptimisedHierarchy
import com.cacaosd.droidmind.agent.tools.description.DeviceControllerToolsConstant

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

    @Tool(DeviceControllerToolsConstant.UI_DUMP_TOOL)
    @LLMDescription(DeviceControllerToolsConstant.UI_DUMP_TOOL_DESC)
    suspend fun uiDump(packageName: String, serial: String?): OptimisedHierarchy? =
        deviceController.getOptimisedUiHierarchy(packageName = packageName, serial = serial)

    @Tool(DeviceControllerToolsConstant.INPUT_TEXT_TOOL)
    @LLMDescription(DeviceControllerToolsConstant.INPUT_TEXT_TOOL_DESC)
    suspend fun inputText(text: String, serial: String?): String = deviceController.inputText(
        text = text,
        serial = serial
    )

    @Tool(DeviceControllerToolsConstant.TAP_TOOL)
    @LLMDescription(DeviceControllerToolsConstant.TAP_TOOL_DESC)
    suspend fun tap(x: Int, y: Int, serial: String?): String = deviceController.tap(x = x, y = y, serial = serial)

    @Tool(DeviceControllerToolsConstant.SEND_KEY_EVENT_TOOL)
    @LLMDescription(DeviceControllerToolsConstant.SEND_KEY_EVENT_TOOL_DESC)
    suspend fun sendKeyEvent(key: String, serial: String?): String =
        deviceController.sendKeyEvent(key = key, serial = serial)

    @Tool(DeviceControllerToolsConstant.DEVICE_SIZE_TOOL)
    @LLMDescription(DeviceControllerToolsConstant.DEVICE_SIZE_TOOL_DESC)
    suspend fun deviceSize(serial: String?): String = deviceController.deviceSize(serial = serial)

    @Tool(DeviceControllerToolsConstant.SCREENSHOT_TOOL)
    @LLMDescription(DeviceControllerToolsConstant.SCREENSHOT_TOOL_DESC)
    suspend fun screenshot(serial: String?): String = deviceController.screenshot(serial = serial)

    @Tool(DeviceControllerToolsConstant.SWIPE_TOOL)
    @LLMDescription(DeviceControllerToolsConstant.SWIPE_TOOL_DESC)
    suspend fun swipe(
        startX: Int,
        startY: Int,
        endX: Int,
        endY: Int,
        durationMs: Long,
        serial: String?
    ): String = deviceController.swipe(
        startX = startX,
        startY = startY,
        endX = endX,
        endY = endY,
        durationMs = durationMs,
        serial = serial
    )
}
