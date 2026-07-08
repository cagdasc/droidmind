@file:OptIn(ExperimentalUuidApi::class)

package com.cacaosd.droidmind.di

import ai.koog.utils.time.KoogClock
import com.cacaosd.droidmind.agent.di.AgentEventFlowQualifier
import com.cacaosd.droidmind.agent.di.agentModule
import com.cacaosd.droidmind.agent.di.agentToolsModule
import com.cacaosd.droidmind.core.config.di.coreConfigModule
import com.cacaosd.droidmind.data_local.appdatabase.databaseModule
import com.cacaosd.droidmind.data_local.di.dataLocalModule
import com.cacaosd.droidmind.domain.AgentEvent
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
        agentToolsModule,
        databaseModule,
        dataLocalModule
    )

    single<MutableSharedFlow<AgentEvent>>(AgentEventFlowQualifier) { MutableSharedFlow() }
    single<Properties> { localProperties }
    single<Clock> { Clock.System }
    single<KoogClock> { KoogClock.System }
}
