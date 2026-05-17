package com.cacaosd.droidmind.domain.tools

object UiHierarchyToolsConstant {
    const val TOOLSET_DESCRIPTION =
        "A set of tools to interact with the UI hierarchy of the device. These tools allow you to retrieve the current UI hierarchy and find specific UI elements based on their type or text."

    const val GET_UI_HIERARCHY_TOOL = "get_ui_hierarchy"
    const val GET_UI_HIERARCHY_TOOL_DESC =
        "Retrieves the current UI hierarchy (in XML). It is needed when you need to find an element or understand what is in the screen."

    const val FIND_ELEMENT_BY_TYPE_TOOL = "find_ui_element_by_type"
    const val FIND_ELEMENT_BY_TYPE_TOOL_DESC = "Retrieves ui elements that matches the given ElementType"

    const val FIND_ELEMENT_BY_TEXT_TOOL = "find_ui_element_by_text"
    const val FIND_ELEMENT_BY_TEXT_TOOL_DESC =
        "Retrieves ui elements that matches the given text"

}
