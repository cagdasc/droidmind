package com.cacaosd.droidmind.agent.client

import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import ai.koog.prompt.executor.llms.all.simpleOllamaAIExecutor
import ai.koog.prompt.llm.OllamaModels

fun provideGoogleAgentBuilder(apiKey: String): AgentClientBuilder {
    return AgentClientBuilder.create(
        llmModel = GoogleModels.Gemini2_5Flash,
        executor = simpleGoogleAIExecutor(apiKey)
    )
}

fun provideMataLLama32AgentBuilder(): AgentClientBuilder {
    return AgentClientBuilder.create(
        llmModel = OllamaModels.Meta.LLAMA_3_2,
        executor = simpleOllamaAIExecutor()
    )
}
