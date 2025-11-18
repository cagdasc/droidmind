package com.cacaosd.droidmind.mind.layout.parser

import com.cacaosd.droidmind.mind.layout.model.*
import com.cacaosd.droidmind.mind.layout.optimisation_strategy.NodeOptimisationStrategy
import kotlinx.serialization.decodeFromString
import nl.adaptivity.xmlutil.serialization.XML
import java.io.File

class IosLayoutParser(private val xml: XML, private val nodeOptimisationStrategy: NodeOptimisationStrategy<WdaNode>) :
    LayoutParser {
    override fun parse(uiDumpFile: File): OptimisedHierarchy? {
        val uiText = uiDumpFile.readText()
        val wdaNode = xml.decodeFromString<WdaNode>(uiText)

        val optimisedWdaNode = nodeOptimisationStrategy.optimise(wdaNode)
        return OptimisedHierarchy(ScreenRotation.PORTRAIT, optimisedWdaNode.toUiElement())
    }

    private fun WdaNode.toUiElement(): UiElement {
        val children = children.map { it.toUiElement() }

        val rect = Rect(x, y, width - x, height - y)

        val type = elementTypeWdaFromNode(this)
        return UiElement(
            type = type,
            text = name,
            contentDescription = label,
            bounds = rect,
            clickable = type is Element.TextBased.Button || type is Element.TextBased.InputField,
            focusable = accessible,
            enabled = enabled,
            children = children
        )
    }

    fun elementTypeWdaFromNode(node: WdaNode): Element {
        val type = node.type

        // 1. explicit map match
        iosElementMapping[type]?.let { return it }

        // 2. substring heuristics (future-proof)
        return when {
            type.contains("Button", ignoreCase = true) -> Element.TextBased.Button
            type.contains("Link", ignoreCase = true) -> Element.TextBased.Button
            type.contains("TextField", ignoreCase = true) -> Element.TextBased.InputField
            type.contains("SecureTextField", ignoreCase = true) -> Element.TextBased.InputField
            type.contains("SearchField", ignoreCase = true) -> Element.TextBased.InputField
            type.contains("StaticText", ignoreCase = true) -> Element.TextBased.Label
            type.contains("TextView", ignoreCase = true) -> Element.TextBased.InputField
            type.contains("Image", ignoreCase = true) -> Element.TextBased.Label
            type.contains("Cell", ignoreCase = true) -> Element.Container
            type.contains("Table", ignoreCase = true) -> Element.Container
            type.contains("Collection", ignoreCase = true) -> Element.Container
            type.contains("Window", ignoreCase = true) -> Element.Container
            type.contains("View", ignoreCase = true) -> Element.Container
            else -> {
                Element.Unknown
            }
        }
    }
}
