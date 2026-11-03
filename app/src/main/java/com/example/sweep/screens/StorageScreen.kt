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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sweep.LocalAppContainer
import com.example.sweep.data.FileHasher
import com.example.sweep.data.FileRepositoryImpl
import com.example.sweep.data.FileSystemDataSource
import com.example.sweep.data.MediaStoreDataSource
import com.example.sweep.data.StorageCategory
import com.example.sweep.util.formatBytes
import java.lang.String.format
import java.util.Locale

@Composable
fun StorageScreen(
    viewModel: StorageViewModel = viewModel(
        factory = StorageViewModel.Factory(
            LocalAppContainer.current.repository)
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
            .verticalScroll(rememberScrollState())
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
 * Colour for each storage category's bar and dot.
 *
 * These are deliberately fixed colours instead of theme roles. Each category
 * has a lighter tone for dark surfaces and a deeper one for light surfaces, so every bar
 * keeps enough contrast with its backgroumd
 */
@Composable
private fun getCategoryColor(category: StorageCategory): Color {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    return when (category) {
        StorageCategory.IMAGE -> if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32) // green
        StorageCategory.VIDEO -> if (isDark) Color(0xFF64B5F6) else Color(0xFF1565C0) // blue
        StorageCategory.AUDIO -> if (isDark) Color(0xFFFFB74D) else Color(0xFFE65100) // orange
        StorageCategory.OTHER -> if (isDark) Color(0xFFBA68C8) else Color(0xFF6A1B9A) // purple
    }
}
