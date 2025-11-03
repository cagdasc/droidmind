package com.cacaosd.droidmind.adb.layout_optimizer

data class OptimisedHierarchy(val rotation: ScreenRotation, val root: UiElement)

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

data class UiElement(
    val type: Element,
    val text: String?,
    val contentDescription: String?,
    val bounds: Rect,
    val clickable: Boolean,
    val focusable: Boolean,
    val enabled: Boolean,
    val children: List<UiElement> = emptyList()
)

sealed class Element {
    sealed class TextBased : Element() {
        data object Label : TextBased()
        data object InputField : TextBased()
        data object Button : TextBased()
        data object ViewGroup : TextBased()
    }

    data object Container : Element()
    object Unknown : Element()
}

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
