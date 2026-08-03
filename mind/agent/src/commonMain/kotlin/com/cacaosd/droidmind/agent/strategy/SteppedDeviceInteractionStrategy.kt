package com.cacaosd.droidmind.agent.strategy

import ai.koog.agents.core.agent.entity.AIAgentStorage
import ai.koog.agents.core.agent.entity.createStorageKey
import ai.koog.agents.core.annotation.InternalAgentsApi
import ai.koog.agents.core.dsl.builder.strategy
import com.cacaosd.droidmind.agent.strategy.persistence.currentStepIndexKey
import com.cacaosd.droidmind.agent.strategy.persistence.getExecutionPlan
import com.cacaosd.droidmind.agent.strategy.persistence.storeExecutionPlan
import com.cacaosd.droidmind.agent.strategy.persistence.storeInteraction
import com.cacaosd.droidmind.agent.strategy.task.*
import com.cacaosd.droidmind.agent.tools.DeviceInfoTools
import com.cacaosd.droidmind.agent.tools.DeviceManagerTools
import com.cacaosd.droidmind.agent.tools.UiHierarchyTools
import com.cacaosd.droidmind.agent.tools.UiInteractionTools
import com.cacaosd.droidmind.core.logging.Logger
import kotlinx.serialization.json.Json

/**
 * Stepped strategy: executes an ordered plan (interaction/verification steps) produced by a
 * prompt rewrite phase. Supports both ExecutionPlan (variant 1) and StrategyExecutionPlanV2 (variant 2).
 */
