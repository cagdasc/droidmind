package com.cacaosd.droidmind.agent.strategy.persistence

import ai.koog.agents.core.agent.entity.AIAgentStorage
import ai.koog.agents.core.agent.entity.createStorageKey
import ai.koog.agents.core.dsl.builder.node
import com.cacaosd.droidmind.agent.strategy.CURRENT_STEP_INDEX_V2_KEY
import com.cacaosd.droidmind.agent.strategy.EXECUTION_PLAN_V2_KEY
import com.cacaosd.droidmind.agent.strategy.StrategyExecutionPlanV2
import com.cacaosd.droidmind.core.logging.Logger
import kotlinx.serialization.json.Json

internal val executionPlanV2Key = createStorageKey<String>(EXECUTION_PLAN_V2_KEY)
internal val currentStepIndexKey = createStorageKey<Int>(CURRENT_STEP_INDEX_V2_KEY)

internal fun storeExecutionPlan(json: Json) = node<String, String> { rawJson ->
    try {
        val executionPlan = getExecutionPlan(storage, json, rawJson)
        storage.set(executionPlanV2Key, json.encodeToString(executionPlan))
        storage.set(currentStepIndexKey, 0)
        executionPlan.request
    } catch (e2: Exception) {
        Logger.error("storeExecutionPlan", e2)
        // If parsing fails, propagate original request so downstream can still try
        rawJson
    }
}

internal suspend fun getExecutionPlan(storage: AIAgentStorage, json: Json, rawJson: String?): StrategyExecutionPlanV2 {
    val planJson =
        rawJson ?: storage.get(executionPlanV2Key) ?: throw IllegalStateException("No execution plan found in storage")
    return json.decodeFromString<StrategyExecutionPlanV2>(planJson)
}