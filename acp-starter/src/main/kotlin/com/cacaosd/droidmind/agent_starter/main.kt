package com.cacaosd.droidmind.agent_starter

import ai.koog.utils.time.KoogClock
import com.cacaosd.droidmind.agent.acp.DroidMindAgentMain
import com.cacaosd.droidmind.agent.di.AgentEventFlowQualifier
import com.cacaosd.droidmind.agent.di.agentModule
import com.cacaosd.droidmind.agent.di.agentToolsModule
import com.cacaosd.droidmind.core.config.AppConfigManager
import com.cacaosd.droidmind.core.config.di.coreConfigModule
import com.cacaosd.droidmind.domain.AgentEvent
import com.cacaosd.droidmind.mind.device.di.deviceModule
import com.cacaosd.droidmind.mind.layout.di.layoutModule
import com.cacaosd.droidmind.mind.verifier.di.verifierModule
import com.cacaosd.platform.coroutines.di.platformCoroutines
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.dsl.module
import kotlin.time.Clock

val mainModule = module {
    includes(
        coreConfigModule,
        layoutModule,
        verifierModule,
        deviceModule,
        agentModule,
        agentToolsModule,
    )

    single<MutableSharedFlow<AgentEvent>>(AgentEventFlowQualifier) { MutableSharedFlow() }
    single<Clock> { Clock.System }
    single<KoogClock> { KoogClock.System }
}

suspend fun main() = coroutineScope {
    startKoin {
        allowOverride(false)
        platformCoroutines()
        modules(mainModule)
    }
    val koin = GlobalContext.get()
    val appConfigManager = koin.get<AppConfigManager>()
    appConfigManager.initializeApp()

    val droidMindAgentMain = koin.get<DroidMindAgentMain>()
    droidMindAgentMain.run()
}