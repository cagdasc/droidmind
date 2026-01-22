package com.cacaosd.droidmind.domain

interface AgentClient {
    val modelProvider: String

    val modelName: String

    suspend fun executePrompt(prompt: String)

    suspend fun stop()
}
