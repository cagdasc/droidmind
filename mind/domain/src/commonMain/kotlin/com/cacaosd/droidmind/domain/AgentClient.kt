package com.cacaosd.droidmind.domain

interface AgentClient {
    val modelProvider: String

    val modelName: String

    val modelType: ModelType

    suspend fun executePrompt(prompt: String)
}

enum class ModelType {
    LOCAL,
    REMOTE
}
