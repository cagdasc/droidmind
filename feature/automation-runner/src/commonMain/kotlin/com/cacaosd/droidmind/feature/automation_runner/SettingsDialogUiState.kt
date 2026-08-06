package com.cacaosd.droidmind.feature.automation_runner

data class SettingsDialogUiState(
    val isOpen: Boolean = false,
    val androidHome: String = "",
    val geminiApiKey: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
