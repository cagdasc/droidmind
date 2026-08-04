package com.cacaosd.droidmind.agent.client

import ai.koog.agents.core.agent.GraphAIAgent
import ai.koog.agents.core.agent.entity.AIAgentGraphStrategy
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.features.eventHandler.feature.EventHandler
import ai.koog.agents.features.tokenizer.feature.MessageTokenizer
import ai.koog.prompt.tokenizer.SimpleRegexBasedTokenizer
import ai.koog.utils.time.KoogClock
import com.cacaosd.droidmind.agent.client.system_prompts.SECTIONED_SYSTEM_PROMPT
import com.cacaosd.droidmind.agent.provider.google.getGoogleAgents
import com.cacaosd.droidmind.agent.provider.ollama.getOllamaAgentClientBuilders
import com.cacaosd.droidmind.core.logging.Logger
import com.cacaosd.droidmind.domain.AgentClient
import com.cacaosd.droidmind.domain.AgentClientFactory
import com.cacaosd.droidmind.domain.AgentEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import java.util.*
import kotlin.time.Clock

class DefaultAgentClientFactory(
    private val toolRegistry: ToolRegistry,
    private val aiAgentStrategy: AIAgentGraphStrategy<String, String>,
    private val agentEventFlow: MutableSharedFlow<AgentEvent>,
    private val properties: Properties,
    private val clock: Clock,
    private val koogClock: KoogClock
) :
    AgentClientFactory {

    override fun createRemoteModel(): List<AgentClient> {
        val apiKey = properties.getProperty("GEMINI_API_KEY")
        return getGoogleAgents(apiKey = apiKey, clock = koogClock).map { agents ->
            agents.withSystemPrompt(SYSTEM_PROMPT)
                .withMaxIterations(250)
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
        return getOllamaAgentClientBuilders(clock = koogClock).map { agents ->
            agents.withSystemPrompt(SYSTEM_PROMPT)
                .withMaxIterations(250)
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
                prompt.messages.map { it.textContent() }.forEach { message ->
                    agentEventFlow.emit(
                        AgentEvent.Prompt(
                            content = message,
                            timestamp = clock.now()
                        )
                    )
                }
            }

            onLLMCallCompleted { context ->
                val responses = listOfNotNull(context.response)

                responses.forEach { message ->
                    val mcpMessage = AgentEvent.Response.Assistant(
                        content = message.textContent(),
                        finishReason = message.finishReason,
                        timestamp = message.metaInfo.timestamp
                    )

                    val metadataMessage = AgentEvent.Token(
                        inputTokensCount = message.metaInfo.inputTokensCount ?: 0,
                        outputTokensCount = message.metaInfo.outputTokensCount ?: 0,
                        totalTokensCount = message.metaInfo.totalTokensCount ?: 0,
                        timestamp = message.metaInfo.timestamp
                    )

                    agentEventFlow.emit(metadataMessage)
                    agentEventFlow.emit(mcpMessage)
                }
            }

            onToolCallStarting { context ->
                agentEventFlow.emit(
                    AgentEvent.Response.ToolCall(
                        toolName = context.toolName,
                        content = """
                            Tool description: ${context.toolDescription}
                            Tool args: ${context.toolArgs}
                        """.trimIndent(),
                        timestamp = clock.now()
                    )
                )
            }

            onToolCallCompleted { context ->
                agentEventFlow.emit(
                    AgentEvent.Response.ToolResult(
                        toolName = context.toolName,
                        content = context.toolResult?.toString(),
                        timestamp = clock.now()
                    )
                )
            }

            onToolCallFailed { context ->
                val throwable = context.error
                agentEventFlow.emit(
                    AgentEvent.Failure(
                        reason = context.message,
                        throwable = throwable,
                        timestamp = clock.now()
                    )
                )
            }

            onToolValidationFailed { context ->
                val throwable = context.error
                agentEventFlow.emit(
                    AgentEvent.Failure(
                        reason = context.message,
                        throwable = throwable,
                        timestamp = clock.now()
                    )
                )
            }

            onAgentExecutionFailed { context ->
                val strategyName = context.runId
                val throwable = context.error
                agentEventFlow.emit(
                    AgentEvent.Failure(
                        reason = strategyName,
                        throwable = throwable,
                        timestamp = clock.now()
                    )
                )
            }
        }
    }

    companion object {
        internal const val SYSTEM_PROMPT = SECTIONED_SYSTEM_PROMPT
    }
}
