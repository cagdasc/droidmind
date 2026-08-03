package com.cacaosd.droidmind.agent.strategy

import ai.koog.agents.core.agent.entity.createStorageKey
import ai.koog.agents.core.annotation.InternalAgentsApi
import ai.koog.agents.core.dsl.builder.strategy
import com.cacaosd.droidmind.agent.strategy.persistence.requiresVerificationKey
import com.cacaosd.droidmind.agent.strategy.persistence.storeClassificationInfoTask
import com.cacaosd.droidmind.agent.strategy.task.*
import com.cacaosd.droidmind.agent.tools.DeviceInfoTools
import com.cacaosd.droidmind.agent.tools.DeviceManagerTools
import com.cacaosd.droidmind.agent.tools.UiHierarchyTools
import com.cacaosd.droidmind.agent.tools.UiInteractionTools

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
class OneShotDeviceInteractionStrategy(
    private val deviceManagerTools: DeviceManagerTools,
    private val deviceInfoTools: DeviceInfoTools,
    private val uiHierarchyTools: UiHierarchyTools,
    private val uiInteractionTools: UiInteractionTools
) {

    private val maxVerificationAttempts = 3
    private val verificationAttemptsKey = createStorageKey<Int>("verification-attempts")

    @OptIn(InternalAgentsApi::class)
    fun createStrategy() = strategy<String, String>("device_interaction") {

        // ---- Layer 0: Request classification / scope gate ----------------------------------
        // No tools: this is a pure judgment call, not a task, so it shouldn't touch the device.
        val classifyRequest by classifyRequestTask()

        // Records whether this run should go through layer 3 at all. This is decided in code from
        // the classifier's own field rather than re-asked-for later, so it can't drift as the LLM
        // hands structured results between subgraphs.
        val applyClassification by storeClassificationInfoTask()

        // ---- Layer 1: Device identification ----------------------------------------------
        val identifyEmulatorAndApp by deviceAndAppIdentificationTask(deviceManagerTools.asTools())

        // ---- Layer 2: UI interaction -------------------------------------------------------
        val interactWithApp by appInteractionTask(uiHierarchyTools.asTools() + uiInteractionTools.asTools())

        // ---- Layer 3: UI verification (built-in critic subgraph) ---------------------------
        val verifyInteraction by interactionVerificationTask(uiHierarchyTools.asTools())

        // Small helper node: bumps the retry counter and turns critic feedback into a fresh
        // ProvisioningResult so it can feed back into interactWithApp's input type.
        val prepareRetry by prepareForRetryTask()

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
                    transformed { it.summary }
        )

        edge(
            interactWithApp forwardTo verifyInteraction
                    onCondition { storage.get(requiresVerificationKey) == true }
        )

        // Verified -> finish with a clean summary.
        edge(
            verifyInteraction forwardTo nodeFinish
                    onCondition { it.successful }
                    transformed { it.input.summary }
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
