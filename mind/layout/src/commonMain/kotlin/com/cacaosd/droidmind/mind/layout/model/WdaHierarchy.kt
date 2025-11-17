package com.cacaosd.droidmind.mind.layout.model

import kotlinx.serialization.Serializable

@Serializable(with = WdaNodeSerializer::class)
data class WdaNode(
    val index: Int,
    val type: String = "",
    val name: String = "",
    val label: String = "",
    val enabled: Boolean = false,
    val visible: Boolean = false,
    val accessible: Boolean = false,
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
    val children: List<WdaNode> = emptyList()
)
