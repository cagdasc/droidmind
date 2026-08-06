package com.cacaosd.droidmind.feature.automation_runner

sealed interface AutomationRunnerAction {
    data object AddScenarioClicked : AutomationRunnerAction
    data class DeviceSelected(val deviceData: DeviceData) : AutomationRunnerAction
    data class AppSelected(val installedApp: InstalledApp) : AutomationRunnerAction
    data class LLMSelected(val llmData: LLMData) : AutomationRunnerAction
    data class RemoveChip(val chipItem: ChipItem) : AutomationRunnerAction
    data class UpdatePrompt(val prompt: String) : AutomationRunnerAction
    data class UpdateName(val name: String) : AutomationRunnerAction
    data class UpdateShortDescription(val shortDescription: String) : AutomationRunnerAction
    data class RunScenarioClicked(val automationScenario: AutomationScenario) : AutomationRunnerAction
    data object StopScenarioClicked : AutomationRunnerAction
    data object ClearLogs : AutomationRunnerAction
    data class PromptModeChanged(val promptMode: PromptMode) : AutomationRunnerAction
    data class ScenarioSelected(val scenario: AutomationScenario) : AutomationRunnerAction
    data class RemoveScenarioClicked(val scenario: AutomationScenario) : AutomationRunnerAction

    // Settings dialog actions
    data object SettingsDialogClicked : AutomationRunnerAction
    data object SettingsDialogDismissed : AutomationRunnerAction
    data class SaveSettings(val androidHome: String, val geminiApiKey: String) : AutomationRunnerAction
}
