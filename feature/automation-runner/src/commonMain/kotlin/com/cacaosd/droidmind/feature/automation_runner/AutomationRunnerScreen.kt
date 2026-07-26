package com.cacaosd.droidmind.feature.automation_runner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cacaosd.droidmind.domain.AgentClient
import com.cacaosd.droidmind.feature.automation_runner.composable.*
import com.cacaosd.uikit.theme.AppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.time.Instant
import java.util.*

@Composable
fun AutomationRunnerScreen(automationRunnerViewModel: AutomationRunnerViewModel) {
    val automationRunnerUiState by automationRunnerViewModel.automationRunnerUiState.collectAsState()
    InternalAutomationRunnerScreen(
        automationRunnerUiState = automationRunnerUiState,
        onAction = automationRunnerViewModel::onAction
    )
}

@Composable
private fun InternalAutomationRunnerScreen(
    automationRunnerUiState: AutomationRunnerUiState,
    onAction: (AutomationRunnerAction) -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        // Sidebar
        SidebarPanel(
            automationScenarios = automationRunnerUiState.automationScenarios,
            selectedAutomationScenario = automationRunnerUiState.selectedAutomationScenario,
            onAction = onAction
        )

        // Main content
        Column(modifier = Modifier.weight(1f)) {
            HeaderSection(automationRunnerUiState, onAction = onAction)

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = AppTheme.sizes.xxlarge)
                    .padding(bottom = AppTheme.sizes.xxlarge),
                horizontalArrangement = Arrangement.spacedBy(AppTheme.sizes.xxlarge)
            ) {
                ScenarioPanel(automationRunnerUiState, modifier = Modifier.weight(1f), onAction = onAction)

                DevicePreviewPanel(modifier = Modifier.width(300.dp))
            }

            ConsolePanel(
                modifier = Modifier.height(280.dp),
                logEntryState = automationRunnerUiState.logEntryState,
                onAction = onAction
            )
        }
    }
}

