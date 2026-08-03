package com.cacaosd.droidmind.agent.strategy.task

import ai.koog.agents.core.dsl.builder.node
import com.cacaosd.droidmind.agent.strategy.ProvisioningResult
import com.cacaosd.droidmind.agent.strategy.StrategyExecutionPlanV2
import com.cacaosd.droidmind.agent.strategy.persistence.currentStepIndexKey
import com.cacaosd.droidmind.agent.strategy.persistence.executionPlanV2Key
import com.cacaosd.droidmind.core.logging.Logger
import kotlinx.serialization.json.Json

internal fun getCurrentStepTask(json: Json) = node<ProvisioningResult, ProvisioningResult> { prov ->
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