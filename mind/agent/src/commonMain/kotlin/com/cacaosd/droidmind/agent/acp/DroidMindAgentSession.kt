@file:OptIn(UnstableApi::class)

package com.cacaosd.droidmind.agent.acp

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.agent.entity.AIAgentGraphStrategy
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.features.acp.AcpAgent
import ai.koog.agents.features.acp.toKoogMessage
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.prompt.llm.LLModel
import ai.koog.utils.time.KoogClock
import com.agentclientprotocol.agent.AgentSession
import com.agentclientprotocol.annotations.UnstableApi
import com.agentclientprotocol.common.Event
import com.agentclientprotocol.model.*
import com.agentclientprotocol.protocol.Protocol
import com.cacaosd.droidmind.agent.client.DefaultAgentClientFactory.Companion.SYSTEM_PROMPT
import com.cacaosd.droidmind.agent.extensions.toAcpModelInfo
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

    private var selectedLLModel: LLModel? = null

    @OptIn(UnstableApi::class)
    private val availableModelMap = mapOf(
        GoogleModels.Gemini2_5FlashLite.toAcpModelInfo() to GoogleModels.Gemini2_5FlashLite,
        GoogleModels.Gemini2_5Flash.toAcpModelInfo() to GoogleModels.Gemini2_5Flash,
        GoogleModels.Gemini2_5Pro.toAcpModelInfo() to GoogleModels.Gemini2_5Pro,
        GoogleModels.Gemini3_Pro_Preview.toAcpModelInfo() to GoogleModels.Gemini3_Pro_Preview,
    )

    private val modelIdMap = availableModelMap.mapKeys { it.key.modelId }

    @UnstableApi
    override val availableModels: List<ModelInfo>
        get() = availableModelMap.keys.toSet().toList()

    @UnstableApi
    override val defaultModel: ModelId
        get() = GoogleModels.Gemini2_5FlashLite.toAcpModelInfo().modelId

    @UnstableApi
    override suspend fun setModel(modelId: ModelId, _meta: JsonElement?): SetSessionModelResponse {
        Logger.debug("setModel($modelId) $_meta")
        selectedLLModel = modelIdMap[modelId]
        return SetSessionModelResponse(_meta)
    }

    override suspend fun prompt(
        content: List<ContentBlock>,
        @Suppress("LocalVariableName")
        _meta: JsonElement?
    ): Flow<Event> = channelFlow {
        if (selectedLLModel == null) {
            Logger.warning("Model not set, using default model: $defaultModel")
        }

        val model = selectedLLModel ?: modelIdMap[defaultModel]!!

        val agentConfig = AIAgentConfig(
            prompt = prompt("droidmind_acp") {
                system(SYSTEM_PROMPT)
            },
            model = model,
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
            agentJob = async {
                agent.run(content.toKoogMessage(clock).textContent()).also {
                    Logger.info("Agent message: $it")
                }
            }
            agentJob?.await()
        }
    }

    override suspend fun cancel() {
        Logger.info("Canceling ACP agent")
        agentJob?.cancelAndJoin()
    }
}