@Composable
private fun SidebarPanel(
    automationScenarios: List<AutomationScenario>,
    selectedAutomationScenario: AutomationScenario?,
    onAction: (AutomationRunnerAction) -> Unit
) {
    Surface(
        modifier = Modifier
            .width(288.dp)
            .fillMaxHeight(),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(AppTheme.sizes.large)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Scenario",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleLarge,
                )

                IconButton(onClick = { onAction(AutomationRunnerAction.AddScenarioClicked) }) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(AppTheme.sizes.xxlarge))

            // Scenarios list
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppTheme.sizes.medium)
            ) {
                items(automationScenarios, key = { it.id }) { scenario ->
                    ScenarioCard(
                        automationScenario = scenario,
                        isSelected = scenario.id == selectedAutomationScenario?.id,
                        onClick = { onAction(AutomationRunnerAction.ScenarioSelected(scenario)) },
                        onRemoveClicked = { onAction(AutomationRunnerAction.RemoveScenarioClicked(scenario)) },
                        onNameChanged = { newName -> onAction(AutomationRunnerAction.UpdateName(newName)) },
                        onShortDescriptionChanged = { newDesc ->
                            onAction(
                                AutomationRunnerAction.UpdateShortDescription(
                                    newDesc
                                )
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ScenarioCard(
    automationScenario: AutomationScenario,
    isSelected: Boolean,
    onClick: () -> Unit,
    onRemoveClicked: () -> Unit,
    onNameChanged: (String) -> Unit,
    onShortDescriptionChanged: (String) -> Unit,
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.secondaryContainer
        else -> Color.Transparent
    }

    Surface(
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .fillMaxWidth()
            .clickable { onClick() },
        color = backgroundColor,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(AppTheme.sizes.xmedium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                var nameState by remember { mutableStateOf(TextFieldValue(automationScenario.name)) }
                EditableTextField(
                    value = nameState,
                    onValueChange = { newValue ->
                        nameState = newValue
                        onNameChanged(newValue.text)
                    },
                    onClick = onClick,
                    textStyle = MaterialTheme.typography.titleMedium,
                    unfocusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    focusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    focusedBackgroundColor = MaterialTheme.colorScheme.onPrimary,
                    singleLine = true,
                    placeholder = "New Scenario"
                )
                Row {
                    if (isSelected) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(
                                text = "Active",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(
                                    horizontal = AppTheme.sizes.medium,
                                    vertical = AppTheme.sizes.xsmall
                                )
                            )
                        }
                    }
                    IconButton(onClick = { onRemoveClicked() }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Remove scenario",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(AppTheme.sizes.medium))

            var shortDescriptionState by remember { mutableStateOf(TextFieldValue(automationScenario.shortDescription)) }
            EditableTextField(
                value = shortDescriptionState,
                onValueChange = { newValue ->
                    shortDescriptionState = newValue
                    onShortDescriptionChanged(newValue.text)
                },
                onClick = onClick,
                textStyle = MaterialTheme.typography.bodyMedium,
                unfocusedTextColor = MaterialTheme.colorScheme.secondary,
                focusedTextColor = MaterialTheme.colorScheme.secondary,
                focusedBackgroundColor = MaterialTheme.colorScheme.onPrimary,
                singleLine = false,
                maxLines = 2,
                placeholder = "Short description"
            )
        }
    }
}

@Composable
private fun HeaderSection(
    automationRunnerUiState: AutomationRunnerUiState,
    onAction: (AutomationRunnerAction) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(AppTheme.sizes.xxlarge)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.sizes.large)
        ) {
            // Device selector
            Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.sizes.medium)) {
                SimpleDropdown(
                    items = automationRunnerUiState.deviceDataList,
                    selectedItem = automationRunnerUiState.selectedDevice,
                    onItemSelected = { device ->
                        onAction(AutomationRunnerAction.DeviceSelected(device))
                    },
                    label = "Device",
                    placeholder = "Select device",
                    item = { it.name },
                )

                if (automationRunnerUiState.installedApps.isNotEmpty()) {
                    SimpleDropdown(
                        items = automationRunnerUiState.installedApps,
                        selectedItem = automationRunnerUiState.selectedApp,
                        onItemSelected = { app ->
                            onAction(AutomationRunnerAction.AppSelected(app))
                        },
                        label = "App",
                        placeholder = "Select an app",
                        item = { it.packageName },
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Settings buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = { }) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = Color.Gray)
                }
                IconButton(onClick = { }) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(AppTheme.sizes.large))

        // Token counter
        val inputTokenCount = automationRunnerUiState.selectedAutomationScenario?.inputTokensCount ?: 0
        val outputTokenCount = automationRunnerUiState.selectedAutomationScenario?.outputTokensCount ?: 0
        val totalTokenCount = automationRunnerUiState.selectedAutomationScenario?.totalTokensCount ?: 0

        Column(verticalArrangement = Arrangement.spacedBy(AppTheme.sizes.medium)) {
            SectionedDropdown(
                sections = automationRunnerUiState.availableLLMs,
                selectedItem = automationRunnerUiState.selectedLLM,
                onItemSelected = { llm ->
                    onAction(AutomationRunnerAction.LLMSelected(llm))
                },
                label = "LLMs",
                placeholder = "Select a LLM Model",
                item = { "${it.providerName} - ${it.modelName}" },
            )
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(AppTheme.sizes.medium)
            ) {
                Text(
                    text = "Tokens | Input: $inputTokenCount· Output: $outputTokenCount · Total: $totalTokenCount",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = AppTheme.sizes.large, vertical = AppTheme.sizes.medium)
                )
            }
        }
    }
}

@Composable
private fun ScenarioPanel(
    automationRunnerUiState: AutomationRunnerUiState,
    modifier: Modifier = Modifier,
    onAction: (AutomationRunnerAction) -> Unit
) {
    Surface(
        modifier = modifier.fillMaxHeight(),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(AppTheme.sizes.large)
    ) {
        var promptState by remember(automationRunnerUiState.selectedAutomationScenario?.id) {
            val initialText = automationRunnerUiState.selectedAutomationScenario?.prompt.orEmpty()
            mutableStateOf(
                TextFieldValue(
                    text = initialText,
                    selection = TextRange(initialText.length)
                )
            )
        }

        LaunchedEffect(automationRunnerUiState.selectedAutomationScenario?.prompt) {
            val newPrompt = automationRunnerUiState.selectedAutomationScenario?.prompt.orEmpty()
            if (newPrompt != promptState.text) {
                val oldSelection = promptState.selection
                val clampedPos = minOf(oldSelection.start, newPrompt.length)
                promptState = promptState.copy(text = newPrompt, selection = TextRange(clampedPos))
            }
        }

        val enabled = remember(automationRunnerUiState.selectedAutomationScenario) {
            (automationRunnerUiState.selectedAutomationScenario != null
                    && automationRunnerUiState.executionState !is ExecutionState.Executing)
        }
        ScenarioTextField(
            value = promptState,
            onValueChange = { newValue ->
                promptState = newValue
                onAction(AutomationRunnerAction.UpdatePrompt(newValue.text))
            },
            promptMode = automationRunnerUiState.promptMode,
            onPromptModeChange = { onAction(AutomationRunnerAction.PromptModeChanged(it)) },
            enabled = enabled,
            chipItems = automationRunnerUiState.chipItems.toList(),
            onChipRemove = { onAction(AutomationRunnerAction.RemoveChip(it)) },
            onRun = {
                automationRunnerUiState.selectedAutomationScenario?.let {
                    onAction(AutomationRunnerAction.RunScenarioClicked(it))
                }
            }
        )
    }
}


