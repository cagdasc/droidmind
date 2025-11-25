package com.cacaosd.droidmind.mind.layout.model.ios

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import nl.adaptivity.xmlutil.EventType
import nl.adaptivity.xmlutil.serialization.XML

internal object WdaNodeSerializer : KSerializer<WdaNode> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("WdaNode")

    override fun deserialize(decoder: Decoder): WdaNode {
        require(decoder is XML.XmlInput) { "This serializer only works with XML" }

        val reader = decoder.input

        // Read attributes
        val attributes = mutableMapOf<String, String>()
        for (i in 0 until reader.attributeCount) {
            attributes[reader.getAttributeLocalName(i)] = reader.getAttributeValue(i)
        }

        // Read child elements (any tag name)
        val children = mutableListOf<WdaNode>()

        var depth = 1
        while (depth > 0 && reader.hasNext()) {
            when (reader.next()) {
                EventType.START_ELEMENT -> {
                    // Recursively parse child node
                    children.add(deserialize(decoder))
                }

                EventType.END_ELEMENT -> {
                    depth--
                }

                else -> {}
            }
        }


        return WdaNode(
            index = attributes["index"]?.toInt() ?: 0,
            type = attributes["type"].orEmpty(),
            name = attributes["name"].orEmpty(),
            label = attributes["label"].orEmpty(),
            value = attributes["value"].orEmpty(),
            traits = attributes["traits"].orEmpty(),
            enabled = attributes["enabled"]?.toBooleanStrictOrNull() ?: false,
            visible = attributes["visible"]?.toBooleanStrictOrNull() ?: false,
            accessible = attributes["accessible"]?.toBooleanStrictOrNull() ?: false,
            x = attributes["x"]?.toInt() ?: 0,
            y = attributes["y"]?.toInt() ?: 0,
            width = attributes["width"]?.toInt() ?: 0,
            height = attributes["height"]?.toInt() ?: 0,
            children = children
        )
    }

    override fun serialize(encoder: Encoder, value: WdaNode) {
        throw NotImplementedError("Serialization not implemented")
    }
}
