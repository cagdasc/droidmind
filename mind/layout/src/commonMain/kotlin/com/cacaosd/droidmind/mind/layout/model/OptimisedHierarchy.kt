package com.cacaosd.droidmind.mind.layout.model

import kotlinx.serialization.Serializable

@Serializable
data class OptimisedHierarchy(val rotation: ScreenRotation, val root: UiElement)

@Serializable
enum class ScreenRotation(val value: Int, val description: String) {
    PORTRAIT(0, "Portrait (0°)"),
    LANDSCAPE(1, "Landscape (90°)"),
    REVERSE_PORTRAIT(2, "Reverse Portrait (180°)"),
    REVERSE_LANDSCAPE(3, "Reverse Landscape (270°)");

    companion object {
        fun fromInt(value: Int): ScreenRotation {
            return entries.first { it.value == value }
        }
    }

    override fun toString(): String = description
}

@Serializable
data class UiElement(
    val type: ElementType,
    val text: String?,
    val contentDescription: String?,
    val bounds: Rect,
    val clickable: Boolean,
    val focusable: Boolean,
    val enabled: Boolean,
    val children: List<UiElement> = emptyList()
) {
    val clickPoint: Point = Point(
        x = (bounds.left + bounds.right) / 2,
        y = (bounds.top + bounds.bottom) / 2
    )
}

@Serializable
data class Point(val x: Int, val y: Int)

@Serializable
data class Rect(val left: Int, val top: Int, val right: Int, val bottom: Int)

fun OptimisedHierarchy.flattenBfs(
    filter: (UiElement) -> Boolean
): List<UiElement> {
    val result = mutableListOf<UiElement>()
    val queue = ArrayDeque<UiElement>()
    queue.add(root)

    while (queue.isNotEmpty()) {
        val current = queue.removeFirst()
        if (filter(current)) result.add(current)
        queue.addAll(current.children)
    }

    return result
}

fun OptimisedHierarchy.flattenDfs(
    filter: (UiElement) -> Boolean
): List<UiElement> {
    val result = mutableListOf<UiElement>()
    val stack = ArrayDeque<UiElement>()
    stack.add(root)

    while (stack.isNotEmpty()) {
        val current = stack.removeLast()
        if (filter(current)) result.add(current)
        current.children.asReversed().forEach { stack.add(it) }
    }

    return result
}

/**
 * Retrieves all UI elements of a specific type using breadth-first search.
 * @param elementType The type of elements to retrieve
 * @return List of UI elements matching the specified type
 */
fun OptimisedHierarchy.findElementsByType(elementType: ElementType): List<UiElement> =
    flattenBfs { it.type == elementType }

/**
 * Retrieves all clickable UI elements of a specific type.
 * @param elementType The type of elements to retrieve
 * @return List of clickable UI elements matching the specified type
 */
fun OptimisedHierarchy.findClickableElementsByType(elementType: ElementType): List<UiElement> =
    flattenBfs { it.type == elementType && it.clickable }

/**
 * Retrieves all UI elements by text content.
 * @param text The text to search for
 * @return List of UI elements containing the specified text
 */
fun OptimisedHierarchy.findElementsByText(text: String): List<UiElement> =
    flattenBfs {
        it.text?.contains(text, ignoreCase = true) == true || it.contentDescription?.contains(
            text,
            ignoreCase = true
        ) == true
    }
