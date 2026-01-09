package com.cacaosd.droidmind.feature.automation_runner

import com.cacaosd.droidmind.feature.ChipItem
import com.cacaosd.droidmind.feature.DeviceData
import com.cacaosd.droidmind.feature.ExecutionState
import com.cacaosd.droidmind.feature.InstalledApp

data class AutomationRunnerUiState(
    val deviceDataList: List<DeviceData> = emptyList(),
    val installedApps: List<InstalledApp> = emptyList(),
    val selectedDevice: DeviceData? = null,
    val selectedApp: InstalledApp? = null,
    val chipItems: Set<ChipItem> = emptySet(),
    val logEntrySources: Set<EntrySource> = setOf(EntrySource.DEVICE, EntrySource.AGENT),
    val executionState: ExecutionState = ExecutionState.Idle,
    val automationScenarios: List<AutomationScenario> = emptyList(),
    val selectedAutomationScenario: AutomationScenario? = null,
)

data class AutomationScenario(
    val name: String,
    val description: String,
    val prompt: String,
    val isActive: Boolean = false,
    val messages: List<LogEntry> = emptyList(),
    val inputTokensCount: String = "0",
    val outputTokensCount: String = "0",
    val totalTokensCount: String = "0",
)

data class LogEntry(
    val timestamp: String,
    val entrySource: EntrySource,
    val message: String,
)

enum class EntrySource {
    DEVICE,
    AGENT,
}
