package com.example.sweep.screens.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import com.example.sweep.data.FileItem
import com.example.sweep.util.splitFileName

/**
 * Dialog for renaming file
 *
 * The user edits only the base name; the extension is shown as a fixed
 * suffix so it can't be removed by accident. The base name starts fully
 * selected, so typing replaces it. The dialog validates the input and only
 * calls onConfirm with a name that is valid and different from the current one.
 *
 * @param onConfirm Called with the full new name, extension included.
 */
@Composable
fun RenameDialog(
    file: FileItem,
    onDismiss: () -> Unit,
    onConfirm: (newName: String) -> Unit
) {
    val parts = remember(file.name) { splitFileName(file.name) }
    var text by remember {
        mutableStateOf(TextFieldValue(parts.base, selection = TextRange(0, parts.base.length)))
    }
    var error by remember { mutableStateOf<String?>(null) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    fun submit() {
        val base = text.text.trim()
        val problem = when {
            base.isEmpty() -> "Name can't be empty"
            base.any { it == '/' || it == '\\' } -> "Name can't contain / or \\"
            else -> null
        }
        if (problem != null) {
            error = problem
            return
        }

        val newName = base + parts.extension
        if (newName == file.name) onDismiss() else onConfirm(newName)
    }

    val currentError = error

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = {
                    text = it
                    error = null
                },
                singleLine = true,
                isError = currentError != null,
                supportingText = { currentError?.let { Text(it) } },
                suffix = { Text(parts.extension) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { submit() }),
                modifier = Modifier.focusRequester(focusRequester)
            )
        },
        confirmButton = { TextButton(onClick = { submit() }) { Text("Rename") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}