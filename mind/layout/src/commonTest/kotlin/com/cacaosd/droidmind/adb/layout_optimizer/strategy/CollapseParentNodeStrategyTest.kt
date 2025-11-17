package com.cacaosd.droidmind.adb.layout_optimizer.strategy

import com.cacaosd.droidmind.mind.layout.model.Hierarchy
import com.cacaosd.droidmind.mind.layout.model.Node
import com.cacaosd.droidmind.mind.layout.model.WdaNode
import com.cacaosd.droidmind.mind.layout.parser.xmlParser
import com.cacaosd.droidmind.mind.layout.strategy.CollapseParentNodeStrategy
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

    private fun parseXmlToHierarchy(xmlContent: String): Hierarchy {
        return xmlParser.decodeFromString(xmlContent)
    }

    private fun parseXmlToWda(xmlContent: String): WdaNode {
        return xmlParser.decodeFromString(xmlContent)
    }

    @Test
    fun `parser reads real ui_dump file`() {
        val content = readFile("youtube_home.xml")
        val hierarchy = parseXmlToHierarchy(content)
        // basic sanity checks
        assertTrue(hierarchy.rotation.isNotEmpty())
        assertTrue(hierarchy.node.index.isNotEmpty())
    }

    @Test
    fun `parser reads ios ui_dump file`() {
        val content = readFile("ios.xml").replace(Regex("""\\(["/ ])"""), "$1")

        val wda = parseXmlToWda(content)
        // basic sanity checks
        println(wda)
    }

    @Test
    fun `meaningful parent is preserved when it has content`() {
        val child = Node(
            text = "child",
            resourceId = "",
            contentDesc = "",
            clickable = false,
            focusable = false,
            enabled = true,
            children = emptyList(),
            index = "1"
        )

        val parent = Node(
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
        val child = Node(
            text = "child",
            resourceId = "",
            contentDesc = "",
            clickable = false,
            focusable = false,
            enabled = true,
            children = emptyList(),
            index = "5"
        )

        val parent = Node(
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
        val node = Node(
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
            strategy.optimise(node)
        }
        assertEquals("Node cannot be optimised.", ex.message)
    }
}
