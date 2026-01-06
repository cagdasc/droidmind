package com.cacaosd.droidmind.feature

import androidx.compose.animation.core.*
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cacaosd.uikit.theme.AppTheme

// Color palette
object AppColors {
    val Primary = Color(0xFF136DEC)
    val BackgroundLight = Color(0xFFF6F7F8)
    val BackgroundDark = Color(0xFF101822)
    val SurfaceLight = Color(0xFFE0EBE4)
    val SurfaceDark = Color(0xFF1E2320)
    val LavenderLight = Color(0xFFE8DEF8)
    val LavenderDark = Color(0xFF362F3E)
    val SuccessLight = Color(0xFFD8E7D9)
    val SuccessDark = Color(0xFF2A382C)
}

data class Scenario(
    val title: String,
    val description: String,
    val isActive: Boolean = false
)

data class CodeLine(
    val lineNumber: String,
    val content: List<CodeToken>
)

data class CodeToken(
    val text: String,
    val color: Color
)

data class LogEntry(
    val timestamp: String,
    val tag: String,
    val tagColor: Color,
    val message: String,
    val isHighlighted: Boolean = false
)

@Preview
@Composable
fun PreviewAndroidAutomationPlanner() {
    AppTheme.colorSchemeProvider = colorSchemeProvider
    AppTheme {
        AndroidAutomationPlanner()
    }
}

@Composable
fun AndroidAutomationPlanner() {
    val isDarkTheme: Boolean = false
    val scenarios = remember {
        listOf(
            Scenario(
                "User Login Flow",
                "Test user authentication with valid credentials and verify dashboard load.",
                true
            ),
            Scenario("Checkout Process", "Add to cart to payment success."),
            Scenario("Settings Update", "Change language and theme.")
        )
    }

    val codeLines = remember {
        listOf(
            CodeLine(
                "01", listOf(
                    CodeToken("scenario", Color(0xFF9333EA)),
                    CodeToken(" ", Color.White),
                    CodeToken("\"Login Test\"", Color(0xFF2563EB)),
                    CodeToken(" {", Color.White)
                )
            ),
            CodeLine(
                "02", listOf(
                    CodeToken("    ", Color.White),
                    CodeToken("step", AppColors.Primary),
                    CodeToken(" ", Color.White),
                    CodeToken("\"Launch App\"", Color(0xFF16A34A))
                )
            ),
            CodeLine(
                "03", listOf(
                    CodeToken("    ", Color.White),
                    CodeToken("tap", AppColors.Primary),
                    CodeToken(" ", Color.White),
                    CodeToken("element_id", Color(0xFFEA580C)),
                    CodeToken("=", Color.White),
                    CodeToken("\"login_btn\"", Color(0xFF2563EB))
                )
            ),
            CodeLine(
                "04", listOf(
                    CodeToken("    ", Color.White),
                    CodeToken("input", AppColors.Primary),
                    CodeToken(" ", Color.White),
                    CodeToken("\"user@example.com\"", Color(0xFF2563EB)),
                    CodeToken(" ", Color.White),
                    CodeToken("into", AppColors.Primary),
                    CodeToken(" ", Color.White),
                    CodeToken("field_email", Color(0xFFEA580C))
                )
            ),
            CodeLine(
                "05", listOf(
                    CodeToken("    ", Color.White),
                    CodeToken("input", AppColors.Primary),
                    CodeToken(" ", Color.White),
                    CodeToken("\"password123\"", Color(0xFF2563EB)),
                    CodeToken(" ", Color.White),
                    CodeToken("into", AppColors.Primary),
                    CodeToken(" ", Color.White),
                    CodeToken("field_pass", Color(0xFFEA580C))
                )
            ),
            CodeLine(
                "06", listOf(
                    CodeToken("    ", Color.White),
                    CodeToken("tap", AppColors.Primary),
                    CodeToken(" ", Color.White),
                    CodeToken("submit_btn", Color(0xFFEA580C))
                )
            ),
            CodeLine(
                "07", listOf(
                    CodeToken("    ", Color.White),
                    CodeToken("expect", AppColors.Primary),
                    CodeToken(" ", Color.White),
                    CodeToken("screen", Color(0xFF9333EA)),
                    CodeToken(" ", Color.White),
                    CodeToken("contains", AppColors.Primary),
                    CodeToken(" ", Color.White),
                    CodeToken("\"Welcome\"", Color(0xFF2563EB))
                )
            ),
            CodeLine("08", listOf(CodeToken("}", Color.White)))
        )
    }

    val logEntries = remember {
        listOf(
            LogEntry("00:10:15.068", "[ADB]", Color(0xFF60A5FA), "Connected to device emulator-5554"),
            LogEntry("00:10:15.892", "[DroidMind]", Color(0xFF4ADE80), "Config directory: /home/user/config/droidmind"),
            LogEntry("00:10:16.102", "[Agent]", Color(0xFFA78BFA), "Parsing scenario \"User Login Flow\"..."),
            LogEntry("00:10:16.450", "[Agent]", Color(0xFFA78BFA), "Action: Tap(x=540, y=1860) executed.", true),
            LogEntry("00:10:17.200", "[ADB]", Color(0xFF60A5FA), "Input text \"user@example.com\" sent.")
        )
    }

    val backgroundColor = if (isDarkTheme) AppColors.BackgroundDark else AppColors.BackgroundLight
    val surfaceColor = if (isDarkTheme) AppColors.SurfaceDark else AppColors.SurfaceLight

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Sidebar
        SidebarPanel(scenarios)

        // Main content
        Column(modifier = Modifier.weight(1f)) {
            // Header
            HeaderSection(isDarkTheme, surfaceColor)

            // Content area
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 24.dp, vertical = 0.dp)
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Code editor
                CodeEditorPanel(codeLines, isDarkTheme, surfaceColor, modifier = Modifier.weight(1f))

                // Device preview
                DevicePreviewPanel(isDarkTheme, surfaceColor, modifier = Modifier.width(300.dp))
            }

            // Console
            ConsolePanel(logEntries, modifier = Modifier.height(192.dp))
        }
    }
}

