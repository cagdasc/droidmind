package com.cacaosd.droidmind.agent.client.di

import ai.koog.utils.time.KoogClock
import com.cacaosd.droidmind.agent.client.DefaultAgentClientFactory
import com.cacaosd.droidmind.agent.session.DefaultScenarioExecutor
import com.cacaosd.droidmind.agent.strategy.di.SteppedInteractionVerificationQualifier
import com.cacaosd.droidmind.agent.strategy.di.strategyModule
import com.cacaosd.droidmind.agent.tools.di.toolsModule
import com.cacaosd.droidmind.core.config.di.SelfResolveQualifier
import com.cacaosd.droidmind.core.config.di.coreConfigModule
import com.cacaosd.droidmind.domain.AgentClientFactory
import com.cacaosd.droidmind.domain.session.ScenarioExecutor
import com.cacaosd.droidmind.mind.device.di.AndroidDeviceManagerQualifier
import org.koin.dsl.bind
import org.koin.dsl.module

data object AgentEventFlowQualifier : SelfResolveQualifier()

val agentClientModule = module {
    includes(coreConfigModule, toolsModule, strategyModule)

    single<KoogClock> { KoogClock.System }

    single {
        DefaultAgentClientFactory(
            appConfigManager = get(),
            toolRegistry = get(),
            aiAgentStrategy = get(SteppedInteractionVerificationQualifier),
            agentEventFlow = get(AgentEventFlowQualifier),
            clock = get(),
            koogClock = get()
        )
    } bind AgentClientFactory::class

    single {
        DefaultScenarioExecutor(
            deviceController = get(AndroidDeviceManagerQualifier)
        )
    } bind ScenarioExecutor::class
}
