package com.example.sweep.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sweep.FileRepositoryImpl
import com.example.sweep.FileSystemDataSource
import com.example.sweep.MediaStoreDataSource
import com.example.sweep.StorageCategory
import java.lang.String.format
import java.util.Locale

@Composable
fun StorageScreen(
    // TODO: Both StorageScreen and BrowseScreen currently construct their own
    //  separate FileRepositoryImpl + data source instances. This works but
    //  creates duplicate objects across screens. Introduce a shared AppContainer
    //  (simple manual DI holder) so every screen gets the same repository instance.
    viewModel: StorageViewModel = viewModel(
        factory = StorageViewModel.Factory(
            FileRepositoryImpl(
                mediaStoreDataSource = MediaStoreDataSource(
                    LocalContext.current.applicationContext
                ),
                fileSystemDataSource = FileSystemDataSource(LocalContext.current.applicationContext)
            )
        )
    )
) {
    val summary by viewModel.storageSummary.collectAsStateWithLifecycle()
    val totalBytes = summary.values.sum()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Overall Storage Heaer Card
        Surface(
            tonalElevation = 2.dp,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Total Storage Used",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatBytes(totalBytes),
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Categories",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        StorageCategory.entries.forEach { category ->
            val bytes = summary[category] ?: 0L
            val percentage = if (totalBytes > 0) bytes.toFloat() / totalBytes else 0f

            CategoryRow(
                category = category,
                bytes = bytes,
                percentage = percentage
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        }
    }
}

@Composable
private fun CategoryRow(
    category: StorageCategory,
    bytes: Long,
    percentage: Float
) {
    val categoryColor = getCategoryColor(category)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(categoryColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = category.name.lowercase().replaceFirstChar { it.titlecase() },
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = formatBytes(bytes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        LinearProgressIndicator(
            progress = { percentage },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = categoryColor,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    }
}

/**
 * Maps each [StorageCategory] to a distinct Material theme visual indicator color.
 */
// TODO: category colors are currently hardcoded hex values. Revisit using
//  theme-aware colors (MaterialTheme.colorScheme) so this adapts properly
//  to dark mode / Material You dynamic color later.
@Composable
private fun getCategoryColor(category: StorageCategory): Color {
    return when (category) {
        StorageCategory.IMAGE -> Color(0xFF4CAF50) // Green
        StorageCategory.VIDEO -> Color(0xFF2196F3) // Blue
        StorageCategory.AUDIO -> Color(0xFFFF9800) // Orange
        StorageCategory.OTHER -> Color(0xFF9C27B0) // Purple
    }
}

/**
 * Converts a raw byte count into a formatted human-readable string (e.g. "2.3 MB").
 */
// TODO: formatBytes() will likely be needed again once Browse/Gallery show
//  file sizes too. Extract this into a shared util file (e.g. FormatUtils.kt)
//  instead of duplicating it per screen.
private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var unitIndex = 0

    while (value >= 1024 && unitIndex < units.size - 1) {
        value /= 1024.0
        unitIndex++
    }

    return format(Locale.getDefault(), "%.2f %s", value, units[unitIndex])
}