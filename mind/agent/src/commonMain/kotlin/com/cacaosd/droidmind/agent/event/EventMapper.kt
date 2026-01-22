package com.cacaosd.droidmind.agent.event

import ai.koog.prompt.message.Message
import com.cacaosd.droidmind.domain.McpMessage

class EventMapper {
    fun mapToMcpMessages(message: Message.Response): List<McpMessage> {
        val mcpMessage = when (message) {
            is Message.Assistant -> McpMessage.Response.Assistant(
                content = message.content,
                finishReason = message.finishReason
            )

            is Message.Tool.Call -> McpMessage.Request.Tool(
                toolName = message.tool,
                content = message.content
            )
            else -> null
        }

        val metadataMessage = McpMessage.Response.Metadata.Token(
            inputTokensCount = message.metaInfo.inputTokensCount ?: 0,
            outputTokensCount = message.metaInfo.outputTokensCount ?: 0,
            totalTokensCount = message.metaInfo.totalTokensCount ?: 0
        )

        return listOfNotNull(mcpMessage, metadataMessage)
    }
}
