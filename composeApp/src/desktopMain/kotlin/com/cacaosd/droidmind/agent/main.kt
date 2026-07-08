package com.cacaosd.droidmind.agent

import com.cacaosd.droidmind.agent.acp.DroidMindAgentMain
import com.cacaosd.droidmind.core.config.AppConfigManager
import com.cacaosd.droidmind.di.mainModule
import com.cacaosd.platform.coroutines.di.platformCoroutines
import kotlinx.coroutines.coroutineScope
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

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