@Composable
fun SidebarPanel(scenarios: List<Scenario>) {
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
            }

            Spacer(modifier = Modifier.height(AppTheme.sizes.xxlarge))

            // Scenarios list
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppTheme.sizes.medium)
            ) {
                items(scenarios) { scenario ->
                    ScenarioCard(scenario, false)
                }
            }

            Spacer(modifier = Modifier.height(AppTheme.sizes.large))

            // Run button
            Button(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AppTheme.sizes.x4large),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(AppTheme.sizes.medium))
                Text("Run Scenario", fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun ScenarioCard(scenario: Scenario, isDarkTheme: Boolean) {
    val backgroundColor = if (scenario.isActive) {
        if (isDarkTheme) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.5f)
    } else {
        Color.Transparent
    }

    Surface(
        modifier = Modifier
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
                    text = scenario.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                if (scenario.isActive) {
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
                text = scenario.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun HeaderSection(isDarkTheme: Boolean, surfaceColor: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Device selector
            Surface(
                modifier = Modifier.width(256.dp),
                color = if (isDarkTheme) AppColors.LavenderDark else AppColors.LavenderLight,
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp).padding(top = 4.dp)) {
                    Text(
                        text = "Device",
                        fontSize = 10.sp,
                        color = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pixel 7 Pro (API 33)",
                            fontSize = 14.sp,
                            color = if (isDarkTheme) Color(0xFFF3F4F6) else Color(0xFF1F2937)
                        )
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = if (isDarkTheme) Color(0xFF9CA3AF) else Color(0xFF6B7280)
                        )
                    }
                }
            }

            // Battery and controls
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    modifier = Modifier.height(40.dp),
                    color = if (isDarkTheme) AppColors.SuccessDark else AppColors.SuccessLight,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = if (isDarkTheme) Color(0xFFD8F3DC) else AppColors.Primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "100%",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isDarkTheme) Color(0xFFD8F3DC) else AppColors.Primary
                        )
                    }
                }

                IconButton(
                    onClick = { },
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            if (isDarkTheme) AppColors.SuccessDark else AppColors.SuccessLight,
                            RoundedCornerShape(8.dp)
                        )
                ) {
                    Icon(
                        Icons.Default.Phone,
                        contentDescription = null,
                        tint = if (isDarkTheme) Color(0xFFD8F3DC) else AppColors.Primary
                    )
                }

                IconButton(
                    onClick = { },
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            if (isDarkTheme) AppColors.SuccessDark else AppColors.SuccessLight,
                            RoundedCornerShape(8.dp)
                        )
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = null,
                        tint = if (isDarkTheme) Color(0xFFD8F3DC) else AppColors.Primary
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

        Spacer(modifier = Modifier.height(16.dp))

        // Token counter
        Surface(
            color = surfaceColor,
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(
                text = "Tokens | Input: 142 · Output: 45 · Total: 187",
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                color = if (isDarkTheme) Color(0xFF86EFAC) else AppColors.Primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
fun CodeEditorPanel(
    codeLines: List<CodeLine>,
    isDarkTheme: Boolean,
    surfaceColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxHeight(),
        color = surfaceColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            // Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Design", fontSize = 12.sp, color = Color.Gray)
                Text(
                    "Code",
                    fontSize = 12.sp,
                    color = AppColors.Primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.border(
                        width = 2.dp,
                        color = AppColors.Primary,
                        shape = RoundedCornerShape(0.dp)
                    ).padding(bottom = 2.dp)
                )
                Text("Assets", fontSize = 12.sp, color = Color.Gray)
            }

            Divider(color = Color.Gray.copy(alpha = 0.2f))

            // Code content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(codeLines) { line ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = line.lineNumber,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color.Gray
                        )
                        Row {
                            line.content.forEach { token ->
                                Text(
                                    text = token.text,
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isDarkTheme) token.color.copy(alpha = 0.9f) else token.color
                                )
                            }
                        }
                    }
                }
                item {
                    BlinkingCursor(isDarkTheme)
                }
            }
        }
    }
}

