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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cacaosd.droidmind.feature.automation_runner.composable.*
import com.cacaosd.uikit.theme.AppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

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
                items(automationScenarios) { scenario ->
                    ScenarioCard(scenario)
                }
            }
        }
    }
}

@Composable
private fun ScenarioCard(automationScenario: AutomationScenario) {
    val backgroundColor = if (automationScenario.isActive) {
        Color.White.copy(alpha = 0.5f)
    } else {
        Color.Transparent
    }

    Surface(
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .fillMaxWidth()
            .clickable { },
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
                Text(
                    text = automationScenario.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                if (automationScenario.isActive) {
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
            }
            Spacer(modifier = Modifier.height(AppTheme.sizes.medium))
            Text(
                text = automationScenario.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
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
                GenericDropdown(
                    items = automationRunnerUiState.deviceDataList,
                    selectedItem = automationRunnerUiState.selectedDevice,
                    onItemSelected = { device ->
                        onAction(AutomationRunnerAction.DeviceSelected(device))
                    },
                    label = "Device",
                    placeholder = "Select device",
                    itemText = { it.name },
                )

                if (automationRunnerUiState.installedApps.isNotEmpty()) {
                    GenericDropdown(
                        items = automationRunnerUiState.installedApps,
                        selectedItem = automationRunnerUiState.selectedApp,
                        onItemSelected = { app ->
                            onAction(AutomationRunnerAction.AppSelected(app))
                        },
                        label = "App",
                        placeholder = "Select an app",
                        itemText = { it.packageName },
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
        ScenarioTextField(automationRunnerUiState = automationRunnerUiState, onAction = onAction)
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
            name = "User Login Flow",
            description = "Automates the user login process including inputting credentials and handling 2FA.",
            isActive = true,
            prompt = "Automate the user login process including inputting credentials and handling 2FA.",
            inputTokensCount = "150",
            outputTokensCount = "75",
            totalTokensCount = "225",

            ),
        AutomationScenario(
            name = "Data Extraction",
            description = "Extracts user data from the profile section of the app.",
            isActive = false,
            prompt = "Extract user data from the profile section of the app.",
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
