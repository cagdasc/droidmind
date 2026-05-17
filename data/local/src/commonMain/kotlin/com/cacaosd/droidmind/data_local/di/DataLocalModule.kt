package com.cacaosd.droidmind.data_local.di

import com.cacaosd.droidmind.data_local.implementation.scenario.ScenarioRepositoryImpl
import com.cacaosd.droidmind.data_local.mapper.ScenarioMapper
import com.cacaosd.droidmind.domain.local.scenario.ScenarioRepository
import org.koin.dsl.bind
import org.koin.dsl.module

val dataLocalModule = module {
    single { ScenarioMapper() }

    single {
        ScenarioRepositoryImpl(
            scenarioDao = get(),
            scenarioMapper = get(),
            platformDispatchers = get(),
        )
    } bind ScenarioRepository::class

}
