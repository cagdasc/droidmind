package com.cacaosd.droidmind.feature.automation_runner.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue

@Composable
fun EditableTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    textStyle: TextStyle,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    unfocusedTextColor: Color = Color.Unspecified,
    focusedTextColor: Color = Color.Unspecified,
    unfocusedBackgroundColor: Color = Color.Transparent,
    focusedBackgroundColor: Color = Color.Transparent,
    placeholder: String? = null,
    onClick: (() -> Unit)? = null
) {
    var isFocused by remember { mutableStateOf(false) }
    val backgroundColor = if (isFocused) focusedBackgroundColor else unfocusedBackgroundColor

    Box(
        modifier = modifier
            .background(backgroundColor)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            maxLines = maxLines,
            textStyle = textStyle.copy(
                color = if (isFocused && focusedTextColor != Color.Unspecified) focusedTextColor
                else if (!isFocused && unfocusedTextColor != Color.Unspecified) unfocusedTextColor
                else textStyle.color
            ),
            modifier = Modifier.onFocusChanged {
                isFocused = it.isFocused
                if (isFocused && onClick != null) onClick()
            },
            decorationBox = { innerTextField ->
                if (isFocused) {
                    innerTextField()
                } else {
                    Text(
                        text = if (value.text.isEmpty() && placeholder != null) placeholder else value.text,
                        style = textStyle,
                        color = if (value.text.isEmpty() && placeholder != null) Color.Gray else unfocusedTextColor
                    )
                }
            }
        )
    }
}
