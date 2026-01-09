package com.cacaosd.droidmind.feature

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun ChatScreenBranch(chatViewModel: ChatViewModel) {

    val chatScreenUiState by chatViewModel.chatScreenUiState.collectAsState()

    ChatScreen(
        chatScreenUiState = chatScreenUiState,
        onAction = chatViewModel::onAction
    )
}

sealed interface ChatScreenAction {
    data class DeviceSelected(val deviceData: DeviceData) : ChatScreenAction
    data class AppSelected(val installedApp: InstalledApp) : ChatScreenAction
    data class PromptChanged(val prompt: String) : ChatScreenAction
    data class RemoveChip(val chipItem: ChipItem) : ChatScreenAction
    data object RunScenarioClicked : ChatScreenAction
    data object StopScenarioClicked : ChatScreenAction
}
