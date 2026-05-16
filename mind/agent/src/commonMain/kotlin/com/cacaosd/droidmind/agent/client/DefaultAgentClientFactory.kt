@file:OptIn(ExperimentalUuidApi::class)

package com.cacaosd.droidmind.agent.client

import ai.koog.agents.core.agent.GraphAIAgent
import ai.koog.agents.core.agent.entity.AIAgentGraphStrategy
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.features.eventHandler.feature.EventHandler
import ai.koog.agents.features.tokenizer.feature.MessageTokenizer
import ai.koog.prompt.message.Message
import ai.koog.prompt.tokenizer.SimpleRegexBasedTokenizer
import com.cacaosd.droidmind.agent.client.system_prompts.SECTIONED_SYSTEM_PROMPT
import com.cacaosd.droidmind.agent.event.EventMapper
import com.cacaosd.droidmind.agent.provider.google.getGoogleAgents
import com.cacaosd.droidmind.agent.provider.ollama.getOllamaLocalAgents
import com.cacaosd.droidmind.core.logging.Logger
import com.cacaosd.droidmind.domain.AgentClient
import com.cacaosd.droidmind.domain.AgentClientFactory
import com.cacaosd.droidmind.domain.AgentEvent
import com.cacaosd.droidmind.domain.McpMessage
import kotlinx.coroutines.flow.MutableSharedFlow
import java.util.*
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi

class DefaultAgentClientFactory(
    private val toolRegistry: ToolRegistry,
    private val aiAgentStrategy: AIAgentGraphStrategy<String, String>,
    private val eventMapper: EventMapper,
    private val agentMessageFlow: MutableSharedFlow<McpMessage>,
    private val agentEventFlow: MutableSharedFlow<AgentEvent>,
    private val properties: Properties,
    private val clock: Clock
) :
    AgentClientFactory {

    override fun createRemoteModel(): List<AgentClient> {
        val apiKey = properties.getProperty("GEMINI_API_KEY")
        return getGoogleAgents(apiKey = apiKey).map { agents ->
            agents.withSystemPrompt(SYSTEM_PROMPT)
                .withMaxIterations(50)
                .withTemperature(.2)
                .withTools(toolRegistry)
                .withStrategy(aiAgentStrategy)
                .withFeatures {
                    installEventHandler()
                    installSimpleRegexTokenizer()
                }
        }
    }

    override suspend fun createLocalAgents(): List<AgentClient> {
        return getOllamaLocalAgents().map { agents ->
            agents.withSystemPrompt(SYSTEM_PROMPT)
                .withMaxIterations(50)
                .withTemperature(.2)
                .withTools(toolRegistry)
                .withStrategy(aiAgentStrategy)
                .withFeatures {
                    installEventHandler()
                    installSimpleRegexTokenizer()
                }
        }
    }

    private fun GraphAIAgent.FeatureContext.installSimpleRegexTokenizer() {
        install(MessageTokenizer) {
            tokenizer = SimpleRegexBasedTokenizer()
        }
    }

    private fun GraphAIAgent.FeatureContext.installEventHandler() {
        install(EventHandler) {
            onAgentStarting {
                Logger.info("Agent is starting...")
                agentEventFlow.emit(AgentEvent.Started(timestamp = clock.now()))
            }

            onAgentCompleted {
                Logger.info("Agent has finished execution.")
                agentEventFlow.emit(AgentEvent.Completed(timestamp = clock.now()))
            }

            onLLMCallStarting { context ->
                val prompt = context.prompt
                Logger.info("LLM Call Starting with prompt: $prompt")
                prompt.messages.map { it.content }.forEach { message ->
                    agentEventFlow.emit(
                        AgentEvent.Prompt(
                            content = message,
                            timestamp = clock.now()
                        )
                    )
                }
            }

            onLLMCallCompleted { context ->
                val responses = context.responses
                val mcpMessages = responses.flatMap { response ->
                    eventMapper.mapToMcpMessages(response)
                }
                mcpMessages.forEach { message -> agentMessageFlow.emit(message) }

                responses.forEach { message ->
                    val mcpMessage = when (message) {
                        is Message.Assistant -> AgentEvent.Response.Assistant(
                            content = message.content,
                            finishReason = message.finishReason,
                            timestamp = message.metaInfo.timestamp
                        )

                        is Message.Tool.Call -> AgentEvent.Response.ToolCall(
                            toolName = message.tool,
                            content = message.content,
                            timestamp = message.metaInfo.timestamp
                        )

                        else -> null
                    }

                    val metadataMessage = AgentEvent.Token(
                        inputTokensCount = message.metaInfo.inputTokensCount ?: 0,
                        outputTokensCount = message.metaInfo.outputTokensCount ?: 0,
                        totalTokensCount = message.metaInfo.totalTokensCount ?: 0,
                        timestamp = message.metaInfo.timestamp
                    )

                    agentEventFlow.emit(metadataMessage)
                    mcpMessage?.let { agentEventFlow.emit(it) }
                }
            }

            onAgentExecutionFailed { context ->
                val strategyName = context.runId
                val throwable = context.throwable
                agentMessageFlow.emit(
                    McpMessage.Response.AssistantWithError(
                        strategyName = strategyName,
                        throwable = throwable
                    )
                )
                agentEventFlow.emit(
                    AgentEvent.Failure(
                        reason = strategyName,
                        throwable = throwable,
                        timestamp = clock.now()
                    )
                )
            }

            onToolCallFailed { context ->
                val throwable =
                    context.error?.let { Throwable(message = it.message, cause = Throwable(message = it.cause)) }
                agentEventFlow.emit(
                    AgentEvent.Failure(
                        reason = context.message,
                        throwable = throwable,
                        timestamp = clock.now()
                    )
                )
            }

            onToolValidationFailed { context ->
                val throwable =
                    context.error.let {
                        Throwable(message = it.message, cause = Throwable(message = it.cause))
                    }
                agentEventFlow.emit(
                    AgentEvent.Failure(
                        reason = context.message,
                        throwable = throwable,
                        timestamp = clock.now()
                    )
                )
            }

            onToolCallCompleted { context ->
                agentMessageFlow.emit(
                    McpMessage.Response.ToolResult(
                        toolName = context.toolName,
                        content = context.toolResult?.toString()
                    )
                )

                agentEventFlow.emit(
                    AgentEvent.Response.ToolResult(
                        toolName = context.toolName,
                        content = context.toolResult?.toString(),
                        timestamp = clock.now()
                    )
                )
            }
        }
    }

    companion object {
        private const val SYSTEM_PROMPT = SECTIONED_SYSTEM_PROMPT
    }
}
