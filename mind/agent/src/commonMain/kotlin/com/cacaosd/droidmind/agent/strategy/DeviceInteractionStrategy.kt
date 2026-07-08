@file:OptIn(InternalAgentsApi::class)

package com.cacaosd.droidmind.agent.strategy

import ai.koog.agents.core.agent.entity.createStorageKey
import ai.koog.agents.core.annotation.InternalAgentsApi
import ai.koog.agents.core.dsl.builder.node
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.ext.agent.CriticResult
import ai.koog.agents.ext.agent.subgraphWithTask
import ai.koog.agents.ext.agent.subgraphWithVerification
import com.cacaosd.droidmind.agent.tools.DeviceControllerTools
import com.cacaosd.droidmind.agent.tools.UiHierarchyTools
import com.cacaosd.droidmind.agent.tools.UiInteractionTools
import kotlinx.serialization.Serializable

/**
 * Structured result of Layer 1 (device identification).
 * Carrying `request` forward means later layers don't depend on the LLM "remembering"
 * the original ask across subgraph boundaries.
 */
@Serializable
@LLMDescription("Result of locating the target device/emulator and preparing the app under test")
data class ProvisioningResult(
    @property:LLMDescription("True only if the device is ready and the app is in the foreground")
    val ready: Boolean,
    @property:LLMDescription("The original test request, copied verbatim so later phases keep context")
    val request: String,
    @property:LLMDescription("Reason provisioning failed. Empty when ready = true")
    val reason: String = ""
)

/**
 * Structured result of Layer 2 (UI interaction).
 */
@Serializable
@LLMDescription("Summary of the UI actions that were attempted on the current screen")
data class InteractionResult(
    @property:LLMDescription("The original test request, copied verbatim")
    val request: String,
    @property:LLMDescription("Precisely what was done on screen (taps, text entry, scrolls), detailed enough to verify")
    val summary: String
)

/**
 * AndroidAgentClient implements the Agent Client Protocol (ACP) for Android devices.
 * The strategy is split into three explicit layers:
 *   1. Device identification  -> subgraphWithTask<String, ProvisioningResult>
 *   2. UI interaction         -> subgraphWithTask<ProvisioningResult, InteractionResult>
 *   3. UI verification        -> subgraphWithVerification<InteractionResult> (built-in critic subgraph)
 *
 * Verification failures loop back into interaction with feedback, bounded by MAX_VERIFICATION_ATTEMPTS
 * so a stubborn UI state can't cause an infinite loop.
 */
class AndroidAgentClient(
    private val deviceControllerTools: DeviceControllerTools,
    private val uiHierarchyTools: UiHierarchyTools,
    private val uiInteractionTools: UiInteractionTools
) {

    private val maxVerificationAttempts = 3
    private val verificationAttemptsKey = createStorageKey<Int>("verification-attempts")

    fun createStrategy() = strategy<String, String>("device_interaction") {

        // ---- Layer 1: Device identification ----------------------------------------------
        val identifyEmulatorAndApp by subgraphWithTask<String, ProvisioningResult>(
            name = "identify_device_and_app",
            tools = deviceControllerTools.asTools()
        ) { request ->
            """
            Phase: Device identification.
            Identify the target emulator/device, install the app if needed, and launch it so it is
            in the foreground and ready for UI testing.
            If this cannot be achieved, report `ready = false` with a clear `reason` instead of guessing.
            Always copy the original request verbatim into the `request` field.
 
            Request: $request
            """.trimIndent()
        }

        // ---- Layer 2: UI interaction -------------------------------------------------------
        val interactWithApp by subgraphWithTask<ProvisioningResult, InteractionResult>(
            name = "interact_with_app",
            tools = uiHierarchyTools.asTools() + uiInteractionTools.asTools()
        ) { provisioning ->
            """
            Phase: UI interaction.
            Execute the requested actions on the current screen. Read the UI hierarchy first to find
            the right elements, then perform the interaction.
            Describe precisely what you did so it can be checked afterwards.
            Always copy the original request verbatim into the `request` field.
 
            Request: ${provisioning.request}
            """.trimIndent()
        }

        // ---- Layer 3: UI verification (built-in critic subgraph) ---------------------------
        val verifyInteraction by subgraphWithVerification<InteractionResult>(
            tools = uiHierarchyTools.asTools()
        ) { interaction ->
            """
            Phase: UI verification.
            Read the current UI hierarchy and confirm whether the request below was actually satisfied
            on screen. Be strict: only approve if the visible UI state matches the request.
 
            Original request: ${interaction.request}
            Actions performed: ${interaction.summary}
            """.trimIndent()
        }

        // Small helper node: bumps the retry counter and turns critic feedback into a fresh
        // ProvisioningResult so it can feed back into interactWithApp's input type.
        val prepareRetry by node<CriticResult<InteractionResult>, ProvisioningResult> { critic ->
            val attempts = (storage.get(verificationAttemptsKey) ?: 0) + 1
            storage.set(verificationAttemptsKey, attempts)
            ProvisioningResult(
                ready = true,
                request = "${critic.input.request}\n\nPrevious attempt was insufficient: ${critic.feedback}"
            )
        }

        edge(nodeStart forwardTo identifyEmulatorAndApp)

        // Provisioning failed -> stop immediately, don't waste turns interacting with a broken app.
        edge(
            identifyEmulatorAndApp forwardTo nodeFinish
                    onCondition { !it.ready }
                    transformed { "Could not prepare the device/app: ${it.reason}" }
        )

        edge(
            identifyEmulatorAndApp forwardTo interactWithApp
                    onCondition { it.ready }
        )

        edge(interactWithApp forwardTo verifyInteraction)

        // Verified -> finish with a clean summary.
        edge(
            verifyInteraction forwardTo nodeFinish
                    onCondition { it.successful }
                    transformed { "Done: ${it.input.summary}" }
        )

        // Not verified but out of retries -> give up with the last feedback (checked BEFORE the
        // generic retry edge below, since edges are evaluated in the order they're defined).
        edge(
            verifyInteraction forwardTo nodeFinish
                    onCondition {
                !it.successful && (storage.get(verificationAttemptsKey) ?: 0) >= maxVerificationAttempts
            }
                    transformed { "Gave up after $maxVerificationAttempts attempts. Last feedback: ${it.feedback}" }
        )

        // Not verified, retries left -> loop back into interaction with feedback.
        edge(
            verifyInteraction forwardTo prepareRetry
                    onCondition { !it.successful }
        )

        edge(prepareRetry forwardTo interactWithApp)
    }
}
