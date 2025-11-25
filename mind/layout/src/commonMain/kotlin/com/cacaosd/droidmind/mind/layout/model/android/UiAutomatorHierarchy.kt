package com.cacaosd.droidmind.mind.layout.model.android

import com.cacaosd.droidmind.mind.layout.model.ElementType
import com.cacaosd.droidmind.mind.layout.model.elementLookupTable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("hierarchy")
data class UiAutomatorHierarchy(
    val rotation: String,
    @SerialName("node") val uiAutomatorNode: UiAutomatorNode
)

@Serializable
@SerialName("node")
data class UiAutomatorNode(
    val index: String,
    val text: String = "",
    @SerialName("resource-id") val resourceId: String = "",
    @SerialName("class") val className: String = "",
    @SerialName("package") val packageName: String = "",
    @SerialName("content-desc") val contentDesc: String = "",
    val checkable: Boolean = false,
    val checked: Boolean = false,
    val clickable: Boolean = false,
    val enabled: Boolean = false,
    val focusable: Boolean = false,
    val focused: Boolean = false,
    val scrollable: Boolean = false,
    @SerialName("long-clickable") val longClickable: Boolean = false,
    val password: Boolean = false,
    val selected: Boolean = false,
    val bounds: String = "",
    @SerialName("node") val children: List<UiAutomatorNode> = emptyList()
)

fun UiAutomatorNode.androidClickable(): Boolean {
    return enabled && (clickable || longClickable || checkable)
}

fun UiAutomatorNode.androidLongClickable(): Boolean = enabled && longClickable

fun UiAutomatorNode.androidScrollable(): Boolean = enabled && scrollable

fun UiAutomatorNode.androidFocusable(uiAutomatorNode: UiAutomatorNode): Boolean = focusable

fun UiAutomatorNode.androidTextInput(): Boolean {
    val type = elementLookupTable[className]
    return type == ElementType.TextInput || type == ElementType.Input
}

fun UiAutomatorNode.androidValueInput(): Boolean {
    val type = elementLookupTable[className]
    return type in setOf(
        ElementType.SliderInput,
        ElementType.SwitchInput,
        ElementType.ToggleInput,
        ElementType.Spinner,
        ElementType.DateInput,
        ElementType.PickerInput
    )
}
