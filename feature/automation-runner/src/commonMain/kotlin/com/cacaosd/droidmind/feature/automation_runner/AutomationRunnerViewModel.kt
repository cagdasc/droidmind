package com.cacaosd.droidmind.feature.automation_runner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cacaosd.droidmind.domain.AgentEvent
import com.cacaosd.droidmind.domain.session.ScenarioExecutionRequest
import com.cacaosd.droidmind.domain.session.ScenarioExecutor
import com.cacaosd.droidmind.feature.automation_runner.usecase.DevicePollUseCase
import com.cacaosd.droidmind.feature.automation_runner.usecase.InstalledAppsPollUseCase
import com.cacaosd.droidmind.mind.device.controller.DeviceController
import com.cacaosd.platform.coroutines.dispatchers.PlatformDispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.text.NumberFormat
import java.util.*

class AutomationRunnerViewModel(
    private val scenarioExecutor: ScenarioExecutor,
    private val agentEventFlow: MutableSharedFlow<AgentEvent>,
    private val deviceController: DeviceController,
    private val devicePollUseCase: DevicePollUseCase,
    private val installedAppsPollUseCase: InstalledAppsPollUseCase,
    private val platformDispatchers: PlatformDispatchers
) : ViewModel() {
    private val _automationRunnerUiState = MutableStateFlow(AutomationRunnerUiState())
    val automationRunnerUiState: StateFlow<AutomationRunnerUiState> = _automationRunnerUiState

    private var installedAppsJob: Job? = null
    private val numberFormat: NumberFormat = NumberFormat.getNumberInstance(Locale.UK)

    init {
        pollDevices()
        collectAgentEvents()
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
        installedAppsJob = installedAppsPollUseCase.pollInstalledApps(deviceSerial)
            .onEach { listOfApps ->
                _automationRunnerUiState.update { state ->
                    state.copy(installedApps = listOfApps.sorted().map { InstalledApp(packageName = it) })
                }
            }.launchIn(viewModelScope)
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
                            executionState = ExecutionState.Executing,
                            logEntryState = newLogEntryState
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
                            executionState = ExecutionState.Idle,
                            logEntryState = newLogEntryState
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
                            executionState = ExecutionState.Error(event.throwable),
                            logEntryState = newLogEntryState
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
            AutomationRunnerAction.AddScenarioClicked -> {
                handleAddScenario()
            }

            is AutomationRunnerAction.AppSelected -> {
                handleAppSelection(action)
            }

            is AutomationRunnerAction.DeviceSelected -> {
                handleDeviceSelection(action)
            }

            is AutomationRunnerAction.RemoveChip -> {
                removeChipItem(action)
            }

            is AutomationRunnerAction.RunScenarioClicked -> {
                clearLogs()
                runScenario(action.automationScenario.prompt)
            }

            AutomationRunnerAction.StopScenarioClicked -> TODO()
            is AutomationRunnerAction.UpdatePrompt -> {
                updatePrompt(action)
            }

            AutomationRunnerAction.ClearLogs -> {
                clearLogs()
            }
        }
    }

    private fun updatePrompt(action: AutomationRunnerAction.UpdatePrompt) {
        _automationRunnerUiState.update { state ->
            val updatedScenario = state.selectedAutomationScenario?.copy(prompt = action.prompt)
            val updatedScenarios = state.automationScenarios.map {
                if (it.name == updatedScenario?.name) {
                    updatedScenario
                } else {
                    it
                }
            }

            state.copy(
                automationScenarios = updatedScenarios,
                selectedAutomationScenario = updatedScenario
            )
        }
    }

    private fun handleAddScenario() {
        val newScenario = AutomationScenario(
            name = "New Scenario",
            description = "Describe your scenario here.",
            prompt = "Describe your scenario here.",
            isActive = true
        )
        _automationRunnerUiState.update { state ->
            val automationScenarios = state.automationScenarios.map { it.copy(isActive = false) }
            state.copy(
                automationScenarios = listOf(newScenario) + automationScenarios,
                selectedAutomationScenario = newScenario
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

        viewModelScope.launch(platformDispatchers.default) {
            val scenarioExecutionRequest = ScenarioExecutionRequest.builder()
                .deviceSerial(deviceData.serial)
                .packageName(installedApp.packageName)
                .scenario(prompt)
                .expectation("Once you done with the scenario, explain what you have done.")
                .build()
            scenarioExecutor.execute(
                request = scenarioExecutionRequest,
            )
        }
    }

    private fun handleDeviceSelection(action: AutomationRunnerAction.DeviceSelected) {
        pollForInstalledApp(action.deviceData.serial)
        _automationRunnerUiState.update { state ->
            state.copy(
                selectedDevice = action.deviceData,
                chipItems = setOf(ChipItem.Device(action.deviceData))
            )
        }
    }

    private fun handleAppSelection(action: AutomationRunnerAction.AppSelected) {
        _automationRunnerUiState.update { state ->
            val deviceData =
                state.chipItems.find { it is ChipItem.Device } ?: error("DeviceData should always be present.")
            state.copy(
                selectedApp = action.installedApp,
                chipItems = setOf(deviceData, ChipItem.App(action.installedApp))
            )
        }
    }

    private fun removeChipItem(action: AutomationRunnerAction.RemoveChip) {
        _automationRunnerUiState.update { state ->
            when (action.chipItem) {
                is ChipItem.Device -> {
                    installedAppsJob?.cancel()
                    state.copy(
                        selectedDevice = null,
                        selectedApp = null,
                        chipItems = emptySet(),
                        installedApps = emptyList()
                    )
                }

                is ChipItem.App -> {
                    state.copy(
                        selectedApp = null,
                        chipItems = state.chipItems.toMutableSet().apply {
                            remove(action.chipItem)
                        }
                    )
                }
            }
        }
    }

    fun Instant.toLocalTimeString(timeZone: TimeZone = TimeZone.currentSystemDefault()): String {
        val time = toLocalDateTime(timeZone).time
        return "%02d:%02d:%02d".format(
            time.hour,
            time.minute,
            time.second
        )
    }
}