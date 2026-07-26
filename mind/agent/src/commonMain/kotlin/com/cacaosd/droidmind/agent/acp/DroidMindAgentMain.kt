package com.cacaosd.droidmind.agent.acp

import ai.koog.agents.core.agent.entity.AIAgentGraphStrategy
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.prompt.executor.clients.google.GoogleLLMClient
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import ai.koog.prompt.executor.ollama.client.OllamaClient
import ai.koog.prompt.llm.LLMProvider
import ai.koog.utils.time.KoogClock
import com.agentclientprotocol.agent.Agent
import com.agentclientprotocol.protocol.Protocol
import com.agentclientprotocol.transport.StdioTransport
import com.cacaosd.droidmind.core.logging.Logger
import com.cacaosd.platform.coroutines.dispatchers.PlatformDispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.util.*

class DroidMindAgentMain(
    private val clock: KoogClock,
    private val toolRegistry: ToolRegistry,
    private val aiAgentStrategy: AIAgentGraphStrategy<String, String>,
    private val properties: Properties,
    private val platformDispatchers: PlatformDispatchers
) {

    suspend fun run() = coroutineScope {
        Logger.info("Starting ACP Agent")

        val agentTransport = StdioTransport(
            parentScope = this,
            ioDispatcher = platformDispatchers.io,
            input = BufferedInputStream(System.`in`).asSource().buffered(),
            output = BufferedOutputStream(System.out).asSink().buffered(),
            name = "droidmind_agent_transport"
        )

        val apiKey = properties.getProperty("GEMINI_API_KEY")
        val promptExecutor = MultiLLMPromptExecutor(
            mapOf(LLMProvider.Ollama to OllamaClient(), LLMProvider.Google to GoogleLLMClient(apiKey)),
        )

        try {
            val agentJob = launch {
                val agentProtocol = Protocol(parentScope = this, transport = agentTransport)

                Agent(
                    protocol = agentProtocol,
                    agentSupport = DroidMindAgentSupport(
                        clock = clock,
                        protocol = agentProtocol,
                        toolRegistry = toolRegistry,
                        aiAgentStrategy = aiAgentStrategy,
                        promptExecutor = promptExecutor,
                    )
                )

                Logger.info("Agent initialized, starting protocol")
                agentProtocol.start()
            }

            // Wait for the agent job to complete
            agentJob.join()
            Logger.info("Agent job completed")

        } finally {
            agentTransport.close()
            promptExecutor.close()
        }
    }
}