package com.cacaosd.droidmind.mind.layout.model.ios

import com.cacaosd.droidmind.mind.layout.model.ElementType
import com.cacaosd.droidmind.mind.layout.model.elementLookupTable
import kotlinx.serialization.Serializable

@Serializable(with = WdaNodeSerializer::class)
data class WdaNode(
    val index: Int,
    val type: String,
    val name: String,
    val label: String,
    val value: String,
    val traits: String,
    val enabled: Boolean = false,
    val visible: Boolean = false,
    val accessible: Boolean = false,
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
    val children: List<WdaNode> = emptyList()
)

fun WdaNode.iosClickable(): Boolean {
    val elementType = elementLookupTable.getOrDefault(type, ElementType.Unknown)

    val alwaysInteractive = setOf(
        ElementType.Button,
        ElementType.Cell,
        ElementType.Menu,
        ElementType.TextInput,
        ElementType.SwitchInput,
        ElementType.ToggleInput,
        ElementType.SliderInput,
        ElementType.Spinner,
        ElementType.PickerInput
    )

    return enabled && elementType in alwaysInteractive
}

fun WdaNode.iosScrollable(): Boolean {
    val elementType = elementLookupTable.getOrDefault(type, ElementType.Unknown)
    return elementType == ElementType.Scrollable
}

fun WdaNode.iosLongClickable(): Boolean = false

fun WdaNode.iosTextInput(): Boolean {
    val elementType = elementLookupTable.getOrDefault(type, ElementType.Unknown)
    return elementType == ElementType.TextInput
}

fun WdaNode.iosValueInput(): Boolean {
    val elementType = elementLookupTable.getOrDefault(type, ElementType.Unknown)
    return elementType in setOf(
        ElementType.SliderInput,
        ElementType.SwitchInput,
        ElementType.ToggleInput,
        ElementType.Spinner,
        ElementType.DateInput,
        ElementType.PickerInput
    )
}

fun WdaNode.iosFocusable(): Boolean {
    return enabled || accessible
}
