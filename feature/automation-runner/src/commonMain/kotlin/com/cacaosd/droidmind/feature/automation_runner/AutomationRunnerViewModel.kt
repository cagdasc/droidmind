@file:OptIn(FlowPreview::class)

package com.cacaosd.droidmind.feature.automation_runner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cacaosd.droidmind.core.config.AppConfigManager
import com.cacaosd.droidmind.domain.AgentEvent
import com.cacaosd.droidmind.domain.local.scenario.ScenarioModel
import com.cacaosd.droidmind.domain.local.scenario.ScenarioRepository
import com.cacaosd.droidmind.domain.session.ExecutionMode
import com.cacaosd.droidmind.domain.session.ScenarioExecutionRequest
import com.cacaosd.droidmind.domain.session.ScenarioExecutor
import com.cacaosd.droidmind.feature.automation_runner.composable.DropdownSectionItem
import com.cacaosd.droidmind.feature.automation_runner.usecase.DevicePollUseCase
import com.cacaosd.droidmind.feature.automation_runner.usecase.GetAvailableLLMsUseCase
import com.cacaosd.droidmind.feature.automation_runner.usecase.InstalledAppsPollUseCase
import com.cacaosd.platform.coroutines.dispatchers.PlatformDispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant
import kotlin.time.toJavaInstant

