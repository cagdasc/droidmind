package com.cacaosd.droidmind.data_local.appdatabase.scenario

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ScenarioDao {

    @Query("SELECT * FROM scenario_table ORDER BY timestamp DESC")
    abstract suspend fun getScenarios(): List<ScenarioDto>

    @Query("SELECT * FROM scenario_table ORDER BY timestamp DESC")
    abstract fun getScenariosFlow(): Flow<List<ScenarioDto>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(scenarioDto: ScenarioDto)

    @Query("DELETE FROM scenario_table WHERE id = :scenarioId")
    abstract suspend fun remove(scenarioId: String)

    @Delete
    abstract suspend fun remove(scenarioDto: ScenarioDto)
}
