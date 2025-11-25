package com.cacaosd.droidmind.mind.layout.optimisation_strategy

import com.cacaosd.droidmind.core.config.di.xml
import com.cacaosd.droidmind.mind.layout.model.android.UiAutomatorHierarchy
import com.cacaosd.droidmind.mind.layout.model.android.UiAutomatorNode
import kotlinx.serialization.decodeFromString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CollapseParentNodeStrategyTest {

    private val strategy = CollapseParentNodeStrategy()

    private fun readFile(fileName: String): String {
        val stream = javaClass.classLoader.getResourceAsStream("ui_dump/$fileName")
            ?: throw IllegalArgumentException("Resource not found: ui_dump/$fileName")
        return stream.bufferedReader().use { it.readText() }
    }

    private fun parseXmlToHierarchy(xmlContent: String): UiAutomatorHierarchy {
        return xml.decodeFromString(xmlContent)
    }

    @Test
    fun `parser reads real ui_dump file`() {
        val content = readFile("youtube_home.xml")
        val hierarchy = parseXmlToHierarchy(content)
        // basic sanity checks
        assertTrue(hierarchy.rotation.isNotEmpty())
        assertTrue(hierarchy.uiAutomatorNode.index.isNotEmpty())
    }

    @Test
    fun `meaningful parent is preserved when it has content`() {
        val child = UiAutomatorNode(
            text = "child",
            resourceId = "",
            contentDesc = "",
            clickable = false,
            focusable = false,
            enabled = true,
            children = emptyList(),
            index = "1"
        )

        val parent = UiAutomatorNode(
            text = "parent",
            resourceId = "",
            contentDesc = "",
            clickable = false,
            focusable = false,
            enabled = true,
            children = listOf(child),
            index = "0"
        )

        val result = strategy.optimise(parent)

        assertEquals("parent", result.text)
        assertEquals(1, result.children.size)
    }

    @Test
    fun `meaningless parent with single child collapses to child`() {
        val child = UiAutomatorNode(
            text = "child",
            resourceId = "",
            contentDesc = "",
            clickable = false,
            focusable = false,
            enabled = true,
            children = emptyList(),
            index = "5"
        )

        val parent = UiAutomatorNode(
            text = "",
            resourceId = "",
            contentDesc = "",
            clickable = false,
            focusable = false,
            enabled = true,
            children = listOf(child),
            index = "0"
        )

        val result = strategy.optimise(parent)

        assertEquals("child", result.text)
        assertTrue(result.children.isEmpty())
        assertEquals("5", result.index)
    }

    @Test
    fun `meaningless node with no children throws`() {
        val uiAutomatorNode = UiAutomatorNode(
            text = "",
            resourceId = "",
            contentDesc = "",
            clickable = false,
            focusable = false,
            enabled = true,
            children = emptyList(),
            index = "0"
        )

        val ex = assertFailsWith<IllegalStateException> {
            strategy.optimise(uiAutomatorNode)
        }
        assertEquals("Node cannot be optimised.", ex.message)
    }
}