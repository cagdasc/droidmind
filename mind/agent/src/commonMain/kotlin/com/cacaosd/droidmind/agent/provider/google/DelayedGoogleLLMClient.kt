package com.cacaosd.droidmind.agent.provider.google

//class DelayedGoogleLLMClient(apiKey: String, private val requestIntervalMillis: Duration) : GoogleLLMClient(apiKey) {
//    override suspend fun execute(prompt: Prompt, model: LLModel, tools: List<ToolDescriptor>): List<Message.Response> {
//        // You can add custom behavior here, such as logging or modifying the prompt.
//        val response = super.execute(prompt, model, tools)
//        delay(requestIntervalMillis)
//        return response
//    }
//
//    override suspend fun executeMultipleChoices(
//        prompt: Prompt,
//        model: LLModel,
//        tools: List<ToolDescriptor>
//    ): List<LLMChoice> {
//        val executeMultipleChoices = super.executeMultipleChoices(prompt, model, tools)
//        delay(requestIntervalMillis)
//        return executeMultipleChoices
//    }
//}