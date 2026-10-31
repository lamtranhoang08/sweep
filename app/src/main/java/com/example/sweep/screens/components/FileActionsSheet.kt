package com.example.sweep.screens.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.sweep.data.FileItem
import com.example.sweep.util.formatBytes

/**
 * Bottom sheet listing the actions available for one file.
 *
 * The sheet only reports what the user chose through its callbacks. It
 * doesn't know how to share, rename or delete anything; the screen that
 * shows it decides. That keeps it reusable from Cleanup, Gallery and Search.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileActionsSheet(
    file: FileItem,
    onDismiss: () -> Unit,
    onShare: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            Text(
                text = file.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            Text(
                text = formatBytes(file.sizeBytes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            ActionRow(Icons.Default.Share, "Share", onShare)
            ActionRow(Icons.Default.Edit, "Rename", onRename)
            ActionRow(
                icon = Icons.Default.Delete,
                label = "Delete",
                onClick = onDelete,
                contentColor = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun ActionRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    ListItem(
        headlineContent = { Text(label, color = contentColor) },
        leadingContent = { Icon(icon, contentDescription = null, tint = contentColor) },
        // The sheet has its own background; without this the row would
        // paint a slightly different surface color over it.
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable(onClick = onClick)
    )
}