@Composable
private fun DevicePreviewPanel(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxHeight(),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(AppTheme.sizes.large)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(AppTheme.sizes.xxxlarge),
                contentAlignment = Alignment.Center
            ) {
                DeviceFrame()
            }

            Text(
                text = "Device Preview",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
private fun DeviceFrame() {
    Box(
        modifier = Modifier
            .width(200.dp)
            .height(420.dp)
            .shadow(16.dp, RoundedCornerShape(32.dp))
            .background(Color(0xFF1F2937), RoundedCornerShape(32.dp))
            .border(4.dp, Color(0xFF374151), RoundedCornerShape(32.dp))
    ) {
        // Notch
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .width(96.dp)
                .height(20.dp)
                .background(Color.Black, RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
        )

        // Screen content
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            color = Color.White,
            shape = RoundedCornerShape(28.dp)
        ) {
            Column {
                // App bar
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            "MyApp",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = null,
                            tint = Color(0xFF6B7280),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }

                // Content
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Placeholder boxes
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(96.dp)
                            .background(
                                Color(0xFFF3F4F6),
                                RoundedCornerShape(8.dp)
                            )
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.66f)
                            .height(16.dp)
                            .background(
                                Color(0xFFF3F4F6),
                                RoundedCornerShape(4.dp)
                            )
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(16.dp)
                            .background(
                                Color(0xFFF3F4F6),
                                RoundedCornerShape(4.dp)
                            )
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Login form
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        placeholder = { Text("Email", fontSize = 14.sp) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        placeholder = { Text("Password", fontSize = 14.sp) },
                        modifier = Modifier.fillMaxWidth()
                    )

                }
            }
        }
    }
}

@Composable
private fun ConsolePanel(
    modifier: Modifier = Modifier,
    logEntryState: LogEntryState,
    onAction: (AutomationRunnerAction) -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        Column {
            // Console header
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AppTheme.sizes.xxxlarge),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppTheme.sizes.large),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.sizes.large)) {
                        logEntryState.entrySources.forEachIndexed { index, entrySource ->
                            ConsoleTab(
                                label = entrySource.name,
                                dotColor = colorForIndex(index = index)
                            )
                        }
                    }

                    IconButton(onClick = { onAction(AutomationRunnerAction.ClearLogs) }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.size(AppTheme.sizes.large)
                        )
                    }
                }
            }

            val listState = rememberLazyListState()
            LaunchedEffect(logEntryState.logEntries.size) {
                if (logEntryState.logEntries.isNotEmpty()) {
                    listState.animateScrollToItem(logEntryState.logEntries.lastIndex)
                }
            }

            // Console content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(AppTheme.sizes.medium),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(AppTheme.sizes.small)
            ) {
                items(logEntryState.logEntries) { entry ->
                    LogEntryRow(logEntryState.entrySources, entry)
                }
            }
        }
    }
}

@Composable
private fun ConsoleTab(label: String, dotColor: Color) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(AppTheme.sizes.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(AppTheme.sizes.medium)
                .clip(CircleShape)
                .background(dotColor)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondaryContainer
        )
    }
}

@Composable
private fun LogEntryRow(entrySources: List<EntrySource>, entry: LogEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppTheme.sizes.medium, vertical = AppTheme.sizes.xsmall),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.sizes.medium)
    ) {
        Text(
            text = entry.timestamp,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF6B7280)
        )
        Text(
            text = "[${entry.entrySource.name}]",
            style = MaterialTheme.typography.bodyMedium,
            color = colorForIndex(entrySources.indexOf(entry.entrySource))
        )

        CollapsibleLogText(
            text = entry.message,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF9CA3AF),
        )
    }
}

