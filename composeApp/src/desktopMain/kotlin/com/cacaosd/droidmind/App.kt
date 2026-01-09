package com.cacaosd.droidmind

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import com.cacaosd.droidmind.core.config.AppConfigManager
import com.cacaosd.droidmind.di.featureModule
import com.cacaosd.droidmind.di.mainModule
import com.cacaosd.droidmind.feature.ChatViewModel
import com.cacaosd.droidmind.feature.automation_runner.AutomationRunnerScreen
import com.cacaosd.droidmind.feature.automation_runner.AutomationRunnerViewModel
import com.cacaosd.droidmind.feature.colorSchemeProvider
import com.cacaosd.platform.coroutines.di.platformCoroutines
import com.cacaosd.uikit.theme.AppTheme
import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Preview
fun App() {
    KoinApplication(application = {
        allowOverride(false)
        platformCoroutines()
        modules(mainModule, featureModule)
        val appConfigManager = koin.get<AppConfigManager>()
        appConfigManager.initializeApp()
    }) {
        AppTheme.colorSchemeProvider = colorSchemeProvider
        AppTheme {
            val chatViewModel = koinViewModel<ChatViewModel>()
            val automationRunnerViewModel = koinViewModel<AutomationRunnerViewModel>()
            Surface {
                AutomationRunnerScreen(automationRunnerViewModel)
            }
        }
    }
}

