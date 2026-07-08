package com.cacaosd.droidmind.agent.acp

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.agent.entity.AIAgentGraphStrategy
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.features.acp.AcpAgent
import ai.koog.agents.features.acp.toKoogMessage
import ai.koog.prompt.Prompt
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.utils.time.KoogClock
import com.agentclientprotocol.agent.AgentSession
import com.agentclientprotocol.common.Event
import com.agentclientprotocol.model.ContentBlock
import com.agentclientprotocol.model.SessionId
import com.agentclientprotocol.protocol.Protocol
import com.cacaosd.droidmind.agent.client.DefaultAgentClientFactory.Companion.SYSTEM_PROMPT
import com.cacaosd.droidmind.core.logging.Logger
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonElement

class DroidMindAgentSession(
    override val sessionId: SessionId,
    private val protocol: Protocol,
    private val clock: KoogClock,
    private val toolRegistry: ToolRegistry,
    private val aiAgentStrategy: AIAgentGraphStrategy<String, String>,
    private val promptExecutor: PromptExecutor,
) : AgentSession {

    private var agentJob: Deferred<Unit>? = null
    private val agentMutex = Mutex()

    override suspend fun prompt(
        content: List<ContentBlock>,
        @Suppress("LocalVariableName")
        _meta: JsonElement?
    ): Flow<Event> = channelFlow {

        val agentConfig = AIAgentConfig(
            prompt = prompt("droidmind_acp") {
                system(SYSTEM_PROMPT)
            },
            model = GoogleModels.Gemini2_5Pro,
            maxAgentIterations = 250
        )

        val agent = AIAgent(
            promptExecutor = promptExecutor,
            agentConfig = agentConfig,
            strategy = aiAgentStrategy,
            toolRegistry = toolRegistry,
        ) {
            install(AcpAgent) {
                this.sessionId = this@DroidMindAgentSession.sessionId.value
                this.protocol = this@DroidMindAgentSession.protocol
                this.eventsProducer = this@channelFlow
                this.setDefaultNotifications = true
            }
        }

        agentMutex.withLock {
            agentJob = async { agent.run(content.toKoogMessage(clock).textContent()) }
            agentJob?.await()
        }
    }

    override suspend fun cancel() {
        Logger.info("Canceling ACP agent")
        agentJob?.cancelAndJoin()
    }

    private fun Prompt.appendPrompt(content: List<ContentBlock>): Prompt {
        return withMessages { messages ->
            messages + content.toKoogMessage(clock)
        }
    }
}