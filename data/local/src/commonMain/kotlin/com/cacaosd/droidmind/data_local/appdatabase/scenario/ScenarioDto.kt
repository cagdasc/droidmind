package com.cacaosd.droidmind.data_local.appdatabase.scenario

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.util.*

@Entity(tableName = "scenario_table")
data class ScenarioDto(
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "prompt") val prompt: String,
    @ColumnInfo(name = "short_description") val shortDescription: String,
    @ColumnInfo(name = "timestamp") var timestamp: Instant,
    @PrimaryKey
    @ColumnInfo(name = "id") val id: UUID = UUID.randomUUID(),
)