class SteppedDeviceInteractionStrategy(
    private val deviceManagerTools: DeviceManagerTools,
    private val deviceInfoTools: DeviceInfoTools,
    private val uiHierarchyTools: UiHierarchyTools,
    private val uiInteractionTools: UiInteractionTools
) {

    private val maxVerificationAttempts = 3
    private val verificationAttemptsKey = createStorageKey<Int>("verification-attempts")

    @OptIn(InternalAgentsApi::class)
    fun createStrategy() = strategy<String, String>("stepped_device_interaction") {

        val json = Json { ignoreUnknownKeys = true }

        // ---- Layer 0: Request classification / scope gate (no tools) ---------------------
        val classifyRequest by classifyRequestTask()

        // ---- Prompt rewrite: produce an ordered plan (ask LLM to return JSON for either v1 or v2)
        val rewritePrompt by promptRewriterTask()

        // Parse and persist the plan (supports both variants); set current step = 0
        val persistPlan by storeExecutionPlan(json)

        // ---- Layer 1: Device identification ----------------------------------------------
        val identifyEmulatorAndApp by deviceAndAppIdentificationTask(deviceManagerTools.asTools())

        // ---- Layer 2: UI interaction -------------------------------------------------------
        val interactWithApp by appInteractionTask(uiHierarchyTools.asTools() + uiInteractionTools.asTools())

        // ---- Layer 3: UI verification (built-in critic subgraph) ---------------------------
        val verifyInteraction by interactionVerificationTask(uiHierarchyTools.asTools())

        val summarizeResult by interactionSummarizeTask()

        // Build an InteractionResult for verification-only steps (use last interaction if present)
        val buildInteractionForVerification by createVerificationInteraction(json)

        // Store interaction result and advance step index
        val storeLastInteraction by storeInteraction(json)

        // On verification failure: bump attempts and move to nearest prior interaction step to retry
        val handleVerificationFailure by handleVerificationFailure(json)

        // Helper node: read current step and replace the provisioning.request with step.content
        val applyCurrentStep by getCurrentStepTask(json)

        // Utility to check current step type from storage
        suspend fun isCurrentStepInteraction(storage: AIAgentStorage): Boolean {
            val idx = storage.get(currentStepIndexKey) ?: 0
            try {
                val plan = getExecutionPlan(storage, json, null)
                Logger.info("isCurrentStepInteraction($idx): ${plan.steps.getOrNull(idx)}")
                return plan.steps.getOrNull(idx)?.type == StepType.INTERACTION
            } catch (e: Exception) {
                Logger.error("isCurrentStepInteraction", e)
            }
            return false
        }

        suspend fun isCurrentStepVerification(storage: AIAgentStorage): Boolean {
            return !isCurrentStepInteraction(storage)
        }

        suspend fun hasMoreSteps(storage: AIAgentStorage): Boolean {
            val idx = storage.get(currentStepIndexKey) ?: 0
            return try {
                val plan = getExecutionPlan(storage, json, null)
                Logger.info("Check has more steps, current step $idx, plan size ${plan.steps.size}")
                idx < plan.steps.size
            } catch (e: Exception) {
                Logger.error("storeLastInteraction forwardTo nodeFinish", e)
                false
            }
        }

        // Start edges
        edge(edgeIntermediate = nodeStart forwardTo classifyRequest)

        edge(
            edgeIntermediate = classifyRequest forwardTo nodeFinish
                    onCondition { !it.inScope }
                    transformed { "I can only help with mobile app testing or device-control tasks. ${it.reason}".trim() }
        )

        edge(
            edgeIntermediate = classifyRequest forwardTo rewritePrompt
                    onCondition { it.inScope }
        )
        edge(edgeIntermediate = rewritePrompt forwardTo persistPlan)
        edge(edgeIntermediate = persistPlan forwardTo identifyEmulatorAndApp)

        // Provisioning failed -> stop immediately
        edge(
            edgeIntermediate = identifyEmulatorAndApp forwardTo nodeFinish
                    onCondition { !it.ready }
                    transformed { "Could not prepare the device/app: ${it.reason}" }
        )

        // Provisioning succeeded -> apply current step and dispatch
        edge(
            edgeIntermediate = identifyEmulatorAndApp forwardTo applyCurrentStep
                    onCondition { it.ready }
        )

        edge(
            edgeIntermediate = applyCurrentStep forwardTo interactWithApp
                    onCondition { isCurrentStepInteraction(storage) }
        )

        edge(
            edgeIntermediate = applyCurrentStep forwardTo buildInteractionForVerification
                    onCondition { isCurrentStepVerification(storage) }
        )

        // After an interaction: store result, advance index, loop or finish
        edge(edgeIntermediate = interactWithApp forwardTo storeLastInteraction)

        edge(
            edgeIntermediate = storeLastInteraction forwardTo summarizeResult
                    onCondition { !hasMoreSteps(storage) }
        )
        edge(
            edgeIntermediate = summarizeResult forwardTo nodeFinish
                    transformed {
                if (it.successful) {
                    "Request is successfully executed. Here is the summary ${it.feedback}"
                } else {
                    "Request cannot executed successfully. Here is the summary ${it.feedback}"
                }
            }
        )

        edge(
            edgeIntermediate = storeLastInteraction forwardTo applyCurrentStep
                    onCondition { hasMoreSteps(storage) }
                    transformed { ProvisioningResult(ready = true, request = it.request) }
        )

        // Verification path
        edge(edgeIntermediate = buildInteractionForVerification forwardTo verifyInteraction)

        edge(
            edgeIntermediate = verifyInteraction forwardTo summarizeResult
                    onCondition { it.successful && !hasMoreSteps(storage) }
                    transformed { it.input }
        )

        edge(
            edgeIntermediate = verifyInteraction forwardTo storeLastInteraction
                    onCondition { it.successful && hasMoreSteps(storage) }
                    transformed { InteractionResult(request = it.input.request, summary = it.feedback) }
        )

        edge(
            edgeIntermediate = verifyInteraction forwardTo nodeFinish
                    onCondition {
                !it.successful || !hasMoreSteps(storage)
                //                        && (storage.get(verificationAttemptsKey) ?: 0) >= maxVerificationAttempts
            }
                    transformed { "Gave up after $maxVerificationAttempts attempts. Last feedback: ${it.feedback}" }
        )

//        edge(
//            verifyInteraction forwardTo handleVerificationFailure
//                    onCondition {
//                !it.successful && (storage.get(verificationAttemptsKey)
//                    ?: 0) < maxVerificationAttempts
//            }
//        )
//
//        edge(
//            handleVerificationFailure forwardTo applyCurrentStep
//                    onCondition { true }
//        )
    }
}
