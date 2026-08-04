package com.cacaosd.droidmind.agent.provider.google

import ai.koog.prompt.executor.clients.google.GoogleLLMClient
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import ai.koog.prompt.llm.LLModel
import ai.koog.utils.time.KoogClock
import com.cacaosd.droidmind.agent.client.AgentClientBuilder
import com.cacaosd.droidmind.domain.ModelType

internal fun getGoogleAgents(apiKey: String, clock: KoogClock): List<AgentClientBuilder> {
    val gemini30Flash = provideGoogleAgentBuilder(apiKey, GoogleModels.Gemini3_Flash_Preview, clock)
    val gemini25Flash = provideGoogleAgentBuilder(apiKey, GoogleModels.Gemini2_5Flash, clock)
    val gemini25FlashLite = provideGoogleAgentBuilder(apiKey, GoogleModels.Gemini2_5FlashLite, clock)
    return listOf(gemini30Flash, gemini25Flash, gemini25FlashLite)
}

private fun provideGoogleAgentBuilder(apiKey: String, llmModel: LLModel, clock: KoogClock): AgentClientBuilder {
    return AgentClientBuilder.create(
        llmModel = llmModel,
        executor = MultiLLMPromptExecutor(GoogleLLMClient(apiKey)),
        modelType = ModelType.REMOTE,
        clock = clock
    )
}
