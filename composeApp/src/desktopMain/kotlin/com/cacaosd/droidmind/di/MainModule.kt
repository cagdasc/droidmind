@file:OptIn(ExperimentalUuidApi::class)

package com.cacaosd.droidmind.di

import com.cacaosd.droidmind.agent.di.AgentEventFlowQualifier
import com.cacaosd.droidmind.agent.di.AgentMessageFlowQualifier
import com.cacaosd.droidmind.agent.di.agentModule
import com.cacaosd.droidmind.agent.di.agentToolsModule
import com.cacaosd.droidmind.agent.event.EventMapper
import com.cacaosd.droidmind.core.config.di.coreConfigModule
import com.cacaosd.droidmind.domain.AgentEvent
import com.cacaosd.droidmind.domain.McpMessage
import com.cacaosd.droidmind.localProperties
import com.cacaosd.droidmind.mind.device.di.deviceModule
import com.cacaosd.droidmind.mind.layout.di.layoutModule
import com.cacaosd.droidmind.mind.verifier.di.verifierModule
import kotlinx.coroutines.flow.MutableSharedFlow
import org.koin.dsl.module
import java.util.*
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi

val mainModule = module {
    includes(
        coreConfigModule,
        layoutModule,
        verifierModule,
        deviceModule,
        agentModule,
        agentToolsModule
    )

    single { EventMapper() }
    single<MutableSharedFlow<McpMessage>>(AgentMessageFlowQualifier) { MutableSharedFlow() }
    single<MutableSharedFlow<AgentEvent>>(AgentEventFlowQualifier) { MutableSharedFlow() }
    single<Properties> { localProperties }
    single<Clock> { Clock.System }
}