@Preview
@Composable
private fun AutomationRunnerScreenPreview() {
    AppTheme.colorSchemeProvider = colorSchemeProvider

    val automationScenarios = listOf(
        AutomationScenario(
            id = UUID.randomUUID(),
            name = "User Login Flow",
            shortDescription = "Automates the user login process including inputting credentials and handling 2FA.",
            prompt = "Automate the user login process including inputting credentials and handling 2FA.",
            timestamp = Instant.now(),
            inputTokensCount = "150",
            outputTokensCount = "75",
            totalTokensCount = "225",
        ),
        AutomationScenario(
            id = UUID.randomUUID(),
            name = "Data Extraction",
            shortDescription = "Extracts user data from the profile section of the app.",
            prompt = "Extract user data from the profile section of the app.",
            timestamp = Instant.now(),
            inputTokensCount = "120",
            outputTokensCount = "60",
            totalTokensCount = "180",
        )
    )

    val automationRunnerUiState = AutomationRunnerUiState(
        automationScenarios = automationScenarios,
        selectedAutomationScenario = automationScenarios.first(),
        logEntryState = LogEntryState(
            entrySources = listOf(EntrySource.DEVICE, EntrySource.AGENT),
            logEntries = listOf(
                LogEntry("00:10:16.102", EntrySource.AGENT, "Parsing scenario \"User Login Flow\"..."),
                LogEntry("00:10:16.450", EntrySource.AGENT, "Action: Tap(x=540, y=1860) executed."),
                LogEntry(
                    "00:10:17.200", EntrySource.DEVICE, "Input text "
                )
            ),
        ),
        deviceDataList = listOf(
            DeviceData(
                name = "Pixel 5",
                serial = "ABC123456",
                batteryLevel = 85,
                screenWidth = 1080,
                screenHeight = 2340,
                osVersion = "Android 12"
            ),
        ),
        installedApps = listOf(
            InstalledApp(packageName = "com.example.myapp"),
            InstalledApp(packageName = "com.example.anotherapp"),
        ),
        availableLLMs = listOf(
            DropdownSectionItem(
                header = "Remote",
                listOf(
                    LLMData(
                        providerName = "Google", modelName = "Gemini Pro",
                        modelType = ModelType.Remote,
                        agentClient = object : AgentClient {
                            override val modelProvider: String = "Google"
                            override val modelName: String = "Google Model"
                            override val modelType: com.cacaosd.droidmind.domain.ModelType =
                                com.cacaosd.droidmind.domain.ModelType.REMOTE

                            override suspend fun executePrompt(prompt: String) {}
                        }
                    ),
                    LLMData(
                        providerName = "Meta", modelName = "Llama 3",
                        modelType = ModelType.Remote,
                        agentClient = object : AgentClient {
                            override val modelProvider: String = "Google"
                            override val modelName: String = "Google Model"
                            override val modelType: com.cacaosd.droidmind.domain.ModelType =
                                com.cacaosd.droidmind.domain.ModelType.REMOTE

                            override suspend fun executePrompt(prompt: String) {}
                        }),
                    LLMData(
                        providerName = "Ollama",
                        modelName = "Llama 2",
                        modelType = ModelType.Remote,
                        agentClient = object : AgentClient {
                            override val modelProvider: String = "Google"
                            override val modelName: String = "Google Model"
                            override val modelType: com.cacaosd.droidmind.domain.ModelType =
                                com.cacaosd.droidmind.domain.ModelType.REMOTE

                            override suspend fun executePrompt(prompt: String) {}
                        }
                    ),
                )
            )

        ),
        selectedDevice = DeviceData(
            name = "Pixel 5",
            serial = "ABC123456",
            batteryLevel = 85,
            screenWidth = 1080,
            screenHeight = 2340,
            osVersion = "Android 12"
        ),
        selectedApp = InstalledApp(packageName = "com.example.myapp"),
        chipItems = setOf(
            ChipItem.Device(
                DeviceData(
                    name = "Pixel 5",
                    serial = "ABC123456",
                    batteryLevel = 85,
                    screenWidth = 1080,
                    screenHeight = 2340,
                    osVersion = "Android 12"
                )
            ),
            ChipItem.App(InstalledApp(packageName = "com.example.myapp")),
        )
    )
    AppTheme {
        InternalAutomationRunnerScreen(automationRunnerUiState = automationRunnerUiState, onAction = {})
    }
}
