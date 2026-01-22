@file:OptIn(ExperimentalLayoutApi::class)

package com.cacaosd.droidmind.feature.automation_runner.composable

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cacaosd.uikit.theme.AppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun <T> ChipFlowRow(
    modifier: Modifier = Modifier,
    items: List<T>,
    onItemToggle: ((T) -> Unit),
    chipText: (T) -> String,
    maxLines: Int = Int.MAX_VALUE,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(AppTheme.sizes.medium),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(AppTheme.sizes.small),
    enabled: Boolean = true
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        maxLines = maxLines
    ) {
        items.forEach { item ->
            BasicChip(
                item = item,
                text = chipText(item),
                onToggle = onItemToggle,
                enabled = enabled
            )
        }
    }
}

@Composable
fun <T> BasicChip(
    item: T,
    text: String,
    onToggle: ((T) -> Unit),
    enabled: Boolean = true,
) {

    InputChip(
        enabled = enabled,
        selected = true,
        onClick = { onToggle(item) },
        label = {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                modifier = Modifier.size(AppTheme.sizes.large)
            )
        },
        shape = MaterialTheme.shapes.large,
        colors = InputChipDefaults.inputChipColors(
            containerColor = MaterialTheme.colorScheme.inverseSurface,
            labelColor = MaterialTheme.colorScheme.inverseOnSurface,
            trailingIconColor = MaterialTheme.colorScheme.inverseOnSurface,
            selectedContainerColor = MaterialTheme.colorScheme.inverseSurface,
            selectedLabelColor = MaterialTheme.colorScheme.inverseOnSurface,
            selectedTrailingIconColor = MaterialTheme.colorScheme.inverseOnSurface,
        )
    )
}

@Preview
@Composable
private fun TagsExample() {
    val tags = listOf("kotlin", "compose", "android", "mobile", "ui", "material", "development")
    var selectedTags by remember { mutableStateOf(setOf<String>()) }

    AppTheme {
        ChipFlowRow(
            items = tags,
            onItemToggle = { tag ->
                selectedTags = if (selectedTags.contains(tag)) {
                    selectedTags - tag
                } else {
                    selectedTags + tag
                }
            },
            chipText = { "#$it" },
            maxLines = 3,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}