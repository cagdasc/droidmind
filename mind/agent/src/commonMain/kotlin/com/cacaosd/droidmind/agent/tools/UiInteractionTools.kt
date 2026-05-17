package com.cacaosd.droidmind.agent.tools

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import com.cacaosd.droidmind.domain.tools.UiInteractionToolsConstant
import com.cacaosd.droidmind.mind.device.controller.DeviceController

@LLMDescription(UiInteractionToolsConstant.TOOLSET_DESCRIPTION)
class UiInteractionTools(private val deviceController: DeviceController) : ToolSet {

    @Tool(UiInteractionToolsConstant.INPUT_TEXT_TOOL)
    @LLMDescription(UiInteractionToolsConstant.INPUT_TEXT_TOOL_DESC)
    suspend fun inputText(text: String, serial: String?): String = deviceController.inputText(
        text = text,
        serial = serial
    )

    @Tool(UiInteractionToolsConstant.TAP_TOOL)
    @LLMDescription(UiInteractionToolsConstant.TAP_TOOL_DESC)
    suspend fun tap(x: Int, y: Int, tapCount: Int, serial: String?): String {
        require(tapCount > 0) { "tapCount must be greater than 0." }
        return buildString {
            repeat(tapCount) {
                append("Tap ${it + 1}: ")
                val result = deviceController.tap(x = x, y = y, serial = serial)
                appendLine(result)
            }
        }
    }

    @Tool(UiInteractionToolsConstant.SEND_KEY_EVENT_TOOL)
    @LLMDescription(UiInteractionToolsConstant.SEND_KEY_EVENT_TOOL_DESC)
    suspend fun sendKeyEvent(key: String, serial: String?): String =
        deviceController.sendKeyEvent(key = key, serial = serial)

    @Tool(UiInteractionToolsConstant.SCROLL_DOWN_TOOL)
    @LLMDescription(UiInteractionToolsConstant.SCROLL_DOWN_TOOL_DESC)
    suspend fun scrollDown(
        startX: Int,
        startY: Int,
        endX: Int,
        endY: Int,
        durationMs: Long,
        serial: String?
    ): String {
        require(startX == endX) { "For scroll down, startX must be equal to endX." }
        require(endY > startY) { "For scroll down, endY must be greater than startY." }
        return deviceController.swipe(
            startX = startX,
            startY = startY,
            endX = endX,
            endY = endY,
            durationMs = durationMs,
            serial = serial
        )
    }

    @Tool(UiInteractionToolsConstant.SCROLL_UP_TOOL)
    @LLMDescription(UiInteractionToolsConstant.SCROLL_UP_TOOL_DESC)
    suspend fun scrollUp(
        startX: Int,
        startY: Int,
        endX: Int,
        endY: Int,
        durationMs: Long,
        serial: String?
    ): String {
        require(startX == endX) { "For scroll up, startX must be equal to endX." }
        require(endY < startY) { "For scroll up, endY must be less than startY." }
        return deviceController.swipe(
            startX = startX,
            startY = startY,
            endX = endX,
            endY = endY,
            durationMs = durationMs,
            serial = serial
        )
    }

    @Tool(UiInteractionToolsConstant.SCROLL_RIGHT_TOOL)
    @LLMDescription(UiInteractionToolsConstant.SCROLL_RIGHT_TOOL_DESC)
    suspend fun scrollRight(
        startX: Int,
        startY: Int,
        endX: Int,
        endY: Int,
        durationMs: Long,
        serial: String?
    ): String {
        require(startY == endY) { "For scroll right, startY must be equal to endY." }
        require(endX > startX) { "For scroll right, endX must be greater than startX." }
        return deviceController.swipe(
            startX = startX,
            startY = startY,
            endX = endX,
            endY = endY,
            durationMs = durationMs,
            serial = serial
        )
    }

    @Tool(UiInteractionToolsConstant.SCROLL_LEFT_TOOL)
    @LLMDescription(UiInteractionToolsConstant.SCROLL_LEFT_TOOL_DESC)
    suspend fun scrollLeft(
        startX: Int,
        startY: Int,
        endX: Int,
        endY: Int,
        durationMs: Long,
        serial: String?
    ): String {
        require(startY == endY) { "For scroll, startY must be equal to endY." }
        require(endX < startX) { "For scroll left, endX must be less than startX." }
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
