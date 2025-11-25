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
import com.cacaosd.droidmind.mind.layout.model.ElementType
import com.cacaosd.droidmind.mind.layout.model.Rect
import com.cacaosd.droidmind.mind.layout.model.UiElement
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

            val type = element.type
            val color = when (type) {
                in ElementType.TEXTUAL -> {
                    Color(0x40_2196F3)
                }

                in ElementType.INTERACTIVE -> {
                    Color(0x40_4CAF50)
                }

                in ElementType.CONTAINERS -> {
                    Color(0x40_9C27B0)
                }

                else -> {
                    Color(0x40_9E9E9E)
                }
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
        type = ElementType.Column,
        text = null,
        contentDescription = null,
        bounds = Rect(0, 0, 400, 800),
        clickable = false,
        focusable = false,
        enabled = true,
        children = listOf(
            UiElement(
                type = ElementType.Button,
                text = "Click Me",
                contentDescription = "A button",
                bounds = Rect(50, 100, 200, 150),
                clickable = true,
                focusable = true,
                enabled = true
            ),
            UiElement(
                type = ElementType.TextInput,
                text = "",
                contentDescription = "Input Field",
                bounds = Rect(50, 200, 350, 250),
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
