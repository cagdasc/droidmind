package com.cacaosd.droidmind.domain

import kotlinx.datetime.Instant

sealed interface AgentEvent {
    val timestamp: Instant

    data class Started(override val timestamp: Instant) : AgentEvent
    data class Completed(override val timestamp: Instant) : AgentEvent
    data class Prompt(val content: String, override val timestamp: Instant) : AgentEvent

    sealed class Response(override val timestamp: Instant) : AgentEvent {
        data class Assistant(val content: String, val finishReason: String? = null, override val timestamp: Instant) :
            Response(timestamp)

        data class ToolCall(val toolName: String, val content: String, override val timestamp: Instant) :
            Response(timestamp)

        data class ToolResult(val toolName: String, val content: Any?, override val timestamp: Instant) :
            Response(timestamp)
    }

    data class Failure(val reason: String, val throwable: Throwable?, override val timestamp: Instant) : AgentEvent
    data class Token(
        val inputTokensCount: Int,
        val outputTokensCount: Int,
        val totalTokensCount: Int,
        override val timestamp: Instant
    ) : AgentEvent
}