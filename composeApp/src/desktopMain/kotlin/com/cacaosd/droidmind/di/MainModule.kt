package com.cacaosd.droidmind.di

import com.cacaosd.droidmind.agent.client.di.AgentEventFlowQualifier
import com.cacaosd.droidmind.agent.client.di.agentClientModule
import com.cacaosd.droidmind.core.config.di.coreConfigModule
import com.cacaosd.droidmind.data_local.di.dataLocalModule
import com.cacaosd.droidmind.domain.AgentEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import org.koin.dsl.module

val mainModule = module {
    includes(
        coreConfigModule,
        agentClientModule,
        dataLocalModule
    )

    single<MutableSharedFlow<AgentEvent>>(AgentEventFlowQualifier) { MutableSharedFlow() }
}
