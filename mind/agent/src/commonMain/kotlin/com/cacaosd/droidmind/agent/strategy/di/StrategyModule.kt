package com.cacaosd.droidmind.agent.strategy.di

import com.cacaosd.droidmind.agent.strategy.OneShotDeviceInteractionStrategy
import com.cacaosd.droidmind.agent.strategy.SteppedDeviceInteractionStrategy
import com.cacaosd.droidmind.agent.tools.di.toolsModule
import com.cacaosd.droidmind.core.config.di.SelfResolveQualifier
import org.koin.dsl.module

data object OneshotInteractionVerificationQualifier : SelfResolveQualifier()
data object SteppedInteractionVerificationQualifier : SelfResolveQualifier()

internal val strategyModule = module {
    includes(toolsModule)

    single(qualifier = OneshotInteractionVerificationQualifier) {
        OneShotDeviceInteractionStrategy(
            deviceManagerTools = get(),
            deviceInfoTools = get(),
            uiHierarchyTools = get(),
            uiInteractionTools = get()
        ).createStrategy()
    }

    single(qualifier = SteppedInteractionVerificationQualifier) {
        SteppedDeviceInteractionStrategy(
            deviceManagerTools = get(),
            deviceInfoTools = get(),
            uiHierarchyTools = get(),
            uiInteractionTools = get()
        ).createStrategy()
    }
}
