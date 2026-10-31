package com.example.sweep.screens.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import com.example.sweep.data.FileItem
import com.example.sweep.data.StorageCategory
import com.example.sweep.util.formatBytes

/**
 * A list row for a media file: thumbnail, name and size. Shared by every
 * screen that lists files (Cleanup now, Search later).
 *
 * @param onClick Called when the row is tapped. Null makes the row non-interactive
 */
@Composable
fun MediaRow(
    file: FileItem,
    imageLoader: ImageLoader,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Column(modifier = modifier) {
        ListItem(
            headlineContent = {
                Text(file.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
            },
            supportingContent = { Text(formatBytes(file.sizeBytes)) },
            leadingContent = { RowThumbnail(file, imageLoader) },
            modifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
        )
        HorizontalDivider()
    }
}

@Composable
private fun RowThumbnail(file: FileItem, imageLoader: ImageLoader) {
    if (file.category == StorageCategory.AUDIO) {
        // Audio files have no visual to decode, so show an icon instead.
        Icon(
            imageVector = Icons.Default.AudioFile,
            contentDescription = null,
            modifier = Modifier.size(48.dp)
        )
    } else {
        AsyncImage(
            model = file.uri,
            imageLoader = imageLoader,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(6.dp))
        )
    }
}