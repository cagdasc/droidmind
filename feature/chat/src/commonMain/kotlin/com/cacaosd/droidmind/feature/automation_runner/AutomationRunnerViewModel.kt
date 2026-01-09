package com.cacaosd.droidmind.feature.automation_runner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cacaosd.droidmind.domain.McpMessage
import com.cacaosd.droidmind.domain.session.ScenarioExecutor
import com.cacaosd.droidmind.feature.ChipItem
import com.cacaosd.droidmind.feature.DeviceData
import com.cacaosd.droidmind.feature.InstalledApp
import com.cacaosd.droidmind.feature.usecase.DevicePollUseCase
import com.cacaosd.droidmind.feature.usecase.InstalledAppsPollUseCase
import com.cacaosd.droidmind.mind.device.controller.DeviceController
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*

class AutomationRunnerViewModel(
    private val scenarioExecutor: ScenarioExecutor,
    private val mcpMessageFlow: MutableSharedFlow<McpMessage>,
    private val deviceController: DeviceController,
    private val devicePollUseCase: DevicePollUseCase,
    private val installedAppsPollUseCase: InstalledAppsPollUseCase
) : ViewModel() {
    private val _automationRunnerUiState = MutableStateFlow(AutomationRunnerUiState())
    val automationRunnerUiState: StateFlow<AutomationRunnerUiState> = _automationRunnerUiState

    private var installedAppsJob: Job? = null

    init {
        pollDevices()
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

    fun onAction(action: AutomationRunnerAction) {
        when (action) {
            is AutomationRunnerAction.AppSelected -> {
                _automationRunnerUiState.update { state ->
                    val deviceData =
                        state.chipItems.find { it is ChipItem.Device } ?: error("DeviceData should always be present.")
                    state.copy(
                        selectedApp = action.installedApp,
                        chipItems = setOf(deviceData, ChipItem.App(action.installedApp))
                    )
                }
            }

            is AutomationRunnerAction.DeviceSelected -> {
                pollForInstalledApp(action.deviceData.serial)
                _automationRunnerUiState.update { state ->
                    state.copy(
                        selectedDevice = action.deviceData,
                        chipItems = setOf(ChipItem.Device(action.deviceData))
                    )
                }
            }

            is AutomationRunnerAction.RemoveChip -> removeChipItem(action)
            is AutomationRunnerAction.RunScenarioClicked -> TODO()
            AutomationRunnerAction.StopScenarioClicked -> TODO()
            is AutomationRunnerAction.UpdateScenario -> TODO()
        }
    }
}