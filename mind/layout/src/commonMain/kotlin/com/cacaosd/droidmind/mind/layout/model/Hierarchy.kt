package com.cacaosd.droidmind.mind.layout.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("hierarchy")
data class Hierarchy(
    val rotation: String,
    @SerialName("node") val node: Node
)

@Serializable
@SerialName("node")
data class Node(
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
    @SerialName("node") val children: List<Node> = emptyList()
)
