package com.cacaosd.droidmind.feature

import com.cacaosd.droidmind.adb.layout_optimizer.UiElement
import java.time.Instant
import java.util.*

data class ChatScreenUiState(
    val deviceDataList: List<DeviceData> = emptyList(),
    val installedApps: List<InstalledApp> = emptyList(),
    val selectedDevice: DeviceData? = null,
    val selectedApp: InstalledApp? = null,
    val chipItems: Set<ChipItem> = emptySet(),
    val prompt: String = "",
    val messages: List<MessageBubble> = emptyList(),
    val executionState: ExecutionState = ExecutionState.Idle,
    val inputTokensCount: String = "0",
    val outputTokensCount: String = "0",
    val totalTokensCount: String = "0",
    val rootUiElement: UiElement? = null,
)

data class DeviceData(
    val name: String,
    val serial: String,
    val batteryLevel: Int = 0,
    val screenWidth: Int,
    val screenHeight: Int,
    val osVersion: String = "",
) : ChipItem {
    override val label: String
        get() = name
}

data class InstalledApp(
    val packageName: String,
) : ChipItem {
    override val label: String
        get() = packageName
}

interface ChipItem {
    val label: String
}

sealed class ExecutionState {
    data object Idle : ExecutionState()
    data object Executing : ExecutionState()
    data class Success(val message: String? = null) : ExecutionState()
    data class Error(val error: Throwable) : ExecutionState()
}

/**
 * Represents the sender of a message in a conversation.
 *
 * This sealed class defines the different types of senders that can be involved
 * in a chat or conversation context, providing type safety and exhaustive handling.
 */
sealed class MessageOwner {
    abstract val displayName: String

    data object Assistant : MessageOwner() {
        override val displayName: String = "Assistant"
    }

    data object User : MessageOwner() {
        override val displayName: String = "User"
    }

    data class Tool(val toolName: String) : MessageOwner() {
        override val displayName: String = "Tool - $toolName"
    }
}

/**
 * Represents a message bubble in a conversation.
 *
 * This sealed interface defines the different types of messages that can appear
 * in a chat or conversation context, providing type safety and exhaustive handling.
 */
sealed interface MessageBubble {
    val id: String
    val owner: MessageOwner
    val content: String
    val timestamp: Instant
    val metadata: Map<String, Any>

    /**
     * Represents a request/input message from a user or system.
     *
     * @param id Unique identifier for the message.
     * @param owner The sender of the request (null for anonymous).
     * @param content The actual message content.
     * @param timestamp When the message was created.
     * @param metadata Additional metadata associated with the message.
     */
    data class Request(
        override val id: String = UUID.randomUUID().toString(),
        override val owner: MessageOwner,
        override val content: String,
        override val timestamp: Instant = Instant.now(),
        override val metadata: Map<String, Any> = emptyMap()
    ) : MessageBubble {
        init {
            require(content.isNotBlank()) { "Request content cannot be blank" }
        }
    }

    /**
     * Represents a response/output message from an AI agent or system.
     *
     * @param id Unique identifier for the message.
     * @param owner The sender of the response (null for anonymous).
     * @param content The actual message content.
     * @param timestamp When the message was created.
     * @param metadata Additional metadata associated with the message.
     * @param requestId Optional ID of the request this response is answering.
     */
    data class Response(
        override val id: String = UUID.randomUUID().toString(),
        override val owner: MessageOwner,
        override val content: String,
        override val timestamp: Instant = Instant.now(),
        override val metadata: Map<String, Any> = emptyMap(),
        val requestId: String? = null,
        val throwable: Throwable? = null
    ) : MessageBubble {
        init {
            require(content.isNotBlank()) { "Response content cannot be blank" }
        }
    }

    companion object {
        /**
         * Creates a request message with the given content and optional sender.
         */
        fun request(content: String, sender: MessageOwner): Request {
            return Request(owner = sender, content = content)
        }

        /**
         * Creates a response message with the given content and optional sender.
         */
        fun response(
            content: String,
            sender: MessageOwner,
            requestId: String? = null,
            throwable: Throwable? = null
        ): Response {
            return Response(owner = sender, content = content, requestId = requestId, throwable = throwable)
        }

        /**
         * Creates a request message with additional metadata.
         */
        fun requestWithMetadata(
            content: String,
            sender: MessageOwner,
            metadata: Map<String, Any>
        ): Request {
            return Request(owner = sender, content = content, metadata = metadata)
        }

        /**
         * Creates a response message with additional metadata.
         */
        fun responseWithMetadata(
            content: String,
            sender: MessageOwner,
            requestId: String? = null,
            metadata: Map<String, Any>
        ): Response {
            return Response(
                owner = sender,
                content = content,
                requestId = requestId,
                metadata = metadata
            )
        }
    }
}

/**
 * Extension functions for MessageBubble
 */

/**
 * Checks if this message is a request.
 */
fun MessageBubble.isRequest(): Boolean = this is MessageBubble.Request

/**
 * Checks if this message is a response.
 */
fun MessageBubble.isResponse(): Boolean = this is MessageBubble.Response

/**
 * Gets the message as a Request if it is one, null otherwise.
 */
fun MessageBubble.asRequest(): MessageBubble.Request? = this as? MessageBubble.Request

/**
 * Gets the message as a Response if it is one, null otherwise.
 */
fun MessageBubble.asResponse(): MessageBubble.Response? = this as? MessageBubble.Response

/**
 * Returns a formatted string representation of the message.
 */
fun MessageBubble.format(): String {
    val typePrefix = when (this) {
        is MessageBubble.Request -> "REQUEST"
        is MessageBubble.Response -> "RESPONSE"
    }
    return "$typePrefix: [$owner] $content"
}