@Composable
fun BlinkingCursor(isDarkTheme: Boolean) {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .padding(start = 32.dp)
            .width(8.dp)
            .height(16.dp)
            .background(AppColors.Primary.copy(alpha = alpha))
    )
}

@Composable
fun DevicePreviewPanel(isDarkTheme: Boolean, surfaceColor: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxHeight(),
        color = surfaceColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                DeviceFrame(isDarkTheme)
            }

            Text(
                text = "Device Preview",
                fontSize = 14.sp,
                color = AppColors.Primary.copy(alpha = 0.6f),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
fun DeviceFrame(isDarkTheme: Boolean) {
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
            color = if (isDarkTheme) Color(0xFF111827) else Color.White,
            shape = RoundedCornerShape(28.dp)
        ) {
            Column {
                // App bar
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    color = AppColors.Primary.copy(alpha = 0.1f)
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
                            color = if (isDarkTheme) Color.White else Color(0xFF1F2937),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = null,
                            tint = if (isDarkTheme) Color(0xFFD1D5DB) else Color(0xFF6B7280),
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
                                if (isDarkTheme) Color(0xFF1F2937) else Color(0xFFF3F4F6),
                                RoundedCornerShape(8.dp)
                            )
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.66f)
                            .height(16.dp)
                            .background(
                                if (isDarkTheme) Color(0xFF1F2937) else Color(0xFFF3F4F6),
                                RoundedCornerShape(4.dp)
                            )
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(16.dp)
                            .background(
                                if (isDarkTheme) Color(0xFF1F2937) else Color(0xFFF3F4F6),
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
                    Button(
                        onClick = { },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary)
                    ) {
                        Text("Login")
                    }
                }
            }
        }
    }
}

@Composable
fun ConsolePanel(logEntries: List<LogEntry>, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color(0xFF1E1E1E)
    ) {
        Column {
            // Console header
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
                color = Color(0xFF2D2D2D)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        ConsoleTab("ADB Logs", Color(0xFF22C55E), true)
                        ConsoleTab("Network", Color(0xFF3B82F6), false)
                        ConsoleTab("Error", Color(0xFFEF4444), false)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Console content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(logEntries) { entry ->
                    LogEntryRow(entry)
                }
            }
        }
    }
}

@Composable
fun ConsoleTab(label: String, dotColor: Color, isActive: Boolean) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { }
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = if (isActive) Color.White else Color.Gray
        )
    }
}

@Composable
fun LogEntryRow(entry: LogEntry) {
    val backgroundColor = if (entry.isHighlighted) Color.White.copy(alpha = 0.05f) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = entry.timestamp,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF6B7280)
        )
        Text(
            text = entry.tag,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = entry.tagColor
        )
        Text(
            text = entry.message,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = if (entry.isHighlighted) Color(0xFFD1D5DB) else Color(0xFF9CA3AF)
        )
    }
}