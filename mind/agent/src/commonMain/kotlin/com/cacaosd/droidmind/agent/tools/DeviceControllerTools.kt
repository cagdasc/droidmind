package com.cacaosd.droidmind.agent.tools

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import com.cacaosd.droidmind.domain.tools.DeviceControllerToolsConstant
import com.cacaosd.droidmind.mind.device.controller.DeviceController
import com.cacaosd.droidmind.mind.layout.model.OptimisedHierarchy

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

    @Tool(DeviceControllerToolsConstant.VERTICAL_SCROLL_DOWN_TOOL)
    @LLMDescription(DeviceControllerToolsConstant.VERTICAL_SCROLL_DOWN_TOOL_DESC)
    suspend fun verticalScrollDown(
        startX: Int,
        startY: Int,
        endX: Int,
        endY: Int,
        durationMs: Long,
        serial: String?
    ): String {
        require(startX == endX) { "For vertical scroll, startX must be equal to endX." }
        require(endY < startY) { "For vertical scroll down, endY must be less than startY." }
        return deviceController.swipe(
            startX = startX,
            startY = startY,
            endX = endX,
            endY = endY,
            durationMs = durationMs,
            serial = serial
        )
    }

    @Tool(DeviceControllerToolsConstant.VERTICAL_SCROLL_UP_TOOL)
    @LLMDescription(DeviceControllerToolsConstant.VERTICAL_SCROLL_UP_TOOL_DESC)
    suspend fun verticalScrollUp(
        startX: Int,
        startY: Int,
        endX: Int,
        endY: Int,
        durationMs: Long,
        serial: String?
    ): String {
        require(startX == endX) { "For vertical scroll, startX must be equal to endX." }
        require(endY > startY) { "For vertical scroll up, endY must be greater than startY." }
        return deviceController.swipe(
            startX = startX,
            startY = startY,
            endX = endX,
            endY = endY,
            durationMs = durationMs,
            serial = serial
        )
    }

    @Tool(DeviceControllerToolsConstant.HORIZONTAL_SCROLL_RIGHT_TOOL)
    @LLMDescription(DeviceControllerToolsConstant.HORIZONTAL_SCROLL_RIGHT_TOOL_DESC)
    suspend fun horizontalScrollRight(
        startX: Int,
        startY: Int,
        endX: Int,
        endY: Int,
        durationMs: Long,
        serial: String?
    ): String {
        require(startY == endY) { "For horizontal scroll, startY must be equal to endY." }
        require(endX < startX) { "For horizontal scroll right, endX must be less than startX." }
        return deviceController.swipe(
            startX = startX,
            startY = startY,
            endX = endX,
            endY = endY,
            durationMs = durationMs,
            serial = serial
        )
    }

    @Tool(DeviceControllerToolsConstant.HORIZONTAL_SCROLL_LEFT_TOOL)
    @LLMDescription(DeviceControllerToolsConstant.HORIZONTAL_SCROLL_LEFT_TOOL_DESC)
    suspend fun horizontalScrollLeft(
        startX: Int,
        startY: Int,
        endX: Int,
        endY: Int,
        durationMs: Long,
        serial: String?
    ): String {
        require(startY == endY) { "For horizontal scroll, startY must be equal to endY." }
        require(endX > startX) { "For horizontal scroll left, endX must be greater than startX." }
        return deviceController.swipe(
            startX = startX,
            startY = startY,
            endX = endX,
            endY = endY,
            durationMs = durationMs,
            serial = serial
        )
    }
}
