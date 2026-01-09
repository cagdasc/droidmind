@file:OptIn(ExperimentalTime::class, ObsoleteCoroutinesApi::class)

package com.cacaosd.droidmind.feature

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cacaosd.droidmind.core.logging.Logger
import com.cacaosd.droidmind.domain.McpMessage
import com.cacaosd.droidmind.domain.session.ScenarioExecution
import com.cacaosd.droidmind.domain.session.ScenarioExecutor
import com.cacaosd.droidmind.domain.tools.DeviceControllerToolsConstant
import com.cacaosd.droidmind.domain.tools.TestCaseVerifierToolsConstant
import com.cacaosd.droidmind.mind.device.controller.DeviceController
import com.cacaosd.droidmind.mind.layout.model.OptimisedHierarchy
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
                        chipItems = chipItems
                    )
                }
            }
        }
    }

    private fun pollForConnectedDevices() {
        ticker(DEVICE_POLL_INTERVAL, 0L).receiveAsFlow()
            .onEach {
                val devices = withContext(Dispatchers.IO) { deviceController.getDevices() }
                _chatScreenUiState.update { state ->
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
            }
            .catch {
                Logger.error("Error while polling for connected devices", it)
            }.launchIn(viewModelScope)
    }

    private fun pollForInstalledApp(): Job {
        return ticker(INSTALLED_PACKAGES_POLL_INTERVAL, 0L).receiveAsFlow()
            .onEach {
                val serial = _chatScreenUiState.value.selectedDevice?.serial
                val listOfApps = withContext(Dispatchers.IO) { deviceController.listInstalledPackages(serial) }
                _chatScreenUiState.update { state ->
                    state.copy(installedApps = listOfApps.sortedDescending().map { InstalledApp(packageName = it) })
                }
            }.launchIn(viewModelScope)
    }

    private fun mapMcpMessageRequest(mcpMessageRequest: McpMessage.Request): MessageBubble.Request {
        return when (mcpMessageRequest) {
            is McpMessage.Request.User -> {
                MessageBubble.request(
                    sender = MessageOwner.User,
                    content = mcpMessageRequest.message
                )
            }

            is McpMessage.Request.Tool -> {
                MessageBubble.request(
                    sender = MessageOwner.Tool(toolName = mcpMessageRequest.toolName),
                    content = mcpMessageRequest.content
                )
            }
        }
    }

    private fun mapMcpMessageResponse(mcpMessageResponse: McpMessage.Response): MessageBubble.Response? {
        return when (mcpMessageResponse) {
            is McpMessage.Response.Assistant -> {
                MessageBubble.response(
                    sender = MessageOwner.Assistant,
                    content = mcpMessageResponse.content.trimIndent()
                )
            }

            is McpMessage.Response.AssistantWithError -> {
                MessageBubble.response(
                    sender = MessageOwner.Assistant,
                    content = "Error happened while executing the prompt",
                    throwable = mcpMessageResponse.throwable
                )
            }

            is McpMessage.Response.Metadata.Token -> {
                _chatScreenUiState.update { state ->
                    state.copy(
                        inputTokensCount = numberFormat.format(mcpMessageResponse.inputTokensCount),
                        outputTokensCount = numberFormat.format(mcpMessageResponse.outputTokensCount),
                        totalTokensCount = numberFormat.format(mcpMessageResponse.totalTokensCount)
                    )
                }
                null
            }

            is McpMessage.Response.ToolResult -> {
                handleToolResult(mcpMessageResponse)

                MessageBubble.response(
                    sender = MessageOwner.Assistant,
                    content = mcpMessageResponse.content.toString(),
                ).takeIf { mcpMessageResponse.toolName == TestCaseVerifierToolsConstant.VERIFY_UI_TEXT_TOOL_NAME }
            }
        }
    }

    private fun handleToolResult(mcpMessageResponse: McpMessage.Response.ToolResult) {
        if (mcpMessageResponse.toolName == DeviceControllerToolsConstant.UI_DUMP_TOOL) {
            val optimisedHierarchy = mcpMessageResponse.content as OptimisedHierarchy
            _chatScreenUiState.update { state ->
                state.copy(
                    rootUiElement = optimisedHierarchy.root
                )
            }
        }
    }

    private fun collectAgentEvent() {
        mcpMessageFlow
            .mapNotNull { mcpMessage ->
                when (mcpMessage) {
                    is McpMessage.Request -> mapMcpMessageRequest(mcpMessage)
                    is McpMessage.Response -> mapMcpMessageResponse(mcpMessage)
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
            state.copy(selectedDevice = deviceData, selectedApp = null, chipItems = setOf(ChipItem.Device(deviceData)))
        }

        installedAppsJob?.cancel()
        installedAppsJob = pollForInstalledApp()
    }

    private fun setSelectedApp(installedApp: InstalledApp) {
        _chatScreenUiState.update { state ->
            val deviceData =
                state.chipItems.find { it is ChipItem.Device } ?: error("DeviceData should always be present.")
            state.copy(selectedApp = installedApp, chipItems = setOf(deviceData, ChipItem.App(installedApp)))
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
                val scenarioExecution = ScenarioExecution.builder()
                    .deviceSerial(serial)
                    .packageName(packageName)
                    .scenario(userMessage)
                    .expectation("Once you done with the scenario, explain what you have done.")
                    .build()

                mcpMessageFlow.emit(McpMessage.Request.User(message = userMessage)).also {
                    scenarioExecutor.execute(request = scenarioExecution)
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
}
