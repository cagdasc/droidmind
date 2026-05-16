package com.cacaosd.droidmind.agent.provider.google

import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import ai.koog.prompt.llm.LLModel
import com.cacaosd.droidmind.agent.client.AgentClientBuilder
import com.cacaosd.droidmind.domain.ModelType

internal fun getGoogleAgents(apiKey: String): List<AgentClientBuilder> {
    val gemini30Flash = provideGoogleAgentBuilder(apiKey, GoogleModels.Gemini3_Flash_Preview)
    val gemini25Flash = provideGoogleAgentBuilder(apiKey, GoogleModels.Gemini2_5Flash)
    val gemini25FlashLite = provideGoogleAgentBuilder(apiKey, GoogleModels.Gemini2_5FlashLite)
    return listOf(gemini30Flash, gemini25Flash, gemini25FlashLite)
}

private fun provideGoogleAgentBuilder(apiKey: String, llmModel: LLModel): AgentClientBuilder {
    return AgentClientBuilder.create(
        llmModel = llmModel,
        simpleGoogleAIExecutor(apiKey),
        modelType = ModelType.REMOTE
    )
}
