@file:OptIn(InternalAgentsApi::class)

package com.cacaosd.droidmind.agent.strategy.task

import ai.koog.agents.core.agent.entity.ToolSelectionStrategy
import ai.koog.agents.core.agent.entity.createStorageKey
import ai.koog.agents.core.annotation.InternalAgentsApi
import ai.koog.agents.core.dsl.builder.node
import ai.koog.agents.core.tools.ToolBase
import ai.koog.agents.ext.agent.CriticResult
import ai.koog.agents.ext.agent.subgraphWithVerification
import ai.koog.serialization.typeToken
import com.cacaosd.droidmind.agent.strategy.InteractionResult
import com.cacaosd.droidmind.agent.strategy.ProvisioningResult
import com.cacaosd.droidmind.agent.strategy.StepType
import com.cacaosd.droidmind.agent.strategy.StrategyExecutionPlanV2
import com.cacaosd.droidmind.agent.strategy.persistence.currentStepIndexKey
import com.cacaosd.droidmind.agent.strategy.persistence.executionPlanV2Key
import com.cacaosd.droidmind.agent.strategy.persistence.lastInteractionKey
import com.cacaosd.droidmind.core.logging.Logger
import kotlinx.serialization.json.Json

val verificationAttemptsKey = createStorageKey<Int>("verification-attempts")

internal fun interactionVerificationTask(tools: List<ToolBase<*, *>>) = subgraphWithVerification<InteractionResult>(
    name = "verify_interaction",
    inputType = typeToken<InteractionResult>(),
    toolSelectionStrategy = ToolSelectionStrategy.Tools(tools.map { it.descriptor })
) { interaction ->
    """
    Phase: UI verification.
    Read the current UI hierarchy and confirm whether the request below was actually satisfied
    on screen. Be strict: only approve if the visible UI state matches the request.

    Original request: ${interaction.request}
    Actions performed: ${interaction.summary}
    """.trimIndent()
}

internal fun createVerificationInteraction(json: Json) = node<ProvisioningResult, InteractionResult> { prov ->
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

internal fun prepareForRetryTask() = node<CriticResult<InteractionResult>, ProvisioningResult> { critic ->
    val attempts = (storage.get(verificationAttemptsKey) ?: 0) + 1
    storage.set(verificationAttemptsKey, attempts)
    ProvisioningResult(
        ready = true,
        request = "${critic.input.request}\n\nPrevious attempt was insufficient: ${critic.feedback}"
    )
}

internal fun handleVerificationFailure(json: Json) =
    node<CriticResult<InteractionResult>, ProvisioningResult> { critic ->
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