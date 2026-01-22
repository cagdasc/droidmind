package com.cacaosd.droidmind.feature.automation_runner.usecase

import com.cacaosd.droidmind.domain.AgentClient

class GetAvailableLLMsUseCase(
    private val googleAgentClient: AgentClient,
    private val metaAgentClient: AgentClient,
    private val ollamaAgentClients: List<AgentClient>,
) {

    fun invoke(): List<AgentClient> {
        return listOf(
            googleAgentClient,
            metaAgentClient,
        ) + ollamaAgentClients
    }
}