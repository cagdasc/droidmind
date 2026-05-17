package com.cacaosd.droidmind.data_local.appdatabase

import com.cacaosd.droidmind.core.config.AppConfigManager
import com.cacaosd.droidmind.core.config.di.coreConfigModule
import com.cacaosd.droidmind.data_local.appdatabase.scenario.ScenarioDao
import org.koin.dsl.module

val databaseModule = module {
    includes(coreConfigModule)
    single { provideAppDatabase(appConfigManager = get()) }
    single { provideScenarioDao(appDatabase = get()) }
}

expect fun provideAppDatabase(appConfigManager: AppConfigManager): AppDatabase

fun provideScenarioDao(appDatabase: AppDatabase): ScenarioDao = appDatabase.getScenarioDao()
