package com.cacaosd.droidmind.data_local.implementation.scenario

import com.cacaosd.droidmind.data_local.appdatabase.scenario.ScenarioDao
import com.cacaosd.droidmind.data_local.mapper.ScenarioMapper
import com.cacaosd.droidmind.domain.local.scenario.ScenarioModel
import com.cacaosd.droidmind.domain.local.scenario.ScenarioRepository
import com.cacaosd.droidmind.domain.result.asResultFlow
import com.cacaosd.droidmind.domain.result.getAsResult
import com.cacaosd.platform.coroutines.dispatchers.PlatformDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.*

class ScenarioRepositoryImpl(
    private val scenarioDao: ScenarioDao,
    private val scenarioMapper: ScenarioMapper,
    private val platformDispatchers: PlatformDispatchers,
) : ScenarioRepository {

    override fun getScenarios(): Flow<Result<List<ScenarioModel>>> {
        return scenarioDao.getScenariosFlow().map {
            scenarioMapper.toDomain(it)
        }.asResultFlow(coroutineContext = platformDispatchers.io)
    }

    override suspend fun saveScenario(scenario: ScenarioModel): Result<Unit> {
        return getAsResult(coroutineContext = platformDispatchers.io) {
            scenarioDao.insert(scenarioMapper.toDto(scenario))
        }
    }

    override suspend fun removeScenario(scenarioId: UUID): Result<Unit> {
        return getAsResult(coroutineContext = platformDispatchers.io) {
            scenarioDao.remove(scenarioId.toString())
        }
    }
}