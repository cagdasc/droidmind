package com.cacaosd.droidmind.feature.automation_runner

data class AutomationRunnerUiState(
    val deviceDataList: List<DeviceData> = emptyList(),
    val availableLLMs: List<LLMData> = emptyList(),
    val selectedLLM: LLMData? = null,
    val installedApps: List<InstalledApp> = emptyList(),
    val selectedDevice: DeviceData? = null,
    val selectedApp: InstalledApp? = null,
    val chipItems: Set<ChipItem> = emptySet(),
    val logEntryState: LogEntryState = LogEntryState(),
    val executionState: ExecutionState = ExecutionState.Idle,
    val automationScenarios: List<AutomationScenario> = emptyList(),
    val selectedAutomationScenario: AutomationScenario? = null,
    val promptMode: PromptMode = PromptMode.PLAIN_TEXT
)

data class AutomationScenario(
    val name: String,
    val description: String,
    val prompt: String,
    val isActive: Boolean = false,
    val inputTokensCount: String = "0",
    val outputTokensCount: String = "0",
    val totalTokensCount: String = "0",
)

data class LogEntryState(
    val entrySources: List<EntrySource> = listOf(EntrySource.AGENT, EntrySource.TOOL, EntrySource.DEVICE),
    val logEntries: List<LogEntry> = emptyList(),
)

data class LogEntry(
    val timestamp: String,
    val entrySource: EntrySource,
    val message: String,
)

enum class EntrySource {
    DEVICE,
    TOOL,
    AGENT,
}

data class DeviceData(
    val name: String,
    val serial: String,
    val batteryLevel: Int = 0,
    val screenWidth: Int,
    val screenHeight: Int,
    val osVersion: String = "",
)

data class LLMData(
    val providerName: String,
    val modelName: String,
)

data class InstalledApp(
    val packageName: String,
)

sealed class ChipItem(val label: String) {
    data class Device(val deviceData: DeviceData) : ChipItem(label = deviceData.name)
    data class App(val installedApp: InstalledApp) : ChipItem(label = installedApp.packageName)
}

sealed class ExecutionState {
    data object Idle : ExecutionState()
    data object Executing : ExecutionState()
    data class Success(val message: String? = null) : ExecutionState()
    data class Error(val error: Throwable?) : ExecutionState()
}

enum class PromptMode {
    PLAIN_TEXT,
    MIND_SCRIPT
}
