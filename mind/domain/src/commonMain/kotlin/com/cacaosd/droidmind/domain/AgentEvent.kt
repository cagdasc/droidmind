package com.cacaosd.droidmind.domain

sealed interface AgentEvent {
    data object Started : AgentEvent
    data object Completed : AgentEvent
    data class Prompt(val content: String) : AgentEvent

    sealed interface Response : AgentEvent {
        data class Assistant(val content: String, val finishReason: String? = null) : Response
        data class ToolCall(val toolName: String, val content: String) : Response
        data class ToolResult(val toolName: String, val content: Any?) : Response
    }

    data class Failure(val reason: String, val throwable: Throwable) : AgentEvent
    data class Token(val inputTokensCount: Int, val outputTokensCount: Int, val totalTokensCount: Int) : AgentEvent

}