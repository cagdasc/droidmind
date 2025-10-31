package com.cacaosd.droidmind

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import com.cacaosd.droidmind.core.AppConfigManager
import com.cacaosd.droidmind.di.featureModule
import com.cacaosd.droidmind.di.mainModule
import com.cacaosd.droidmind.feature.ChatScreenBranch
import com.cacaosd.droidmind.feature.ChatViewModel
import com.cacaosd.droidmind.ui_theme.AppTheme
import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Preview
fun App() {
    KoinApplication(application = {
        modules(mainModule, featureModule)
        val appConfigManager = koin.get<AppConfigManager>()
        appConfigManager.initializeApp()
    }) {
        AppTheme {
            val chatViewModel = koinViewModel<ChatViewModel>()
            Surface {
                ChatScreenBranch(chatViewModel)
            }
        }
    }


}
