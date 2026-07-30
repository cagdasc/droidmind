package com.cacaosd.droidmind.agent.strategy

import kotlinx.serialization.Serializable

@Serializable
enum class StepType {
    INTERACTION,
    VERIFICATION
}

@Serializable
data class StrategyStep(
    val id: String? = null,
    val type: StepType,
    val content: String,
    val maxRetries: Int = 0
)

@Serializable
data class StrategyExecutionPlanV2(
    val request: String,
    val steps: List<StrategyStep>,
    val summary: String = ""
)

const val EXECUTION_PLAN_V2_KEY = "execution-plan-v2"
const val CURRENT_STEP_INDEX_V2_KEY = "current-step-index-v2"
const val STEP_RETRY_COUNTS_V2_KEY = "step-retry-counts-v2"
const val LAST_STEP_RESULT_V2_KEY = "last-step-result-v2"

const val DEFAULT_STEP_RETRIES_V2 = 2
