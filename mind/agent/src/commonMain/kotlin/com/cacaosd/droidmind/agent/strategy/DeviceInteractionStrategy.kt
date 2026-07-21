package com.cacaosd.droidmind.agent.strategy

import ai.koog.agents.core.agent.entity.createStorageKey
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
 * Structured result of Layer 0 (request classification / scope gate).
 */
@Serializable
@LLMDescription("Classification of whether a user request is in scope for a mobile app testing/device-control agent")
data class PromptClassification(
    @property:LLMDescription(
        "True only if the request is about testing, inspecting, or controlling a mobile app, " +
                "emulator, or device - e.g. launching/installing an app, tapping/typing/scrolling on screen, " +
                "reading UI state, verifying on-screen behavior, or managing an emulator/device"
    )
    val inScope: Boolean,
    @property:LLMDescription("The original request, copied verbatim, to hand off to later phases when in scope")
    val request: String,
    @property:LLMDescription(
        "True only if the request explicitly or implicitly asks to verify, check, confirm, or test the " +
                "outcome (e.g. contains wording like 'verify', 'check that', 'make sure', 'confirm', 'test that'). " +
                "False for a plain action request where no confirmation of the result was asked for."
    )
    val requiresVerification: Boolean = false,
    @property:LLMDescription("Short, user-facing explanation when inScope = false. Empty when inScope = true")
    val reason: String = ""
)

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
 * The strategy is split into four explicit layers:
 *   0. Request classification -> subgraphWithTask<String, PromptClassification>   (scope gate, no tools)
 *   1. Device identification  -> subgraphWithTask<String, ProvisioningResult>
 *   2. UI interaction         -> subgraphWithTask<ProvisioningResult, InteractionResult>
 *   3. UI verification        -> subgraphWithVerification<InteractionResult> (built-in critic subgraph)
 *
 * Out-of-scope requests (anything not about testing/controlling a mobile app or device) are rejected
 * right after layer 0, before any device or UI tools are ever touched.
 * Layer 3 only runs when the classifier (layer 0) decided the request actually asked for verification;
 * otherwise the agent finishes right after layer 2 with a plain summary of what was done.
 * Verification failures loop back into interaction with feedback, bounded by maxVerificationAttempts
 * so a stubborn UI state can't cause an infinite loop.
 */
class AndroidAgentClient(
    private val deviceControllerTools: DeviceControllerTools,
    private val uiHierarchyTools: UiHierarchyTools,
    private val uiInteractionTools: UiInteractionTools
) {

    private val maxVerificationAttempts = 3
    private val verificationAttemptsKey = createStorageKey<Int>("verification-attempts")
    private val requiresVerificationKey = createStorageKey<Boolean>("requires-verification")

    fun createStrategy() = strategy<String, String>("device_interaction") {

        // ---- Layer 0: Request classification / scope gate ----------------------------------
        // No tools: this is a pure judgment call, not a task, so it shouldn't touch the device.
        val classifyRequest by subgraphWithTask<String, PromptClassification>(
            name = "classify_request",
            tools = emptyList()
        ) { request ->
            """
            Phase: Request classification.
            Decide whether the request below is about testing, inspecting, or controlling a mobile
            (Android) application or device: launching/installing an app, tapping/typing/scrolling on
            screen, reading UI state, verifying on-screen behavior, or managing an emulator/device.
            If it's unrelated (general chit-chat, unrelated coding help, unrelated questions, etc.),
            set inScope = false and give a brief, user-facing reason.
            Do not attempt to fulfill the request yourself here - only classify it.
            Always copy the original request verbatim into the `request` field.

            Request: $request
            """.trimIndent()
        }

        // Records whether this run should go through layer 3 at all. This is decided in code from
        // the classifier's own field rather than re-asked-for later, so it can't drift as the LLM
        // hands structured results between subgraphs.
        val applyClassification by node<PromptClassification, String> { classification ->
            storage.set(requiresVerificationKey, classification.requiresVerification)
            classification.request
        }

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

        edge(nodeStart forwardTo classifyRequest)

        // Out of scope -> stop right here, before any device/UI tool is ever touched.
        edge(
            classifyRequest forwardTo nodeFinish
                    onCondition { !it.inScope }
                    transformed { "I can only help with mobile app testing or device-control tasks. ${it.reason}".trim() }
        )

        edge(
            classifyRequest forwardTo applyClassification
                    onCondition { it.inScope }
        )

        edge(applyClassification forwardTo identifyEmulatorAndApp)

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

        // No verification requested -> finish right after the action, layer 3 never runs.
        edge(
            interactWithApp forwardTo nodeFinish
                    onCondition { storage.get(requiresVerificationKey) != true }
                    transformed { "Done: ${it.summary}" }
        )

        edge(
            interactWithApp forwardTo verifyInteraction
                    onCondition { storage.get(requiresVerificationKey) == true }
        )

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
