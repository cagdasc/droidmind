package com.cacaosd.droidmind.agent.acp.di

import ai.koog.utils.time.KoogClock
import com.cacaosd.droidmind.agent.acp.DroidMindAgentMain
import com.cacaosd.droidmind.agent.strategy.di.SteppedInteractionVerificationQualifier
import com.cacaosd.droidmind.agent.strategy.di.strategyModule
import com.cacaosd.droidmind.agent.tools.di.toolsModule
import com.cacaosd.droidmind.core.config.di.coreConfigModule
import org.koin.dsl.module

val acpModule = module {
    includes(coreConfigModule, toolsModule, strategyModule)

    single<KoogClock> { KoogClock.System }

    single {
        DroidMindAgentMain(
            clock = get(),
            toolRegistry = get(),
            aiAgentStrategy = get(SteppedInteractionVerificationQualifier),
            platformDispatchers = get()
        )
    }
}
