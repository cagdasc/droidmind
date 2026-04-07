@file:OptIn(ExperimentalUuidApi::class)

package com.cacaosd.droidmind.agent.client

import ai.koog.agents.core.agent.GraphAIAgent
import ai.koog.agents.core.agent.entity.AIAgentGraphStrategy
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.features.eventHandler.feature.EventHandler
import ai.koog.agents.features.tokenizer.feature.MessageTokenizer
import ai.koog.prompt.executor.llms.SingleLLMPromptExecutor
import ai.koog.prompt.executor.ollama.client.OllamaClient
import ai.koog.prompt.executor.ollama.client.toLLModel
import ai.koog.prompt.message.Message
import ai.koog.prompt.tokenizer.SimpleRegexBasedTokenizer
import com.cacaosd.droidmind.agent.event.EventMapper
import com.cacaosd.droidmind.core.logging.Logger
import com.cacaosd.droidmind.domain.AgentClient
import com.cacaosd.droidmind.domain.AgentClientFactory
import com.cacaosd.droidmind.domain.AgentEvent
import com.cacaosd.droidmind.domain.McpMessage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.runBlocking
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi

class DefaultAgentClientFactory(
    private val toolRegistry: ToolRegistry,
    private val aiAgentStrategy: AIAgentGraphStrategy<String, String>,
    private val eventMapper: EventMapper,
    private val agentMessageFlow: MutableSharedFlow<McpMessage>,
    private val agentEventFlow: MutableSharedFlow<AgentEvent>,
    private val clock: Clock
) :
    AgentClientFactory {
    override fun createGoogleAgent(apiKey: String): AgentClient {
        val builder = provideGoogleAgentBuilder(apiKey)
            .withSystemPrompt(systemPrompt)
            .withMaxIterations(50)
            .withTemperature(.2)
            .withTools(toolRegistry)
            .withStrategy(aiAgentStrategy)
            .withFeatures {
                installEventHandler()
                installSimpleRegexTokenizer()
            }
        return DefaultAgentClient(builder)
    }

    override fun createMetaLLamaAgent(): AgentClient {
        val builder = provideMataLLama32AgentBuilder()
            .withSystemPrompt(systemPrompt)
            .withMaxIterations(50)
            .withTemperature(.2)
            .withTools(toolRegistry)
            .withStrategy(aiAgentStrategy)
            .withFeatures {
                installEventHandler()
                installSimpleRegexTokenizer()
            }
        return DefaultAgentClient(builder)
    }

    override fun createCustomModel(modelName: String): AgentClient {
        val ollamaClient = OllamaClient()
        val llmModel = runBlocking {
            ollamaClient.getModels().find { it.name == modelName }?.toLLModel()
                ?: error("Model not found")
        }
        val builder = AgentClientBuilder.create(llmModel, SingleLLMPromptExecutor(ollamaClient))
            .withSystemPrompt(systemPrompt)
            .withMaxIterations(50)
            .withTemperature(.2)
            .withTools(toolRegistry)
            .withStrategy(aiAgentStrategy)
            .withFeatures {
                installEventHandler()
                installSimpleRegexTokenizer()
            }
        return DefaultAgentClient(builder)
    }

    override fun createOllamaAgents(): List<AgentClient> {
        val ollamaClient = OllamaClient()
        val models = runBlocking { ollamaClient.getModels() }
        return models.map { modelInfo ->
            val llmModel = modelInfo.toLLModel()
            val builder = AgentClientBuilder.create(llmModel, SingleLLMPromptExecutor(ollamaClient))
                .withSystemPrompt(systemPrompt)
                .withMaxIterations(50)
                .withTemperature(.2)
                .withTools(toolRegistry)
                .withStrategy(aiAgentStrategy)
                .withFeatures {
                    installEventHandler()
                    installSimpleRegexTokenizer()
                }
            DefaultAgentClient(builder)
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
        private val systemPrompt = """
            SYSTEM PROMPT — DROIDMIND (MindScript)

            You are DroidMind, an intelligent automation agent with full control over a virtual device (Android emulator or iOS simulator).

            Your goal is to fulfill user intent efficiently and deterministically, even when instructions are vague or provided as plain text. User input may be MindScript or plain text; plain text must be inferred and internally converted into an executable MindScript plan.

            MindScript Structure:

            SCENARIO "Name"

            DEVICE <device_id>        (optional, defaults to first connected device)
            APP <app_package>         (optional, infer if omitted)

            DO
            - launch_app
            - tap <element_id>
            - input_text <element_id> "<text>"
            - send_key_event <key>
            - device_screenshot "<file_path>"
            - scroll_down <x1> <y1> <x2> <y2>
            - scroll_up <x1> <y1> <x2> <y2>
            - scroll_left <x1> <y1> <x2> <y2>
            - scroll_right <x1> <y1> <x2> <y2>

            EXPECT
            - UiVisible <element_id>
            - VerifyText <element_id> "<expected_text>"

            Execution Rules:

            - Automatically detect the active device unless the user specifies one.
            - Infer the correct app package via installed packages when not explicitly provided.
            - User-provided device IDs or app package names always take priority.
            - Convert each DO action into the correct tool invocation.
            - Call ui_dump after every action to refresh UI state.
            - Never use hard-coded coordinates for UI elements; locate elements via ui_dump and extract coordinates dynamically.
            - Execute EXPECT checks immediately and report pass/fail.
            - Recover from unexpected UI states or failures by adapting strategy.
            - Navigate proactively to complete the scenario.
            - Ask for clarification only when absolutely necessary.
            - Use only available tools.

            Available Tools:

            list_connected_devices  
            list_installed_packages  
            launch_app_by_package  
            ui_dump  
            input_text  
            tap  
            send_key_event  
            device_screenshot  
            scroll_down / scroll_up / scroll_left / scroll_right  
            verify_ui_text

            Mission:

            Interpret intent, execute deterministically, and verify results step-by-step using ui_dump.
        """.trimIndent()
    }
}
