package com.cacaosd.droidmind.feature.composable

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.cacaosd.droidmind.adb.layout_optimizer.Element
import com.cacaosd.droidmind.adb.layout_optimizer.UiElement
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.min

@Composable
internal fun ActiveLayoutRenderer(modifier: Modifier, rootElement: UiElement) {
    val bounds = rootElement.bounds

    Canvas(modifier = modifier) {
        val maxWidth = bounds.right - bounds.left
        val maxHeight = bounds.bottom - bounds.top
        val scale = min(size.width / maxWidth, size.height / maxHeight)

        fun DrawScope.drawElement(element: UiElement) {
            val elementBounds = element.bounds
            val left = elementBounds.left * scale
            val top = elementBounds.top * scale
            val width = (elementBounds.right - elementBounds.left) * scale
            val height = (elementBounds.bottom - elementBounds.top) * scale

            val color = when (element.type) {
                is Element.TextBased.Label -> Color(0x40_2196F3) // Blue → static text
                is Element.TextBased.InputField -> Color(0x40_4CAF50) // Green → editable field
                is Element.TextBased.Button -> Color(0x40_F44336) // Red → actionable element
                is Element.TextBased.ViewGroup -> Color(0x40_FFC107) // Amber → grouping view
                is Element.Container -> Color(0x40_9C27B0) // Purple → layout container
                is Element.Unknown -> Color(0x40_9E9E9E) // Gray → undefined/other
            }

            drawRect(color, Offset(left, top), Size(width, height))
            drawRect(Color.White, Offset(left, top), Size(width, height), style = Stroke(width = 2f))

            element.children.forEach { drawElement(it) }
        }

        drawElement(rootElement)
    }
}

@Preview
@Composable
private fun ActiveLayoutRendererPreview() {
    val sampleUiElement = UiElement(
        type = Element.Container,
        text = null,
        contentDescription = null,
        bounds = com.cacaosd.droidmind.adb.layout_optimizer.Rect(0, 0, 400, 800),
        clickable = false,
        focusable = false,
        enabled = true,
        children = listOf(
            UiElement(
                type = Element.TextBased.Button,
                text = "Click Me",
                contentDescription = "A button",
                bounds = com.cacaosd.droidmind.adb.layout_optimizer.Rect(50, 100, 200, 150),
                clickable = true,
                focusable = true,
                enabled = true
            ),
            UiElement(
                type = Element.TextBased.InputField,
                text = "",
                contentDescription = "Input Field",
                bounds = com.cacaosd.droidmind.adb.layout_optimizer.Rect(50, 200, 350, 250),
                clickable = true,
                focusable = true,
                enabled = true
            )
        )
    )

    ActiveLayoutRenderer(
        modifier = Modifier.width(1080.dp).height(2400.dp).background(Color.DarkGray).then(Modifier),
        rootElement = sampleUiElement
    )
}
