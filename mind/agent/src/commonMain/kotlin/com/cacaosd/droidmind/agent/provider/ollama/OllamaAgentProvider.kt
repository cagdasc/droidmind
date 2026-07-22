package com.cacaosd.droidmind.agent.provider.ollama

import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import ai.koog.prompt.executor.ollama.client.OllamaClient
import ai.koog.prompt.executor.ollama.client.OllamaModelCard
import ai.koog.prompt.executor.ollama.client.toLLModel
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLModel
import com.cacaosd.droidmind.agent.client.AgentClientBuilder
import com.cacaosd.droidmind.domain.ModelType

internal suspend fun getOllamaLocalAgents(): List<LLModel> {
    val ollamaClient = OllamaClient()
    return ollamaClient.getModels().filter {
        it.capabilities.any { capability -> capability in listOf(LLMCapability.Tools, LLMCapability.ToolChoice) }
    }
        .map(OllamaModelCard::toLLModel)
}

internal suspend fun getOllamaAgentClientBuilders(): List<AgentClientBuilder> {
    return getOllamaLocalAgents()
        .map { lLModel ->
            provideOllamaAgentBuilder(llmModel = lLModel)
        }
}

private fun provideOllamaAgentBuilder(llmModel: LLModel): AgentClientBuilder {
    return AgentClientBuilder.create(
        llmModel = llmModel,
        MultiLLMPromptExecutor(OllamaClient()),
        modelType = ModelType.LOCAL
    )
}
