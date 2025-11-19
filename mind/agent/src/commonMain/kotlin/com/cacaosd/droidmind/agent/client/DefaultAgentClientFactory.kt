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
import ai.koog.prompt.tokenizer.SimpleRegexBasedTokenizer
import com.cacaosd.droidmind.agent.event.EventMapper
import com.cacaosd.droidmind.core.logging.Logger
import com.cacaosd.droidmind.domain.AgentClient
import com.cacaosd.droidmind.domain.AgentClientFactory
import com.cacaosd.droidmind.domain.McpMessage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.runBlocking
import kotlin.uuid.ExperimentalUuidApi

class DefaultAgentClientFactory(
    private val toolRegistry: ToolRegistry,
    private val aiAgentStrategy: AIAgentGraphStrategy<String, String>,
    private val eventMapper: EventMapper,
    private val agentEventFlow: MutableSharedFlow<McpMessage>
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
            }

            onAgentCompleted {
                Logger.info("Agent has finished execution.")
            }

            onLLMCallCompleted { context ->
                val responses = context.responses
                val mcpMessages = responses.map { response ->
                    eventMapper.mapToMcpMessages(response)
                }.flatten()

                agentEventFlow.emitAll(mcpMessages.asFlow())
            }

            onAgentExecutionFailed { context ->
                val strategyName = context.runId
                val throwable = context.throwable
                agentEventFlow.emit(
                    McpMessage.Response.AssistantWithError(
                        strategyName = strategyName,
                        throwable = throwable
                    )
                )
            }

            onToolCallCompleted { context ->
                agentEventFlow.emit(
                    McpMessage.Response.ToolResult(
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
            7. Interaction: Perform actions like tap, input_text, send_key_event, device_screenshot, and swipe to simulate real user behavior. Execute "UI Understanding" after any interaction to ensure latest state of screen.
            8. Proactive Navigation: Move through apps and system settings as needed to accomplish tasks—without waiting for explicit instructions.
            9. Error Handling: If a task fails or unexpected behavior occurs, adjust your strategy intelligently to recover or reroute.
            10. Minimal Clarification: Only ask for user input when absolutely necessary; prefer to resolve ambiguity through observation or inference.
        You act like a human-level assistant with operational control, UI awareness, and autonomous decision-making. Your mission is not to wait, but to complete.
        """.trimIndent()
    }
}