class AutomationRunnerViewModel(
    private val scenarioExecutor: ScenarioExecutor,
    private val getAvailableLLMsUseCase: GetAvailableLLMsUseCase,
    private val agentEventFlow: MutableSharedFlow<AgentEvent>,
    private val devicePollUseCase: DevicePollUseCase,
    private val installedAppsPollUseCase: InstalledAppsPollUseCase,
    private val scenarioRepository: ScenarioRepository,
    private val platformDispatchers: PlatformDispatchers,
    private val appConfigManager: AppConfigManager
) : ViewModel() {
    private val _automationRunnerUiState = MutableStateFlow(AutomationRunnerUiState())
    val automationRunnerUiState: StateFlow<AutomationRunnerUiState> = _automationRunnerUiState

    private val scenarioUpdateFlow = MutableSharedFlow<ScenarioModel>()

    private var installedAppsJob: Job? = null
    private val numberFormat: NumberFormat = NumberFormat.getNumberInstance(Locale.UK)

    init {
        listenConfigChanges()
        loadSettings()
        pollDevices()
        loadAvailableLLMs()
        fetchScenarios()
        updateScenariosOnChange()
        collectAgentEvents()
    }

    private fun listenConfigChanges() {
        appConfigManager.settingsUpdatedFlow
            .onEach {
                loadSettings()
                loadAvailableLLMs()
            }
            .launchIn(viewModelScope)
    }

    private fun loadSettings() {
        val geminiApiKey = appConfigManager.getApiKey("gemini").orEmpty()
        val androidHome = appConfigManager.getEnvironmentVariable("ANDROID_HOME").orEmpty()
        _automationRunnerUiState.update { state ->
            state.copy(
                settingsDialogUiState = state.settingsDialogUiState.copy(
                    androidHome = androidHome,
                    geminiApiKey = geminiApiKey
                )
            )
        }
    }

    private fun pollDevices() {
        devicePollUseCase.pollDeviceState().onEach { devices ->
            _automationRunnerUiState.update { state ->
                state.copy(deviceDataList = devices.map { device ->
                    DeviceData(
                        name = device.name,
                        serial = device.serial,
                        batteryLevel = device.batteryLevel,
                        screenWidth = device.dimensions.width,
                        screenHeight = device.dimensions.height,
                        osVersion = device.osVersion
                    )
                })
            }
        }.launchIn(viewModelScope)
    }

    private fun pollForInstalledApp(deviceSerial: String) {
        installedAppsJob?.cancel()
        installedAppsJob = installedAppsPollUseCase.pollInstalledApps(deviceSerial).onEach { listOfApps ->
            _automationRunnerUiState.update { state ->
                state.copy(
                    installedApps = listOfApps.sorted().map { InstalledApp(packageName = it) })
            }
        }.launchIn(viewModelScope)
    }

    private fun loadAvailableLLMs() {
        viewModelScope.launch {
            val availableLLMs = getAvailableLLMsUseCase()
                .groupBy { it.modelType }
                .map { entry ->
                    DropdownSectionItem(
                        header = when (entry.key) {
                            ModelType.Local -> "Local Models"
                            ModelType.Remote -> "Remote Models"
                        },
                        items = entry.value,
                    )
                }
            _automationRunnerUiState.update { state ->
                state.copy(availableLLMs = availableLLMs, selectedLLM = null)
            }
        }
    }

    private fun fetchScenarios() {
        scenarioRepository.getScenarios().onEach { result ->
            result.onSuccess { scenarios ->
                val automationScenarios = scenarios.map { scenario ->
                    AutomationScenario(
                        id = scenario.id,
                        name = scenario.title,
                        shortDescription = scenario.shortDescription,
                        prompt = scenario.prompt,
                        timestamp = scenario.timestamp,
                    )
                }
                val selectedAutomationScenario = _automationRunnerUiState.value.selectedAutomationScenario
                val scenarioMap = automationScenarios.associateBy { it.id }
                val updatedSelectedScenario = scenarioMap[selectedAutomationScenario?.id]
                _automationRunnerUiState.update { state ->
                    state.copy(
                        automationScenarios = automationScenarios,
                        selectedAutomationScenario = updatedSelectedScenario
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun updateScenariosOnChange() {
        scenarioUpdateFlow.debounce(100.milliseconds)
            .onEach {
                scenarioRepository.saveScenario(
                    ScenarioModel(
                        id = it.id,
                        title = it.title,
                        shortDescription = it.shortDescription,
                        prompt = it.prompt,
                        timestamp = it.timestamp,
                    )
                )
            }
            .launchIn(viewModelScope)
    }

    private fun collectAgentEvents() {
        agentEventFlow.onEach { event ->
            when (event) {
                is AgentEvent.Started -> {
                    _automationRunnerUiState.update { state ->
                        val newLogEntryState = state.logEntryState.copy(
                            logEntries = state.logEntryState.logEntries + LogEntry(
                                timestamp = event.timestamp.toLocalTimeString(),
                                entrySource = EntrySource.AGENT,
                                message = "Agent execution started."
                            )
                        )
                        state.copy(
                            executionState = ExecutionState.Executing, logEntryState = newLogEntryState
                        )
                    }
                }

                is AgentEvent.Completed -> {
                    _automationRunnerUiState.update { state ->
                        val newLogEntryState = state.logEntryState.copy(
                            logEntries = state.logEntryState.logEntries + LogEntry(
                                timestamp = event.timestamp.toLocalTimeString(),
                                entrySource = EntrySource.AGENT,
                                message = "Agent execution completed."
                            )
                        )
                        state.copy(
                            executionState = ExecutionState.Idle, logEntryState = newLogEntryState
                        )
                    }
                }

                is AgentEvent.Failure -> {
                    _automationRunnerUiState.update { state ->
                        val newLogEntryState = state.logEntryState.copy(
                            logEntries = state.logEntryState.logEntries + LogEntry(
                                timestamp = event.timestamp.toLocalTimeString(),
                                entrySource = EntrySource.AGENT,
                                message = "Error: ${event.reason}"
                            )
                        )
                        state.copy(
                            executionState = ExecutionState.Error(event.throwable), logEntryState = newLogEntryState
                        )
                    }
                }

                is AgentEvent.Prompt -> {}

                is AgentEvent.Response.Assistant -> {
                    _automationRunnerUiState.update { state ->
                        val newLogEntryState = state.logEntryState.copy(
                            logEntries = state.logEntryState.logEntries + LogEntry(
                                timestamp = event.timestamp.toLocalTimeString(),
                                entrySource = EntrySource.AGENT,
                                message = event.content
                            )
                        )
                        state.copy(
                            logEntryState = newLogEntryState
                        )
                    }
                }

                is AgentEvent.Response.ToolCall -> {
                    _automationRunnerUiState.update { state ->
                        val newLogEntryState = state.logEntryState.copy(
                            logEntries = state.logEntryState.logEntries + LogEntry(
                                timestamp = event.timestamp.toLocalTimeString(),
                                entrySource = EntrySource.TOOL,
                                message = "Tool called: ${event.toolName} with content: ${event.content}"
                            )
                        )
                        state.copy(
                            logEntryState = newLogEntryState
                        )
                    }
                }

                is AgentEvent.Response.ToolResult -> {
                    _automationRunnerUiState.update { state ->
                        val newLogEntryState = state.logEntryState.copy(
                            logEntries = state.logEntryState.logEntries + LogEntry(
                                timestamp = event.timestamp.toLocalTimeString(),
                                entrySource = EntrySource.DEVICE,
                                message = "Tool result: ${event.toolName} with content: ${event.content}"
                            )
                        )
                        state.copy(
                            logEntryState = newLogEntryState
                        )
                    }
                }

                is AgentEvent.Token -> {
                    _automationRunnerUiState.update { state ->
                        val selectedScenario = state.selectedAutomationScenario?.let { scenario ->
                            val inputTokens = numberFormat.format(event.inputTokensCount)
                            val outputTokens = numberFormat.format(event.outputTokensCount)
                            val totalTokens = numberFormat.format(event.totalTokensCount)
                            scenario.copy(
                                inputTokensCount = inputTokens,
                                outputTokensCount = outputTokens,
                                totalTokensCount = totalTokens
                            )
                        }
                        state.copy(
                            selectedAutomationScenario = selectedScenario
                        )
                    }
                }
            }
        }.launchIn(viewModelScope)
    }

    fun onAction(action: AutomationRunnerAction) {
        when (action) {
            AutomationRunnerAction.AddScenarioClicked -> handleAddScenario()

            is AutomationRunnerAction.ScenarioSelected -> {
                _automationRunnerUiState.update { state ->
                    state.copy(selectedAutomationScenario = action.scenario)
                }
            }

            is AutomationRunnerAction.RemoveScenarioClicked -> handleRemoveScenario(action.scenario)
            is AutomationRunnerAction.AppSelected -> handleAppSelection(action.installedApp)
            is AutomationRunnerAction.DeviceSelected -> handleDeviceSelection(action.deviceData)

            is AutomationRunnerAction.RemoveChip -> removeChipItem(action.chipItem)

            is AutomationRunnerAction.RunScenarioClicked -> {
                clearLogs()
                runScenario(action.automationScenario.prompt)
            }

            AutomationRunnerAction.StopScenarioClicked -> {
                val currentState = _automationRunnerUiState.value
                val selectedLLM = currentState.selectedLLM ?: return
                viewModelScope.launch {
                    scenarioExecutor.cancel(agentClient = selectedLLM.agentClient)
                }
            }

            is AutomationRunnerAction.UpdatePrompt -> {
                val automationScenario = _automationRunnerUiState.value.selectedAutomationScenario
                automationScenario?.copy(prompt = action.prompt)?.let { updatedScenario ->
                    updateScenario(updatedScenario)
                }
            }

            is AutomationRunnerAction.UpdateName -> {
                val automationScenario = _automationRunnerUiState.value.selectedAutomationScenario
                automationScenario?.copy(name = action.name)?.let { updatedScenario ->
                    updateScenario(updatedScenario)
                }
            }

            is AutomationRunnerAction.UpdateShortDescription -> {
                val automationScenario = _automationRunnerUiState.value.selectedAutomationScenario
                automationScenario?.copy(shortDescription = action.shortDescription)?.let { updatedScenario ->
                    updateScenario(updatedScenario)
                }
            }

            AutomationRunnerAction.ClearLogs -> clearLogs()
            is AutomationRunnerAction.PromptModeChanged -> handlePromptModeChange(action.promptMode)
            is AutomationRunnerAction.LLMSelected -> handleLLMSelection(action)

            AutomationRunnerAction.SettingsDialogClicked -> handleSettingsDialogClicked()
            AutomationRunnerAction.SettingsDialogDismissed -> handleSettingsDialogDismissed()
            is AutomationRunnerAction.SaveSettings -> handleSaveSettings(action.androidHome, action.geminiApiKey)
        }
    }

    private fun handleRemoveScenario(scenario: AutomationScenario) {
        viewModelScope.launch {
            scenarioRepository.removeScenario(scenario.id)
                .onSuccess {
                    _automationRunnerUiState.update { state ->
                        val automationScenario = state.automationScenarios.firstOrNull()
                        state.copy(
                            selectedAutomationScenario = automationScenario
                        )
                    }
                }
        }
    }

    private fun handleLLMSelection(action: AutomationRunnerAction.LLMSelected) {
        _automationRunnerUiState.update { state ->
            state.copy(selectedLLM = action.llmData)
        }
    }

    private fun handlePromptModeChange(promptMode: PromptMode) {
        _automationRunnerUiState.update { it.copy(promptMode = promptMode) }
    }

    private fun updateScenario(updatedScenario: AutomationScenario?) {
        updatedScenario?.let {
            viewModelScope.launch {
                scenarioUpdateFlow.emit(
                    ScenarioModel(
                        id = it.id,
                        title = it.name,
                        shortDescription = it.shortDescription,
                        prompt = it.prompt,
                        timestamp = it.timestamp
                    )
                )
            }
        }

    }

    private fun handleAddScenario() {
        val newScenario = AutomationScenario(
            id = UUID.randomUUID(),
            name = "New Scenario",
            shortDescription = "Describe your scenario here.",
            prompt = "Describe your scenario here.",
            timestamp = java.time.Instant.now()
        )
        _automationRunnerUiState.update { state ->
            state.copy(
                automationScenarios = listOf(newScenario) + state.automationScenarios,
                selectedAutomationScenario = newScenario
            )
        }

        viewModelScope.launch {
            scenarioRepository.saveScenario(
                ScenarioModel(
                    id = newScenario.id,
                    title = newScenario.name,
                    shortDescription = newScenario.shortDescription,
                    prompt = newScenario.prompt,
                    timestamp = newScenario.timestamp
                )
            )
        }
    }

    private fun clearLogs() {
        _automationRunnerUiState.update { state ->
            state.copy(
                logEntryState = state.logEntryState.copy(
                    logEntries = emptyList()
                )
            )
        }
    }

    private fun runScenario(prompt: String) {
        val currentState = _automationRunnerUiState.value
        val deviceData = currentState.selectedDevice ?: return
        val installedApp = currentState.selectedApp ?: return
        val selectedLLM = currentState.selectedLLM ?: return

        viewModelScope.launch(platformDispatchers.default) {
            val scenarioExecutionRequest = ScenarioExecutionRequest.builder(
                deviceSerial = deviceData.serial,
                packageName = installedApp.packageName,
                scenario = prompt,
                executionMode = when (currentState.promptMode) {
                    PromptMode.PLAIN_TEXT -> ExecutionMode.TEXT
                    PromptMode.MIND_SCRIPT -> ExecutionMode.SCRIPT
                }
            )
                .build()

            scenarioExecutor.execute(
                agentClient = selectedLLM.agentClient,
                request = scenarioExecutionRequest,
            )
        }
    }

    private fun handleDeviceSelection(deviceData: DeviceData) {
        pollForInstalledApp(deviceData.serial)
        _automationRunnerUiState.update { state ->
            state.copy(
                selectedDevice = deviceData, chipItems = setOf(ChipItem.Device(deviceData))
            )
        }
    }

    private fun handleAppSelection(installedApp: InstalledApp) {
        _automationRunnerUiState.update { state ->
            val deviceData =
                state.chipItems.find { it is ChipItem.Device } ?: error("DeviceData should always be present.")
            state.copy(
                selectedApp = installedApp, chipItems = setOf(deviceData, ChipItem.App(installedApp))
            )
        }
    }

    private fun removeChipItem(chipItem: ChipItem) {
        _automationRunnerUiState.update { state ->
            when (chipItem) {
                is ChipItem.Device -> {
                    installedAppsJob?.cancel()
                    state.copy(
                        selectedDevice = null, selectedApp = null, chipItems = emptySet(), installedApps = emptyList()
                    )
                }

                is ChipItem.App -> {
                    state.copy(
                        selectedApp = null, chipItems = state.chipItems.toMutableSet().apply {
                            remove(chipItem)
                        })
                }
            }
        }
    }

    private fun handleSettingsDialogClicked() {
        val geminiApiKey = appConfigManager.getApiKey("gemini").orEmpty()
        val androidHome = appConfigManager.getEnvironmentVariable("ANDROID_HOME").orEmpty()
        _automationRunnerUiState.update { state ->
            state.copy(
                settingsDialogUiState = state.settingsDialogUiState.copy(
                    isOpen = true,
                    androidHome = androidHome,
                    geminiApiKey = geminiApiKey,
                    errorMessage = null,
                    successMessage = null
                )
            )
        }
    }

    private fun handleSettingsDialogDismissed() {
        _automationRunnerUiState.update { state ->
            state.copy(
                settingsDialogUiState = state.settingsDialogUiState.copy(
                    isOpen = false,
                    errorMessage = null,
                    successMessage = null
                )
            )
        }
    }

    private fun handleSaveSettings(androidHome: String, geminiApiKey: String) {
        viewModelScope.launch {
            _automationRunnerUiState.update { state ->
                state.copy(
                    settingsDialogUiState = state.settingsDialogUiState.copy(
                        isLoading = true,
                        errorMessage = null,
                        successMessage = null
                    )
                )
            }

            try {
                val apiKeySuccess = appConfigManager.saveApiKey("gemini", geminiApiKey)
                val envVarSuccess = appConfigManager.saveEnvironmentVariable("ANDROID_HOME", androidHome)

                if (apiKeySuccess || envVarSuccess) {
                    _automationRunnerUiState.update { state ->
                        state.copy(
                            settingsDialogUiState = state.settingsDialogUiState.copy(
                                isLoading = false,
                                successMessage = "Settings saved successfully",
                                isOpen = false
                            )
                        )
                    }
                } else {
                    _automationRunnerUiState.update { state ->
                        state.copy(
                            settingsDialogUiState = state.settingsDialogUiState.copy(
                                isLoading = false,
                                errorMessage = "Failed to save settings"
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                _automationRunnerUiState.update { state ->
                    state.copy(
                        settingsDialogUiState = state.settingsDialogUiState.copy(
                            isLoading = false,
                            errorMessage = "Error: ${e.message}"
                        )
                    )
                }
            }
        }
    }

    fun Instant.toLocalTimeString(zoneId: ZoneId = ZoneId.systemDefault()): String {
        val javaInstant = this.toJavaInstant()
        val formatter = DateTimeFormatter.ofPattern("hh:mm:ss").withZone(zoneId)
        return formatter.format(javaInstant)
    }
}