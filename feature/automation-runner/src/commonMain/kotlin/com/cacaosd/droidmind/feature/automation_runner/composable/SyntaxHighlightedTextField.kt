package com.cacaosd.droidmind.feature.automation_runner.composable

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.cacaosd.droidmind.feature.automation_runner.*
import com.cacaosd.uikit.theme.AppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

val keywords = setOf("SCENARIO", "DEVICE", "APP", "DO", "EXPECT")
val actions = setOf(
    "tap", "input_text", "send_key_event", "launch_app",
    "device_screenshot", "scroll_down", "scroll_up", "scroll_left", "scroll_right"
)
val expectFunctions = setOf("VerifyText", "UiVisible")


@Composable
fun ScenarioTextField(
    automationRunnerUiState: AutomationRunnerUiState,
    onAction: (AutomationRunnerAction) -> Unit
) {
    val keywordColor = Color(0xFF21042B) // Purple for keywords
    val actionColor = Color(0xFF7D3C06) // Orange for actions
    val expectColor = Color(0xFF7878D9) // Blue for expect functions
    val normalColor = MaterialTheme.colorScheme.onPrimaryContainer

    // Convert TextStyle to SpanStyle
    val baseTextStyle = MaterialTheme.typography.bodyLarge
    val baseSpanStyle = SpanStyle(
        color = normalColor,
        fontSize = baseTextStyle.fontSize,
        fontWeight = baseTextStyle.fontWeight,
        fontStyle = baseTextStyle.fontStyle,
        fontFamily = baseTextStyle.fontFamily,
        letterSpacing = baseTextStyle.letterSpacing
    )

    fun buildHighlightedText(text: String) = buildAnnotatedString {
        val words = text.split(Regex("\\b"))

        words.forEach { word ->
            when {
                keywords.contains(word) -> {
                    withStyle(style = baseSpanStyle.copy(color = keywordColor, fontWeight = FontWeight.Bold)) {
                        append(word)
                    }
                }

                actions.contains(word) -> {
                    withStyle(style = baseSpanStyle.copy(color = actionColor, fontStyle = FontStyle.Italic)) {
                        append(word)
                    }
                }

                expectFunctions.contains(word) -> {
                    withStyle(style = baseSpanStyle.copy(color = expectColor)) {
                        append(word)
                    }
                }

                else -> {
                    withStyle(style = baseSpanStyle.copy(color = normalColor)) {
                        append(word)
                    }
                }
            }

        }
    }

    val visualTransformation = if (automationRunnerUiState.promptMode == PromptMode.MIND_SCRIPT) {
        VisualTransformation { text ->
            TransformedText(
                buildHighlightedText(text.text),
                OffsetMapping.Identity
            )
        }
    } else {
        VisualTransformation.None
    }

    val enabled = (automationRunnerUiState.selectedAutomationScenario != null
            && automationRunnerUiState.executionState !is ExecutionState.Executing)
    TextField(
        value = automationRunnerUiState.selectedAutomationScenario?.prompt.orEmpty(),
        onValueChange = {
            automationRunnerUiState.selectedAutomationScenario?.let { _ ->
                onAction(AutomationRunnerAction.UpdatePrompt(it))
            }
        },
        label = {
            Column(
                modifier = Modifier.padding(bottom = AppTheme.sizes.small),
                verticalArrangement = Arrangement.spacedBy(AppTheme.sizes.small)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Scenario", style = MaterialTheme.typography.titleLarge)

                    // Segmented Control for Editor Mode
                    SegmentedButton(
                        selectedMode = automationRunnerUiState.promptMode,
                        enabled = enabled,
                        onModeChange = { onAction(AutomationRunnerAction.PromptModeChanged(it)) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        if (automationRunnerUiState.chipItems.isNotEmpty()) {
                            ChipFlowRow(
                                items = automationRunnerUiState.chipItems.toList(),
                                onItemToggle = { item ->
                                    onAction(AutomationRunnerAction.RemoveChip(item))
                                },
                                chipText = { it.label },
                            )
                        }
                    }
                }
            }
        },
        trailingIcon = {
            Box(
                modifier = Modifier
                    .padding(bottom = AppTheme.sizes.small, end = AppTheme.sizes.medium)
                    .fillMaxHeight()
            ) {
                Button(
                    onClick = {
                        val selectedAutomationScenario = automationRunnerUiState.selectedAutomationScenario
                        selectedAutomationScenario?.let {
                            onAction(AutomationRunnerAction.RunScenarioClicked(it))
                        }
                    },
                    modifier = Modifier.align(Alignment.BottomEnd),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                        disabledContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = .5f),
                        disabledContentColor = MaterialTheme.colorScheme.onSecondary.copy(alpha = .5f)
                    ),
                    shape = MaterialTheme.shapes.medium,
                    enabled = enabled
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                }
            }
        },
        modifier = Modifier.fillMaxWidth(),
        textStyle = MaterialTheme.typography.bodyLarge,
        shape = MaterialTheme.shapes.small,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledIndicatorColor = Color.Transparent,
        ),
        enabled = enabled,
        visualTransformation = visualTransformation
    )
}

@Composable
private fun SegmentedButton(
    selectedMode: PromptMode,
    enabled: Boolean = true,
    onModeChange: (PromptMode) -> Unit
) {
    @Composable
    fun SegmentItem(label: String, targetMode: PromptMode) {
        val isSelected = selectedMode == targetMode
        OutlinedButton(
            onClick = { onModeChange(targetMode) },
            enabled = enabled,
            modifier = Modifier.height(AppTheme.sizes.xxxlarge),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (isSelected) MaterialTheme.colorScheme.secondary else Color.Transparent,
                contentColor = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
            ),
            contentPadding = PaddingValues(horizontal = AppTheme.sizes.xmedium, vertical = 0.dp)
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }

    Row(
        modifier = Modifier
            .height(AppTheme.sizes.xxxlarge)
            .padding(horizontal = AppTheme.sizes.small),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.sizes.small)
    ) {
        SegmentItem(label = "Plain Text", targetMode = PromptMode.PLAIN_TEXT)
        SegmentItem(label = "MindScript", targetMode = PromptMode.MIND_SCRIPT)
    }
}

@Preview
@Composable
fun PreviewSyntaxHighlightedTextField() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            ScenarioTextField(
                automationRunnerUiState = AutomationRunnerUiState(
                    selectedAutomationScenario = AutomationScenario(
                        name = "Sample Scenario",
                        description = "A sample automation scenario",
                        prompt = """
                            SCENARIO: Sample Scenario
                            DEVICE: MyDevice
                            APP: com.example.app
                            DO:
                                launch_app com.example.app
                                tap 100 200
                                input_text "Hello World"
                            EXPECT:
                                VerifyText "Welcome"
                        """.trimIndent()
                    ),
                    chipItems = emptySet(),
                    executionState = ExecutionState.Idle
                ),
                onAction = {}
            )
        }
    }
}