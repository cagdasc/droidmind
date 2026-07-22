package com.cacaosd.droidmind.agent.acp

import ai.koog.agents.core.agent.entity.AIAgentGraphStrategy
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.utils.time.KoogClock
import com.agentclientprotocol.agent.AgentInfo
import com.agentclientprotocol.agent.AgentSession
import com.agentclientprotocol.agent.AgentSupport
import com.agentclientprotocol.client.ClientInfo
import com.agentclientprotocol.common.SessionCreationParameters
import com.agentclientprotocol.model.AgentCapabilities
import com.agentclientprotocol.model.LATEST_PROTOCOL_VERSION
import com.agentclientprotocol.model.PromptCapabilities
import com.agentclientprotocol.model.SessionId
import com.agentclientprotocol.protocol.Protocol
import com.cacaosd.droidmind.core.logging.Logger
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class DroidMindAgentSupport(
    private val clock: KoogClock,
    private val protocol: Protocol,
    private val toolRegistry: ToolRegistry,
    private val aiAgentStrategy: AIAgentGraphStrategy<String, String>,
    private val promptExecutor: PromptExecutor,
) : AgentSupport {

    override suspend fun initialize(clientInfo: ClientInfo): AgentInfo {
        Logger.info("Initializing ACP agent for client with capabilities: ${clientInfo.capabilities}")

        return AgentInfo(
            protocolVersion = LATEST_PROTOCOL_VERSION,
            capabilities = AgentCapabilities(
                loadSession = false,
                promptCapabilities = PromptCapabilities(
                    audio = false,
                    image = false,
                    embeddedContext = false
                )
            ),
            authMethods = emptyList()
        )
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun createSession(sessionParameters: SessionCreationParameters): AgentSession {
        val sessionId = SessionId(Uuid.random().toString())
        Logger.info("Creating new session with ID: $sessionId")

        return DroidMindAgentSession(
            sessionId = sessionId,
            protocol = protocol,
            clock = clock,
            toolRegistry = toolRegistry,
            aiAgentStrategy = aiAgentStrategy,
            promptExecutor = promptExecutor
        )
    }

    override suspend fun loadSession(
        sessionId: SessionId,
        sessionParameters: SessionCreationParameters,
    ): AgentSession {
        Logger.info("Loading session with ID: $sessionId")

        return DroidMindAgentSession(
            sessionId = sessionId,
            protocol = protocol,
            clock = clock,
            toolRegistry = toolRegistry,
            aiAgentStrategy = aiAgentStrategy,
            promptExecutor = promptExecutor
        )
    }
}
