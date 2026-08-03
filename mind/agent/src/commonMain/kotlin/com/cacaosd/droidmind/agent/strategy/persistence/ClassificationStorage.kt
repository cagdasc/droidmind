package com.cacaosd.droidmind.agent.strategy.persistence

import ai.koog.agents.core.agent.entity.createStorageKey
import ai.koog.agents.core.dsl.builder.node
import com.cacaosd.droidmind.agent.strategy.PromptClassification

internal val requiresVerificationKey = createStorageKey<Boolean>("requires-verification")

internal fun storeClassificationInfoTask() = node<PromptClassification, String> { classification ->
    storage.set(requiresVerificationKey, classification.requiresVerification)
    classification.request
}
