package com.cacaosd.droidmind.agent.client

import ai.koog.agents.core.agent.AIAgent
import ai.koog.utils.io.use
import com.cacaosd.droidmind.domain.AgentClient

class DefaultAgentClient(
    private val builder: AgentClientBuilder
) : AgentClient {
    private lateinit var agent: AIAgent<String, String>

    override suspend fun executePrompt(prompt: String) {
        agent = builder.build()
        agent.use {
            it.run(prompt)
        }
    }

    override suspend fun stop() {
        // FIXME: This is buggy.
        // https://github.com/JetBrains/koog/issues/569
        agent.close()
    }
}