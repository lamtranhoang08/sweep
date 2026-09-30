package com.example.sweep.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sweep.BrowseViewModel
import com.example.sweep.FileRepositoryImpl
import com.example.sweep.FileSystemDataSource
import com.example.sweep.MediaStoreDataSource

/**
 * Interactive directory browser screen displaying files and subdirectories
 * Support tree selection via Storage Access Framework (SAF) and native file preview
 *
 * * @param viewModel State holder for managing folder navigation and file listing.
 */
@Composable
fun BrowseScreen(
    viewModel: BrowseViewModel = viewModel(
        factory = BrowseViewModel.Factory(
            FileRepositoryImpl(
                mediaStoreDataSource = MediaStoreDataSource(
                    LocalContext.current.applicationContext
                ),
                fileSystemDataSource = FileSystemDataSource(
                    LocalContext.current.applicationContext
                )
            )
        )
    )
) {
    val context = LocalContext.current
    val files by viewModel.files.collectAsStateWithLifecycle()
    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            viewModel.openFolder(it)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(15.dp)
    ) {
        Button(
            onClick = { folderPickerLauncher.launch(null) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Pick a folder to browse")
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (files.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No files found or no folder selected",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(files) { file ->
                    ListItem(
                        headlineContent = { Text(text = file.name) },
                        supportingContent = {
                            if (!file.isDirectory) {
                                Text(text = "${file.sizeBytes / 1024} KB")
                            }
                        },
                        leadingContent = {
                            Icon(
                                imageVector = if (file.isDirectory) Icons.Default.Folder else Icons.Default.InsertDriveFile,
                                contentDescription = null,
                                tint = if (file.isDirectory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                            )
                        },
                        modifier = Modifier.clickable {
                            if (file.isDirectory) {
                                viewModel.openFolder(file.uri)
                            } else {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(file.uri, context.contentResolver.getType(file.uri))
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // Handle cases where no activity can handle the intent
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

/**
 * Fires an external intent to open and view the given file URI.
 */
private fun openFileIntent(context: android.content.Context, uri: Uri) {
    val mimeType = context.contentResolver.getType(uri) ?: "*/*"
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, mimeType)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(
            context,
            "No application found to open this file format.",
            Toast.LENGTH_SHORT
        ).show()
    }
}
