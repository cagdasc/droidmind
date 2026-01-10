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
import kotlin.uuid.ExperimentalUuidApi

class DefaultAgentClientFactory(
    private val toolRegistry: ToolRegistry,
    private val aiAgentStrategy: AIAgentGraphStrategy<String, String>,
    private val eventMapper: EventMapper,
    private val agentMessageFlow: MutableSharedFlow<McpMessage>,
    private val agentEventFlow: MutableSharedFlow<AgentEvent>,
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

    private fun GraphAIAgent.FeatureContext.installSimpleRegexTokenizer() {
        install(MessageTokenizer) {
            tokenizer = SimpleRegexBasedTokenizer()
        }
    }

    private fun GraphAIAgent.FeatureContext.installEventHandler() {
        install(EventHandler) {
            onAgentStarting {
                Logger.info("Agent is starting...")
                agentEventFlow.emit(AgentEvent.Started)
            }

            onAgentCompleted {
                Logger.info("Agent has finished execution.")
                agentEventFlow.emit(AgentEvent.Completed)
            }

            onLLMCallStarting { context ->
                val prompt = context.prompt
                Logger.info("LLM Call Starting with prompt: $prompt")
                prompt.messages.map { it.content }.forEach { message ->
                    agentEventFlow.emit(AgentEvent.Prompt(content = message))
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
                            finishReason = message.finishReason
                        )

                        is Message.Tool.Call -> AgentEvent.Response.ToolCall(
                            toolName = message.tool,
                            content = message.content
                        )
                    }

                    val metadataMessage = AgentEvent.Token(
                        inputTokensCount = message.metaInfo.inputTokensCount ?: 0,
                        outputTokensCount = message.metaInfo.outputTokensCount ?: 0,
                        totalTokensCount = message.metaInfo.totalTokensCount ?: 0
                    )

                    agentEventFlow.emit(metadataMessage)
                    agentEventFlow.emit(mcpMessage)
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
                        throwable = throwable
                    )
                )
            }

            onToolCallFailed { context ->
                val toolName = context.tool.name
                val throwable = context.throwable
                agentEventFlow.emit(
                    AgentEvent.Failure(
                        reason = toolName,
                        throwable = throwable
                    )
                )
            }

            onToolValidationFailed { context ->
                val toolName = context.tool.name
                val error = context.error
                agentEventFlow.emit(
                    AgentEvent.Failure(
                        reason = toolName,
                        throwable = Throwable(error)
                    )
                )
            }

            onToolCallCompleted { context ->
                agentMessageFlow.emit(
                    McpMessage.Response.ToolResult(
                        toolName = context.tool.name,
                        content = context.result
                    )
                )

                agentEventFlow.emit(
                    AgentEvent.Response.ToolResult(
                        toolName = context.tool.name,
                        content = context.result
                    )
                )
            }
        }
    }

    companion object {
        private val systemPrompt =
            """
        You are an intelligent automation agent with full control over an virtual device such as Android emulator or iOS simulator.
        Your primary goal is to efficiently fulfill the user’s intent—even if the user provides vague or incomplete instructions. You are not limited to passive responses; instead, you take initiative and make autonomous decisions to drive workflows forward.
        You are equipped with the following capabilities:
            1. Device Management: Automatically detect the active device using list_connected_devices.
            2. App Discovery: Identify the correct package name using list_installed_packages, based on app name or any user description.
            3. User Overrides: If the user provides a specific device serial and/or app package name, you must prioritize and use them directly.
            4. Prompt-Based Inference: If the user does not provide an app name or package, try to infer the target app from the context of the prompt and resolve its package name automatically.
            5. App Control: Launch any app with launch_app_by_package using either inferred or user-provided package name and device serial.
            6. UI Understanding: Use get_ui_dump to parse and understand the current screen’s structure and elements.
            7. Interaction: Perform actions like tap, input_text, send_key_event, device_screenshot, vertical_scroll_down, vertical_scroll_up, horizontal_scroll_right, horizontal_scroll_left to simulate real user behavior. Execute "UI Understanding" after any interaction to ensure latest state of screen.
            8. Proactive Navigation: Move through apps and system settings as needed to accomplish tasks—without waiting for explicit instructions.
            9. Error Handling: If a task fails or unexpected behavior occurs, adjust your strategy intelligently to recover or reroute.
            10. Minimal Clarification: Only ask for user input when absolutely necessary; prefer to resolve ambiguity through observation or inference.
        You act like a human-level assistant with operational control, UI awareness, and autonomous decision-making. Your mission is not to wait, but to complete.
        """.trimIndent()
    }
}
