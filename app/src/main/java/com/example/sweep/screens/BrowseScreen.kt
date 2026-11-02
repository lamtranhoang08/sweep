package com.example.sweep.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sweep.LocalAppContainer
import com.example.sweep.data.FileRepositoryImpl
import com.example.sweep.data.FileSystemDataSource
import com.example.sweep.data.MediaStoreDataSource
import com.example.sweep.data.FileHasher
import com.example.sweep.screens.components.CenteredLoading
import com.example.sweep.screens.components.CenteredMessage
import com.example.sweep.util.formatBytes
import com.example.sweep.util.openFile

/**
 * Interactive directory browser screen displaying files and subdirectories
 * Support tree selection via Storage Access Framework (SAF) and native file preview
 *
 * * @param viewModel State holder for managing folder navigation and file listing.
 */
// TODO: Rename/delete aren't offered here: items from the folder picker are SAF documents
// and the file actions only support MediaStore items.
@Composable
fun BrowseScreen(
    viewModel: BrowseViewModel = viewModel(
        factory = BrowseViewModel.Factory(
            LocalAppContainer.current.repository,
            folderPreferences = LocalAppContainer.current.folderPreferences
        )
    )
) {
    val context = LocalContext.current
    val state by viewModel.files.collectAsStateWithLifecycle()
    val folderStack by viewModel.folderStack.collectAsStateWithLifecycle()
    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            viewModel.pickFolder(it)
        }
    }


    // System back climbs the folder trail. With no folder open this is
    // disabled, so back behaves as it did before (it leaves the screen).
    BackHandler(enabled = folderStack.isNotEmpty()) {
        viewModel.navigateUp()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(15.dp)
    ) {
        if (folderStack.isEmpty()) {
            Button(
                onClick = { folderPickerLauncher.launch(null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Pick a folder to browse")
            }
        } else {
            FolderHeader(
                trail = folderStack,
                onUp = viewModel::navigateUp,
                onChangeFolder = { folderPickerLauncher.launch(null) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (val current = state) {
            LoadState.Loading -> CenteredLoading("Loading...")
            is LoadState.Error -> CenteredMessage(current.message)
            is LoadState.Success -> {
                if (current.data.isEmpty()) {
                    CenteredMessage(
                        if (folderStack.isEmpty()) "No media found" else "This folder is empty"
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(current.data, key = { it.uri.toString() }) { file ->
                            ListItem(
                                headlineContent = {
                                    Text(file.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                },
                                supportingContent = {
                                    if (!file.isDirectory) Text(formatBytes(file.sizeBytes))
                                },
                                leadingContent = {
                                    Icon(
                                        imageVector = if (file.isDirectory) {
                                            Icons.Default.Folder
                                        } else {
                                            Icons.Default.InsertDriveFile
                                        },
                                        contentDescription = null,
                                        tint = if (file.isDirectory) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.secondary
                                        }
                                    )
                                },
                                modifier = Modifier.clickable {
                                    if (file.isDirectory) viewModel.openFolder(file.uri)
                                    else openFile(context, file.uri)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Shows where the user is: an up arrow, the current folder's name, the path
 * to it once they're below the picked folder, and a way to pick another folder.
 */
@Composable
private fun FolderHeader(
    trail: List<BrowseFolder>,
    onUp: () -> Unit,
    onChangeFolder: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
    ) {
        IconButton(onClick = onUp) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Up one folder"
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = trail.last().name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (trail.size > 1) {
                Text(
                    text = trail.joinToString(" › ") { it.name },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        TextButton(onClick = onChangeFolder) { Text("Change") }
    }
}
