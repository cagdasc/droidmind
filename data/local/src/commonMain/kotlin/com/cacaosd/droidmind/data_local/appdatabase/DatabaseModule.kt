package com.cacaosd.droidmind.data_local.appdatabase

import com.cacaosd.droidmind.core.config.AppConfigManager
import com.cacaosd.droidmind.core.config.di.coreConfigModule
import com.cacaosd.droidmind.data_local.appdatabase.scenario.ScenarioDao
import com.cacaosd.platform.coroutines.dispatchers.PlatformDispatchers
import org.koin.dsl.module

internal val databaseModule = module {
    includes(coreConfigModule)
    single { provideAppDatabase(appConfigManager = get(), platformDispatchers = get()) }
    single { provideScenarioDao(appDatabase = get()) }
}

expect fun provideAppDatabase(appConfigManager: AppConfigManager, platformDispatchers: PlatformDispatchers): AppDatabase

fun provideScenarioDao(appDatabase: AppDatabase): ScenarioDao = appDatabase.getScenarioDao()
