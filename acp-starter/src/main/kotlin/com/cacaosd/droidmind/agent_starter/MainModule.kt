package com.cacaosd.droidmind.agent_starter

import com.cacaosd.droidmind.agent.acp.di.acpModule
import com.cacaosd.droidmind.core.config.di.coreConfigModule
import org.koin.dsl.module

val mainModule = module {
    includes(
        coreConfigModule,
        acpModule,
    )
}
