package com.cacaosd.droidmind.feature.automation_runner.composable

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.cacaosd.droidmind.feature.automation_runner.AutomationRunnerAction
import com.cacaosd.droidmind.feature.automation_runner.SettingsDialogUiState
import com.cacaosd.uikit.theme.AppTheme

@Composable
fun SettingsDialog(
    settingsDialogUiState: SettingsDialogUiState,
    onSave: (AutomationRunnerAction.SaveSettings) -> Unit,
    onDismiss: () -> Unit
) {
    if (!settingsDialogUiState.isOpen) return

    var androidHomeState by remember(settingsDialogUiState) { mutableStateOf(settingsDialogUiState.androidHome) }
    var geminiApiKeyState by remember(settingsDialogUiState) { mutableStateOf(settingsDialogUiState.geminiApiKey) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Settings", style = MaterialTheme.typography.headlineSmall)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AppTheme.sizes.large)
            ) {
                // Android Home TextField
                OutlinedTextField(
                    value = androidHomeState,
                    onValueChange = { androidHomeState = it },
                    label = { Text("ANDROID_HOME") },
                    placeholder = { Text("/path/to/android-sdk") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !settingsDialogUiState.isLoading,
                    singleLine = true
                )

                // Gemini API Key TextField
                OutlinedTextField(
                    value = geminiApiKeyState,
                    onValueChange = { geminiApiKeyState = it },
                    label = { Text("Gemini API Key") },
                    placeholder = { Text("Enter your API key") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !settingsDialogUiState.isLoading,
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )

                // Error message
                if (settingsDialogUiState.errorMessage != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = settingsDialogUiState.errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(AppTheme.sizes.medium)
                        )
                    }
                }

                // Success message
                if (settingsDialogUiState.successMessage != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = settingsDialogUiState.successMessage,
                            color = MaterialTheme.colorScheme.tertiary,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(AppTheme.sizes.medium)
                        )
                    }
                }

                // Loading indicator
                if (settingsDialogUiState.isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        AutomationRunnerAction.SaveSettings(
                            androidHome = androidHomeState,
                            geminiApiKey = geminiApiKeyState
                        )
                    )
                },
                enabled = !settingsDialogUiState.isLoading
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !settingsDialogUiState.isLoading
            ) {
                Text("Cancel")
            }
        },
        modifier = Modifier.widthIn(min = 400.dp, max = 600.dp)
    )
}
