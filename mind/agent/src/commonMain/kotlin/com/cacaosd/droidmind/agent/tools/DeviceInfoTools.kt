package com.cacaosd.droidmind.agent.tools

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import com.cacaosd.droidmind.domain.tools.DeviceInfoToolsConstant
import com.cacaosd.droidmind.mind.device.controller.DeviceController
import com.cacaosd.droidmind.mind.device.controller.ScreenshotResult

@LLMDescription(DeviceInfoToolsConstant.TOOLSET_DESCRIPTION)
class DeviceInfoTools(private val deviceController: DeviceController) : ToolSet {

    @Tool(DeviceInfoToolsConstant.DEVICE_SIZE_TOOL)
    @LLMDescription(DeviceInfoToolsConstant.DEVICE_SIZE_TOOL_DESC)
    suspend fun deviceSize(serial: String?): String = deviceController.deviceSize(serial = serial)

    @Tool(DeviceInfoToolsConstant.SCREENSHOT_TOOL)
    @LLMDescription(DeviceInfoToolsConstant.SCREENSHOT_TOOL_DESC)
    suspend fun screenshot(serial: String?): ScreenshotResult? = deviceController.screenshot(serial = serial)
}
