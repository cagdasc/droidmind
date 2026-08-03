package com.cacaosd.droidmind.agent.strategy.persistence

import ai.koog.agents.core.agent.entity.createStorageKey
import ai.koog.agents.core.dsl.builder.node
import com.cacaosd.droidmind.agent.strategy.InteractionResult
import com.cacaosd.droidmind.agent.strategy.LAST_STEP_RESULT_V2_KEY
import kotlinx.serialization.json.Json

internal val lastInteractionKey = createStorageKey<String>(LAST_STEP_RESULT_V2_KEY)

fun storeInteraction(json: Json) = node<InteractionResult, InteractionResult> { interaction ->
    storage.set(lastInteractionKey, json.encodeToString(interaction))
    val idx = (storage.get(currentStepIndexKey) ?: 0) + 1
    storage.set(currentStepIndexKey, idx)
    interaction
}
