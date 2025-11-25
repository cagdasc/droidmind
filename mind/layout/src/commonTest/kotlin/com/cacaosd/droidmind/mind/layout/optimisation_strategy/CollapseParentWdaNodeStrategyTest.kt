package com.cacaosd.droidmind.mind.layout.optimisation_strategy

import com.cacaosd.droidmind.core.config.di.xml
import com.cacaosd.droidmind.mind.layout.model.ios.WdaNode
import kotlinx.serialization.decodeFromString
import kotlin.test.Test
import kotlin.test.assertTrue

class CollapseParentWdaNodeStrategyTest {

    private val strategy = CollapseParentWdaNodeStrategy()

    private fun readFile(fileName: String): String {
        val stream = javaClass.classLoader.getResourceAsStream("ui_dump/$fileName")
            ?: throw IllegalArgumentException("Resource not found: ui_dump/$fileName")
        return stream.bufferedReader().use { it.readText() }
    }

    private fun parseXmlToHierarchy(xmlContent: String): WdaNode {
        return xml.decodeFromString(xmlContent)
    }

    @Test
    fun `parser reads real ui_dump file`() {
        val content = readFile("apple_news.xml")
        val wdaNode = parseXmlToHierarchy(content)
        // basic sanity checks
        assertTrue(wdaNode.children.isNotEmpty())
    }

}
