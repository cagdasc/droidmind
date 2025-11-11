package com.cacaosd.droidmind.domain

sealed class McpMessage {
    sealed class Response() : McpMessage() {
        data class Assistant(val content: String, val finishReason: String? = null) : Response()
        data class AssistantWithError(val strategyName: String, val throwable: Throwable) : Response()
        data class ToolResult(val toolName: String, val content: Any?) : Response()
        sealed class Metadata : Response() {
            data class Token(val inputTokensCount: Int, val outputTokensCount: Int, val totalTokensCount: Int) :
                Metadata()
        }
    }

    sealed class Request() : McpMessage() {
        data class User(val message: String) : Request()
        data class Tool(val toolName: String, val content: String) : Request()
    }
}
