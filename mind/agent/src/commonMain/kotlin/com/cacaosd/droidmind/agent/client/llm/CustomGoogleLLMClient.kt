package com.cacaosd.droidmind.agent.client.llm

import ai.koog.agents.core.tools.ToolDescriptor
import ai.koog.prompt.dsl.Prompt
import ai.koog.prompt.executor.clients.google.GoogleLLMClient
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.message.LLMChoice
import ai.koog.prompt.message.Message
import kotlinx.coroutines.delay

class CustomGoogleLLMClient(apiKey: String) : GoogleLLMClient(apiKey) {
    override suspend fun execute(prompt: Prompt, model: LLModel, tools: List<ToolDescriptor>): List<Message.Response> {
        // You can add custom behavior here, such as logging or modifying the prompt.
        val response = super.execute(prompt, model, tools)
        delay(7_000)
        return response
    }

    override suspend fun executeMultipleChoices(
        prompt: Prompt,
        model: LLModel,
        tools: List<ToolDescriptor>
    ): List<LLMChoice> {
        val executeMultipleChoices = super.executeMultipleChoices(prompt, model, tools)
        delay(16_000)
        return executeMultipleChoices
    }
}
