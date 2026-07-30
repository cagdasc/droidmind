package com.cacaosd.droidmind.agent.strategy

import ai.koog.agents.core.agent.entity.AIAgentStorage
import ai.koog.agents.core.agent.entity.ToolSelectionStrategy
import ai.koog.agents.core.agent.entity.createStorageKey
import ai.koog.agents.core.annotation.InternalAgentsApi
import ai.koog.agents.core.dsl.builder.node
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.ext.agent.CriticResult
import ai.koog.agents.ext.agent.subgraphWithTask
import ai.koog.agents.ext.agent.subgraphWithVerification
import ai.koog.serialization.typeToken
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

    // Storage keys for execution plan control (variant keys declared in model files)
    private val executionPlanV2Key = createStorageKey<String>(EXECUTION_PLAN_V2_KEY)
    private val currentStepIndexKey = createStorageKey<Int>(CURRENT_STEP_INDEX_V2_KEY)
    private val lastInteractionKey = createStorageKey<String>(LAST_STEP_RESULT_V2_KEY)

    @OptIn(InternalAgentsApi::class)
    fun createStrategy() = strategy<String, String>("stepped_device_interaction") {

        val json = Json { ignoreUnknownKeys = true }

        // ---- Layer 0: Request classification / scope gate (no tools) ---------------------
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
            set requiresVerification = true if the request explicitly or implicitly asks to verify,
            Do not attempt to fulfill the request yourself here - only classify it.
            Always copy the original request verbatim into the `request` field.

            Request: $request
            """.trimIndent()
        }

        // ---- Prompt rewrite: produce an ordered plan (ask LLM to return JSON for either v1 or v2)
        val rewritePrompt by subgraphWithTask<PromptClassification, String>(
            name = "rewrite_prompt",
            tools = emptyList()
        ) { promptClassification ->
            """
            Phase: Prompt rewriting.
            Rewrite the original request into a machine-readable ordered execution plan. 
            Be careful about below points:
            * Launching app is not an INTERACTION step and visibility of app is not a VERIFICATION step.
            They have already done before plan is started.
            * If the user requests a device interaction such as search, do not handle the search steps separately such as
            Click search field, type the text and search it(by tapping search icon or enter). It is contextual command.
            Identify if there are commands like that and combine in one step until you identify another step.
             
            Output JSON only.
            Accept format below. If you find any special char, escape them if needed. 

            (StrategyExecutionPlanV2): { "request":"ORIGINAL_CONTENT", "steps": [{"id":"s1","type":"INTERACTION|VERIFICATION","content":"...","maxRetries":1}], "summary":"..." }

            The ordered steps may interleave interaction and verification steps. Include precisely what
            should be done or checked in each step's content field.
            
            Return only the JSON payload.

            Original request: ${promptClassification.request}
            """.trimIndent()
        }

        // Parse and persist the plan (supports both variants); set current step = 0
        val persistPlan by node<String, String> { rawJson ->
            try {
                val planV2 = json.decodeFromString<StrategyExecutionPlanV2>(rawJson)
                storage.set(executionPlanV2Key, json.encodeToString(planV2))
                storage.set(currentStepIndexKey, 0)
                planV2.request
            } catch (e2: Exception) {
                Logger.error("persistPlan", e2)
                // If parsing fails, propagate original request so downstream can still try
                rawJson
            }
        }

        // ---- Layer 1: Device identification ----------------------------------------------
        val identifyEmulatorAndApp by subgraphWithTask<String, ProvisioningResult>(
            name = "identify_device_and_app",
            tools = deviceManagerTools.asTools()
        ) { request ->
            """
            Phase: Device identification.
            Identify the target emulator/device and launch the app so it is
            in the foreground and ready for interaction.
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
            the right elements/text, then perform the interaction.
            Describe precisely what you did so it can be checked afterwards.
            Use the step content as the Request field.

            Request: ${provisioning.request}
            """.trimIndent()
        }

        // ---- Layer 3: UI verification (built-in critic subgraph) ---------------------------
        val verifyInteraction by subgraphWithVerification<InteractionResult>(
            name = "verify_interaction",
            inputType = typeToken<InteractionResult>(),
            toolSelectionStrategy = ToolSelectionStrategy.Tools(uiHierarchyTools.asTools().map { it.descriptor })
        ) { interaction ->
            """
            Phase: UI verification.
            Read the current UI hierarchy and confirm whether the request below was actually satisfied
            on screen. Be strict: only approve if the visible UI state matches the request.

            Original request: ${interaction.request}
            Actions performed: ${interaction.summary}
            """.trimIndent()
        }

        val summarizeResult by subgraphWithVerification<InteractionResult>(
            name = "summarize_result",
            inputType = typeToken<InteractionResult>(),
            toolSelectionStrategy = ToolSelectionStrategy.NONE
        ) { interaction ->
            """
            Phase: Summarize result.
            If you reach that stage everything should work fine. Summarize the user request and explain what you did.

            Request: ${interaction.request}
            """.trimIndent()
        }

        // Build an InteractionResult for verification-only steps (use last interaction if present)
        val buildInteractionForVerification by node<ProvisioningResult, InteractionResult> { prov ->
            val lastJson = storage.get(lastInteractionKey)
            if (lastJson != null) {
                try {
                    val lastInteraction = json.decodeFromString<InteractionResult>(lastJson)
                    InteractionResult(
                        request = prov.request,
                        summary = "Last interaction was ${lastInteraction.request}."
                    )
                } catch (e: Exception) {
                    Logger.error("buildInteractionForVerification", e)
                    InteractionResult(request = prov.request, summary = "No actions performed; verification only.")
                }
            } else {
                InteractionResult(request = prov.request, summary = "No actions performed; verification only.")
            }
        }

        // Store interaction result and advance step index
        val storeLastInteraction by node<InteractionResult, InteractionResult> { interaction ->
            storage.set(lastInteractionKey, json.encodeToString(interaction))
            val idx = (storage.get(currentStepIndexKey) ?: 0) + 1
            storage.set(currentStepIndexKey, idx)
            interaction
        }

        // On verification failure: bump attempts and move to nearest prior interaction step to retry
        val handleVerificationFailure by node<CriticResult<InteractionResult>, ProvisioningResult> { critic ->
            val attempts = (storage.get(verificationAttemptsKey) ?: 0) + 1
            storage.set(verificationAttemptsKey, attempts)

            // Try to find previous interaction step index
            var prevIdx: Int? = null
            try {
                val planJson = storage.get(executionPlanV2Key) ?: ""
                val plan = json.decodeFromString<StrategyExecutionPlanV2>(planJson)
                val current = (storage.get(currentStepIndexKey) ?: 0)
                for (i in (current - 1) downTo 0) {
                    if (plan.steps.getOrNull(i)?.type == StepType.INTERACTION) {
                        prevIdx = i; break
                    }
                }
            } catch (e: Exception) {
                Logger.error("handleVerificationFailure", e)
                // ignore parsing errors here
            }

            if (prevIdx != null) {
                storage.set(currentStepIndexKey, prevIdx)
            }
            // no prior interaction to retry; keep the same request but let attempts govern eventual give-up
            ProvisioningResult(
                ready = true,
                request = "${critic.input.request}\n\nPrevious attempt insufficient: ${critic.feedback}"
            )

        }

        // Helper node: read current step and replace the provisioning.request with step.content
        val applyCurrentStep by node<ProvisioningResult, ProvisioningResult> { prov ->
            val idx = storage.get(currentStepIndexKey) ?: 0
            try {
                val planJson = storage.get(executionPlanV2Key) ?: ""
                val plan = json.decodeFromString<StrategyExecutionPlanV2>(planJson)
                val step = plan.steps.getOrNull(idx)
                if (step != null) ProvisioningResult(ready = prov.ready, request = step.content) else prov
            } catch (e: Exception) {
                Logger.error("applyCurrentStep", e)
                prov
            }
        }

        // Utility to check current step type from storage
        suspend fun isCurrentStepInteraction(storage: AIAgentStorage): Boolean {
            val idx = storage.get(currentStepIndexKey) ?: 0
            try {
                val planJson = storage.get(executionPlanV2Key) ?: ""
                val plan = json.decodeFromString<StrategyExecutionPlanV2>(planJson)
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
                val planJson = storage.get(executionPlanV2Key) ?: ""
                val plan = json.decodeFromString<StrategyExecutionPlanV2>(planJson)
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
