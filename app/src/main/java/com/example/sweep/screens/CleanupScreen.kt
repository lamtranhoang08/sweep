package com.example.sweep.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import com.example.sweep.data.DuplicateGroup
import com.example.sweep.data.FileHasher
import com.example.sweep.data.FileItem
import com.example.sweep.data.FileRepositoryImpl
import com.example.sweep.data.FileSystemDataSource
import com.example.sweep.data.MediaStoreDataSource
import com.example.sweep.data.StorageCategory
import com.example.sweep.screens.components.FileActionsSheet
import com.example.sweep.screens.components.MediaRow
import com.example.sweep.util.formatBytes

/**
 * Cleanup screen with two views: the largest files, and groups of
 * duplicate files. View-only for now; deleting comes with file actions.
 */
@Composable
fun CleanupScreen() {
    val context = LocalContext.current

    // TODO: Same repository duplication as the other screens; replace with a
    //  shared AppContainer. The ImageLoader below is also built separately in
    //  GalleryScreen and could be one app-wide instance.
    val repository = remember {
        FileRepositoryImpl(
            mediaStoreDataSource = MediaStoreDataSource(context.applicationContext),
            fileSystemDataSource = FileSystemDataSource(context.applicationContext),
            fileHasher = FileHasher(context.applicationContext)
        )
    }

    val viewModel: CleanupViewModel = viewModel(
        factory = CleanupViewModel.Factory(repository)
    )

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components { add(VideoFrameDecoder.Factory()) }
            .build()
    }

    var selectedTab by rememberSaveable { mutableStateOf(0) }
    var selectedFile by remember { mutableStateOf<FileItem?>(null) }
    val tabTitles = listOf("Large files", "Duplicates")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        // Only the visible tab is composed, so only its flow is collected.
        // That keeps the expensive duplicate scan from starting until the
        // user actually opens the Duplicates tab.
        when (selectedTab) {
            0 -> LargeFilesTab(viewModel, imageLoader) { selectedFile = it }
            else -> DuplicatesTab(viewModel, imageLoader) { selectedFile = it }
        }
    }

    selectedFile?.let { file ->
        FileActionsSheet(
            file = file,
            onDismiss = { selectedFile = null },
            onShare = { /* TODO */ },
            onRename = { /* TODO */ },
            onDelete = { /* TODO */ }
        )
    }
}

@Composable
private fun LargeFilesTab(viewModel: CleanupViewModel, imageLoader: ImageLoader, onFileClick: (FileItem) -> Unit) {
    val state by viewModel.largeFiles.collectAsStateWithLifecycle()

    when (val current = state) {
        LoadState.Loading -> CenteredLoading("Finding large files...")
        is LoadState.Error -> CenteredMessage(current.message)
        is LoadState.Success -> {
            if (current.data.isEmpty()) {
                CenteredMessage("No media files found")
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(current.data, key = { it.uri.toString() }) { file ->
                        MediaRow(file, imageLoader, onClick = { onFileClick(file) })
                    }
                }
            }
        }
    }
}

@Composable
private fun DuplicatesTab(viewModel: CleanupViewModel, imageLoader: ImageLoader, onFileClick: (FileItem) -> Unit) {
    val state by viewModel.duplicateGroups.collectAsStateWithLifecycle()

    when (val current = state) {
        LoadState.Loading -> CenteredLoading("Scanning for duplicates...")
        is LoadState.Error -> CenteredMessage(current.message)
        is LoadState.Success -> {
            if (current.data.isEmpty()) {
                CenteredMessage("No duplicates found")
            } else {
                val totalReclaimable = current.data.sumOf { it.reclaimableBytes }

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        Text(
                            text = "Up to ${formatBytes(totalReclaimable)} can be freed " +
                                    "from ${current.data.size} duplicate groups",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    current.data.forEach { group ->
                        item(key = "header-${group.hash}") {
                            GroupHeader(group)
                        }
                        items(group.files, key = { "${group.hash}-${it.uri}" }) { file ->
                            MediaRow(file, imageLoader, onClick = { onFileClick(file) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupHeader(group: DuplicateGroup) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "${group.files.size} copies · ${formatBytes(group.reclaimableBytes)} reclaimable",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun CenteredLoading(message: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator()
            Text(message)
        }
    }
}

@Composable
private fun CenteredMessage(message: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}