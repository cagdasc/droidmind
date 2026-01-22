package com.cacaosd.droidmind.domain

interface AgentClientFactory {
    fun createGoogleAgent(apiKey: String): AgentClient

    fun createMetaLLamaAgent(): AgentClient

    fun createCustomModel(modelName: String): AgentClient

    fun createOllamaAgents(): List<AgentClient>
}
