package com.cacaosd.droidmind.agent.acp.v2

import com.agentclientprotocol.agent.AgentSession
import com.agentclientprotocol.common.Event
import com.agentclientprotocol.model.*
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.time.delay
import kotlinx.serialization.json.JsonElement
import java.time.Duration

class DroidMindAgentSession(
    override val sessionId: SessionId,
) : AgentSession {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override suspend fun prompt(
        content: List<ContentBlock>,
        @Suppress("LocalVariableName")
        _meta: JsonElement?
    ): Flow<Event> = flow {

        // Send initial plan
        sendPlan()

        // Echo the user's message
        for (block in content) {
            emit(Event.SessionUpdateEvent(SessionUpdate.UserMessageChunk(block)))
            delay(Duration.ofMillis(100)) // Simulate processing time
        }

        // Send agent response
        val responseText = "I received your message: ${
            content.filterIsInstance<ContentBlock.Text>()
                .joinToString(" ") { it.text }
        }"

        emit(
            Event.SessionUpdateEvent(
                SessionUpdate.AgentMessageChunk(
                    ContentBlock.Text(responseText)
                )
            )
        )
    }

    override suspend fun cancel() {
        logger.info { "Canceling ACP agent" }
    }

    private suspend fun FlowCollector<Event>.sendPlan() {
        val plan = Plan(
            listOf(
                PlanEntry("Process user input", PlanEntryPriority.HIGH, PlanEntryStatus.IN_PROGRESS),
                PlanEntry("Generate response", PlanEntryPriority.HIGH, PlanEntryStatus.PENDING),
                PlanEntry("Execute tools if needed", PlanEntryPriority.MEDIUM, PlanEntryStatus.PENDING)
            )
        )

        emit(
            Event.SessionUpdateEvent(
                SessionUpdate.PlanUpdate(plan.entries)
            )
        )
    }

    private suspend fun FlowCollector<Event>.simulateToolCall() {
        val toolCallId = ToolCallId("tool-${System.currentTimeMillis()}")

        // Start tool call
        emit(
            Event.SessionUpdateEvent(
                SessionUpdate.ToolCallUpdate(
                    toolCallId = toolCallId,
                    title = "Reading current directory",
                    kind = ToolKind.READ,
                    status = ToolCallStatus.PENDING,
                    locations = listOf(ToolCallLocation(".")),
                    content = emptyList()
                )
            )
        )

        delay(Duration.ofMillis(500)) // Simulate work

        // Update to in progress
        emit(
            Event.SessionUpdateEvent(
                SessionUpdate.ToolCallUpdate(
                    toolCallId = toolCallId,
                    status = ToolCallStatus.IN_PROGRESS
                )
            )
        )

        delay(Duration.ofMillis(500)) // Simulate work

        // Complete the tool call
        emit(
            Event.SessionUpdateEvent(
                SessionUpdate.ToolCallUpdate(
                    toolCallId = toolCallId,
                    status = ToolCallStatus.COMPLETED,
                    content = listOf(
                        ToolCallContent.Content(
                            ContentBlock.Text("Directory listing completed successfully")
                        )
                    )
                )
            )
        )
    }
}