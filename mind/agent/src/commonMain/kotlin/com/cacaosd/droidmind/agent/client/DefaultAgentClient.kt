package com.cacaosd.droidmind.agent.client

import ai.koog.utils.io.use
import com.cacaosd.droidmind.domain.AgentClient

class DefaultAgentClient(
    builder: AgentClientBuilder
) : AgentClient {
    private val agent = builder.build()

    override suspend fun executePrompt(prompt: String) {
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