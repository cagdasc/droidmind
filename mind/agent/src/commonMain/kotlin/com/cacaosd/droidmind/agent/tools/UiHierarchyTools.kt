package com.cacaosd.droidmind.agent.tools

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import com.cacaosd.droidmind.domain.tools.UiHierarchyToolsConstant
import com.cacaosd.droidmind.mind.device.controller.DeviceController
import com.cacaosd.droidmind.mind.layout.model.*

@LLMDescription(UiHierarchyToolsConstant.TOOLSET_DESCRIPTION)
class UiHierarchyTools(private val deviceController: DeviceController) : ToolSet {

    @Tool(UiHierarchyToolsConstant.GET_UI_HIERARCHY_TOOL)
    @LLMDescription(UiHierarchyToolsConstant.GET_UI_HIERARCHY_TOOL_DESC)
    suspend fun getUi(packageName: String, serial: String?): OptimisedHierarchy? =
        deviceController.getOptimisedUiHierarchy(packageName = packageName, serial = serial)

    @Tool(UiHierarchyToolsConstant.FIND_ELEMENT_BY_TYPE_TOOL)
    @LLMDescription(UiHierarchyToolsConstant.FIND_ELEMENT_BY_TYPE_TOOL_DESC)
    suspend fun findUiElementByType(
        packageName: String,
        serial: String?,
        elementType: ElementType
    ): List<UiElement> {
        val uiHierarchy = deviceController.getOptimisedUiHierarchy(packageName = packageName, serial = serial)
        return uiHierarchy?.findElementsByType(elementType = elementType) ?: emptyList()
    }

    @Tool(UiHierarchyToolsConstant.FIND_ELEMENT_BY_TEXT_TOOL)
    @LLMDescription(UiHierarchyToolsConstant.FIND_ELEMENT_BY_TEXT_TOOL_DESC)
    suspend fun findUiElementByText(
        packageName: String,
        serial: String?,
        text: String
    ): List<UiElement> {
        val uiHierarchy = deviceController.getOptimisedUiHierarchy(packageName = packageName, serial = serial)
        return uiHierarchy?.findElementsByText(text = text) ?: emptyList()
    }

}
