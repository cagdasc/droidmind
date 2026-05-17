package com.cacaosd.droidmind.domain.local.scenario

import kotlinx.coroutines.flow.Flow
import java.util.*

interface ScenarioRepository {

    fun getScenarios(): Flow<Result<List<ScenarioModel>>>

    suspend fun saveScenario(scenario: ScenarioModel): Result<Unit>

    suspend fun removeScenario(scenarioId: UUID): Result<Unit>
}
