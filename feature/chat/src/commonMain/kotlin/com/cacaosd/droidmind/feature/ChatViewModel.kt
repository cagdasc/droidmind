@file:OptIn(ExperimentalTime::class, ObsoleteCoroutinesApi::class)

package com.cacaosd.droidmind.feature

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cacaosd.droidmind.adb.device_controller.DeviceController
import com.cacaosd.droidmind.domain.McpMessage
import com.cacaosd.droidmind.domain.session.ScenarioExecutor
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.flow.*
import java.text.NumberFormat
import java.util.*
import kotlin.time.ExperimentalTime

private const val DEVICE_POLL_INTERVAL = 5000L
private const val INSTALLED_PACKAGES_POLL_INTERVAL = 20_000L

class ChatViewModel(
    private val scenarioExecutor: ScenarioExecutor,
    private val mcpMessageFlow: MutableSharedFlow<McpMessage>,
    private val deviceController: DeviceController
) : ViewModel() {
    private val _chatScreenUiState = MutableStateFlow(ChatScreenUiState())
    val chatScreenUiState: StateFlow<ChatScreenUiState> = _chatScreenUiState

    private var installedAppsJob: Job? = null
    private val numberFormat: NumberFormat = NumberFormat.getNumberInstance(Locale.UK)

    init {
        collectAgentEvent()
        pollForConnectedDevices()
    }

    fun onAction(action: ChatScreenAction) {
        when (action) {
            is ChatScreenAction.DeviceSelected -> setSelectedDevice(action.deviceData)
            is ChatScreenAction.PromptChanged -> updatePrompt(action.prompt)
            ChatScreenAction.RunScenarioClicked -> addUserMessage()
            is ChatScreenAction.AppSelected -> setSelectedApp(action.installedApp)
            is ChatScreenAction.RemoveChip -> removeChipItem(action)
            ChatScreenAction.StopScenarioClicked -> stopScenario()
        }
    }

    private fun removeChipItem(action: ChatScreenAction.RemoveChip) {
        _chatScreenUiState.update { state ->
            val chipItems = state.chipItems.toMutableSet().apply {
                remove(action.chipItem)
            }
            when (action.chipItem) {
                is DeviceData -> {
                    installedAppsJob?.cancel()
                    state.copy(
                        selectedDevice = null,
                        selectedApp = null,
                        chipItems = emptySet(),
                        installedApps = emptyList()
                    )
                }

                is InstalledApp -> {
                    state.copy(
                        selectedApp = null,
                        chipItems = chipItems
                    )
                }

                else -> {
                    state
                }
            }
        }
    }

    private fun pollForConnectedDevices() {
        ticker(DEVICE_POLL_INTERVAL, 0L).receiveAsFlow()
            .onEach {
                val devices = withContext(Dispatchers.IO) { deviceController.getDevices() }
                _chatScreenUiState.update { state ->
                    state.copy(deviceDataList = devices.map {
                        DeviceData(
                            name = it.name,
                            serial = it.serial,
                            batteryLevel = it.batteryLevel,
                            screenSize = it.dimensions,
                            osVersion = it.osVersion
                        )
                    })
                }
            }.launchIn(viewModelScope)
    }

    private fun pollForInstalledApp(): Job {
        return ticker(INSTALLED_PACKAGES_POLL_INTERVAL, 0L).receiveAsFlow()
            .onEach {
                val serial = _chatScreenUiState.value.selectedDevice?.serial
                val listOfApps = withContext(Dispatchers.IO) { deviceController.listInstalledPackages(serial) }
                _chatScreenUiState.update { state ->
                    state.copy(installedApps = listOfApps.sorted().map { InstalledApp(packageName = it) })
                }
            }.launchIn(viewModelScope)
    }

    private fun collectAgentEvent() {
        mcpMessageFlow
            .mapNotNull { event ->
                when (event) {
                    is McpMessage.Request.User -> MessageBubble.request(
                        sender = MessageOwner.User,
                        content = event.message
                    )

                    is McpMessage.Response.Assistant -> MessageBubble.response(
                        sender = MessageOwner.Assistant,
                        content = event.content.trimIndent()
                    )

                    is McpMessage.Request.Tool -> MessageBubble.request(
                        sender = MessageOwner.Tool(toolName = event.toolName),
                        content = event.content
                    )

                    is McpMessage.Response.AssistantWithError -> MessageBubble.response(
                        sender = MessageOwner.Assistant,
                        content = "Error happened while executing the prompt",
                        throwable = event.throwable
                    )

                    is McpMessage.Response.Metadata.Token -> {
                        _chatScreenUiState.update { state ->
                            state.copy(
                                inputTokensCount = numberFormat.format(event.inputTokensCount),
                                outputTokensCount = numberFormat.format(event.outputTokensCount),
                                totalTokensCount = numberFormat.format(event.totalTokensCount)
                            )
                        }
                        null
                    }

                    is McpMessage.Response.ToolResult -> {
                        MessageBubble.response(
                            sender = MessageOwner.Assistant,
                            content = event.content.orEmpty(),
                        ).takeIf { event.toolName == "verify_ui_text" }
                    }
                }
            }
            .onEach { message ->
                _chatScreenUiState.update { state ->
                    state.copy(
                        messages = listOf(message) + state.messages,
                        executionState = when (message.owner) {
                            is MessageOwner.Assistant -> {
                                message.asResponse()?.throwable?.let { ExecutionState.Error(it) }
                                    ?: ExecutionState.Success(message.content)
                            }

                            else -> ExecutionState.Executing
                        }
                    )
                }
            }.launchIn(viewModelScope)
    }

    private fun setSelectedDevice(deviceData: DeviceData) {
        _chatScreenUiState.update { state ->
            state.copy(selectedDevice = deviceData, selectedApp = null, chipItems = setOf(deviceData))
        }

        installedAppsJob?.cancel()
        installedAppsJob = pollForInstalledApp()
    }

    private fun setSelectedApp(installedApp: InstalledApp) {
        _chatScreenUiState.update { state ->
            val deviceData = state.chipItems.find { it is DeviceData } ?: error("DeviceData should always be present.")
            state.copy(selectedApp = installedApp, chipItems = setOf(deviceData, installedApp))
        }
    }

    private fun addUserMessage() {
        viewModelScope.launch(Dispatchers.Default) {
            _chatScreenUiState.update { state ->
                state.copy(executionState = ExecutionState.Executing)
            }

            with(chatScreenUiState.value) {
                val userMessage = prompt
                val serial = selectedDevice?.serial
                val packageName = selectedApp?.packageName

//                val scenario = if (serial == null || packageName == null) {
//                    mcpMessageFlow.emit(
//                        McpMessage.Response.Assistant(
//                            content = "The device or app is not selected. So the scenario will be run in raw mode.",
//                            finishReason = null
//                        )
//                    )
//                    RAW_TEST_SCENARIO_TEMPLATE.format(userMessage).trimIndent()
//                } else {
//                    EXPLICIT_TEST_SCENARIO_TEMPLATE.format(serial, packageName, userMessage).trimIndent()
//                } + RESULT_PREPARATION.trim()

                if (serial != null && packageName != null) {
                    val scenario = EXPLICIT_TEST_SCENARIO_TEMPLATE.format(serial, packageName, userMessage)
                        .trimIndent() + RESULT_PREPARATION.trim()
                    mcpMessageFlow.emit(McpMessage.Request.User(message = scenario)).also {
                        scenarioExecutor.execute(deviceSerial = serial, packageName = packageName, prompt = scenario)
                    }
                }
            }
        }
    }

    private fun updatePrompt(prompt: String) {
        _chatScreenUiState.update { state ->
            state.copy(prompt = prompt)
        }
    }

    private fun stopScenario() {
        viewModelScope.launch(Dispatchers.Default) {
            _chatScreenUiState.update { state ->
                state.copy(executionState = ExecutionState.Idle)
            }
        }
    }

    companion object {
        private const val EXPLICIT_TEST_SCENARIO_TEMPLATE = """
            Device serial is %s
            The application package name that will be launched is %s
            The scenario to run: %s
            """

        private const val RAW_TEST_SCENARIO_TEMPLATE = """
            The scenario to run: %s
            """

        private const val RESULT_PREPARATION = """
            Once you done with the scenario, please:
            Tell content-desc value of views that you clicked.
            {VIEW_CLASS} - {CONTENT_DESC} - {X_COORDINATE}x{Y_COORDINATE}
            """
    }
}
