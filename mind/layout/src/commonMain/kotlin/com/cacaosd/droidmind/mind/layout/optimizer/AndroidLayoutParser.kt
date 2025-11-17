package com.cacaosd.droidmind.mind.layout.optimizer

import com.cacaosd.droidmind.mind.layout.model.*
import com.cacaosd.droidmind.mind.layout.parser.xmlParser
import com.cacaosd.droidmind.mind.layout.strategy.CollapseParentNodeStrategy
import com.cacaosd.droidmind.mind.layout.strategy.NodeOptimisationStrategy
import kotlinx.serialization.decodeFromString
import nl.adaptivity.xmlutil.serialization.XML
import java.io.File

fun androidLayoutParser(): LayoutParser =
    AndroidLayoutParser(xml = xmlParser, nodeOptimisationStrategy = CollapseParentNodeStrategy())

class AndroidLayoutParser(private val xml: XML, private val nodeOptimisationStrategy: NodeOptimisationStrategy<Node>) :
    LayoutParser {

    override fun parse(uiDumpFile: File): OptimisedHierarchy? {
        val uiText = uiDumpFile.readText()
        val hierarchy = xml.decodeFromString<Hierarchy>(uiText)
        return hierarchy.toOptimizedUi()
    }

    private fun Hierarchy.toOptimizedUi(): OptimisedHierarchy? {
        val rotation = ScreenRotation.fromInt(rotation.toInt())
        val cleanNode = nodeOptimisationStrategy.optimise(node = node)

        // TODO: Handle null case and return meaningful message to agent
        val root = cleanNode.toUiElement() ?: return null
        return OptimisedHierarchy(rotation = rotation, root = root)
    }

    private fun Node.toUiElement(): UiElement? {
        val children = children.mapNotNull { it.toUiElement() }

        val rect = bounds.toRect() ?: return null

        return UiElement(
            type = elementTypeFromNode(this),
            text = text.takeIf { it.isNotBlank() },
            contentDescription = contentDesc.takeIf { it.isNotBlank() },
            bounds = rect,
            clickable = clickable,
            focusable = focusable,
            enabled = enabled,
            children = children
        )
    }

    private fun String.toRect(): Rect? {
        val match = Regex("""\[(\d+),(\d+)]\[(\d+),(\d+)]""").find(this) ?: return null
        val (left, top, right, bottom) = match.destructured
        return Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
    }

    private fun elementTypeFromNode(node: Node): Element {
        return when {
            node.className.contains("Button", ignoreCase = true) -> Element.TextBased.Button
            node.className.contains("EditText", ignoreCase = true) -> Element.TextBased.InputField
            node.className.contains("TextView", ignoreCase = true) -> Element.TextBased.Label
            node.className.contains("ViewGroup", ignoreCase = true) -> Element.TextBased.Label
            node.className.contains("View", ignoreCase = true) -> Element.TextBased.Label
            containers.any { it == node.className } -> Element.Container
            else -> {
                val isInteractable = node.clickable || node.checkable || node.longClickable
                if (isInteractable) {
                    Element.TextBased.Button
                } else {
                    Element.Unknown
                }
            }
        }
    }

    private val containers = listOf(
        "androidx.compose.ui.platform.ComposeView",
        "android.view.ViewGroup",
        "android.widget.FrameLayout",
        "android.widget.RelativeLayout",
        "android.widget.LinearLayout",
        "android.widget.ScrollView",
        "android.support.v7.widget.RecyclerView",
    )
}
