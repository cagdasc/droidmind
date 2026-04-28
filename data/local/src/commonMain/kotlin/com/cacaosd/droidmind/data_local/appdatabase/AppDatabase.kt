package com.cacaosd.droidmind.data_local.appdatabase

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.cacaosd.droidmind.data_local.appdatabase.converter.Converter
import com.cacaosd.droidmind.data_local.appdatabase.converter.InstantConverter
import com.cacaosd.droidmind.data_local.appdatabase.converter.UuidConverter
import com.cacaosd.droidmind.data_local.appdatabase.scenario.ScenarioDao
import com.cacaosd.droidmind.data_local.appdatabase.scenario.ScenarioDto

@Database(
    version = AppDatabase.LATEST_VERSION,
    entities = [ScenarioDto::class],
    exportSchema = true,
)
@TypeConverters(Converter::class, UuidConverter::class, InstantConverter::class)
abstract class AppDatabase : RoomDatabase() {

    companion object {
        const val LATEST_VERSION = 1
        const val DB_NAME = "droidmind_db.db"
    }

    abstract fun getScenarioDao(): ScenarioDao

}
