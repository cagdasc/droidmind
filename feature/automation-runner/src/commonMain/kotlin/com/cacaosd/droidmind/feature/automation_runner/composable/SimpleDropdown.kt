@file:OptIn(ExperimentalMaterial3Api::class)

package com.cacaosd.droidmind.feature.automation_runner.composable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.cacaosd.uikit.theme.AppTheme

@Composable
internal fun <T> SimpleDropdown(
    modifier: Modifier = Modifier,
    items: List<T>,
    selectedItem: T?,
    onItemSelected: (T) -> Unit,
    label: String,
    placeholder: String,
    item: (T) -> String,
    enabled: Boolean = true,
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {
                if (enabled) {
                    expanded = !expanded
                }
            }
        ) {
            TextField(
                value = selectedItem?.let(item) ?: placeholder,
                onValueChange = {},
                readOnly = true,
                label = { Text(label) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                enabled = enabled,
                isError = isError,
                supportingText = supportingText,
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                items.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item(item)) },
                        onClick = {
                            onItemSelected(item)
                            expanded = false
                        },
                        enabled = enabled
                    )
                }
            }
        }
    }
}

@Composable
internal fun <T> SectionedDropdown(
    modifier: Modifier = Modifier,
    sections: List<DropdownSectionItem<T>>,
    selectedItem: T?,
    onItemSelected: (T) -> Unit,
    label: String,
    placeholder: String,
    item: (T) -> String,
    enabled: Boolean = true,
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {
                if (enabled) expanded = !expanded
            }
        ) {
            TextField(
                value = selectedItem?.let(item) ?: placeholder,
                onValueChange = {},
                readOnly = true,
                label = { Text(label) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                enabled = enabled,
                isError = isError,
                supportingText = supportingText,
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                sections.forEachIndexed { si, section ->
                    // Section header as a disabled item (styled)
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = section.header,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        onClick = { /* no-op */ },
                        enabled = false
                    )

                    // Items in this section
                    section.items.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item(item)) },
                            onClick = {
                                onItemSelected(item)
                                expanded = false
                            },
                            enabled = enabled
                        )
                    }

                    if (si != sections.lastIndex) {
                        // divider between sections
                        HorizontalDivider(modifier = Modifier.padding(vertical = AppTheme.sizes.xsmall))
                    }
                }
            }
        }
    }
}

data class DropdownSectionItem<T>(val header: String, val items: List<T>)
