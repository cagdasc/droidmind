package com.cacaosd.droidmind.mind.layout.parser

import com.cacaosd.droidmind.mind.layout.model.*
import com.cacaosd.droidmind.mind.layout.model.android.UiAutomatorHierarchy
import com.cacaosd.droidmind.mind.layout.model.android.UiAutomatorNode
import com.cacaosd.droidmind.mind.layout.optimisation_strategy.NodeOptimisationStrategy
import kotlinx.serialization.decodeFromString
import nl.adaptivity.xmlutil.serialization.XML
import java.io.File

class AndroidLayoutParser(
    private val xml: XML,
    private val uiAutomatorNodeOptimisationStrategy: NodeOptimisationStrategy<UiAutomatorNode>
) :
    LayoutParser {

    override fun parse(uiDumpFile: File): OptimisedHierarchy {
        val uiText = uiDumpFile.readText()
        val uiAutomatorHierarchy = xml.decodeFromString<UiAutomatorHierarchy>(uiText)
        return uiAutomatorHierarchy.toOptimizedUi()
    }

    private fun UiAutomatorHierarchy.toOptimizedUi(): OptimisedHierarchy {
        val rotation = ScreenRotation.fromInt(rotation.toInt())
        val optimisedNode = uiAutomatorNodeOptimisationStrategy.optimise(node = uiAutomatorNode)

        // TODO: Handle null case and return meaningful message to agent
        val root = optimisedNode.toUiElement()
        return OptimisedHierarchy(rotation = rotation, root = root)
    }

    private fun UiAutomatorNode.toUiElement(): UiElement {
        val children = children.map { it.toUiElement() }

        val rect = bounds.toRect()
        val elementType = elementLookupTable.getOrDefault(className, ElementType.Unknown)
        return UiElement(
            type = elementType,
            text = text.takeIf { it.isNotBlank() },
            contentDescription = contentDesc.takeIf { it.isNotBlank() },
            bounds = rect,
            clickable = clickable,
            focusable = focusable,
            enabled = enabled,
            children = children
        )
    }

    private fun String.toRect(): Rect {
        val match = Regex("""\[(\d+),(\d+)]\[(\d+),(\d+)]""").find(this)
        val (left, top, right, bottom) = match!!.destructured
        return Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
    }
}
