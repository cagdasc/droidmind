package com.cacaosd.droidmind.mind.layout.parser

import com.cacaosd.droidmind.mind.layout.model.*
import com.cacaosd.droidmind.mind.layout.model.ios.WdaNode
import com.cacaosd.droidmind.mind.layout.model.ios.iosClickable
import com.cacaosd.droidmind.mind.layout.optimisation_strategy.NodeOptimisationStrategy
import kotlinx.serialization.decodeFromString
import nl.adaptivity.xmlutil.serialization.XML
import java.io.File

class IosLayoutParser(private val xml: XML, private val nodeOptimisationStrategy: NodeOptimisationStrategy<WdaNode>) :
    LayoutParser {
    override fun parse(uiDumpFile: File): OptimisedHierarchy {
        val uiText = uiDumpFile.readText()
        val wdaNode = xml.decodeFromString<WdaNode>(uiText)

        return wdaNode.toOptimizedUi()
    }

    private fun WdaNode.toOptimizedUi(): OptimisedHierarchy {
        val optimisedWdaNode = nodeOptimisationStrategy.optimise(this)

        // TODO: Handle orientation of the device
        return OptimisedHierarchy(ScreenRotation.PORTRAIT, optimisedWdaNode.toUiElement())
    }

    private fun WdaNode.toUiElement(): UiElement {
        val children = children.map { it.toUiElement() }

        val rect = Rect(x, y, width - x, height - y)

        val elementType = elementLookupTable.getOrDefault(type, ElementType.Unknown)

        return UiElement(
            type = elementType,
            text = name,
            contentDescription = "Label: $label, Value: $value",
            bounds = rect,
            clickable = iosClickable(),
            focusable = accessible,
            enabled = enabled,
            children = children
        )
    }
}
