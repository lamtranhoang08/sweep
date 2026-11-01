package com.example.sweep.screens.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import coil.ImageLoader
import coil.compose.AsyncImage
import com.example.sweep.data.FileItem
import com.example.sweep.data.StorageCategory
import com.example.sweep.util.formatBytes

/**
 * A list row for a media file: thumbnail, name and size. Shared by every
 * screen that lists files (CleanUp and Search)
 *
 * @param highlight Words to highlight inside the file name. Leave blank for no highlighting
 * @param onClick Called when the row is tapped. Null makes the row non-interactive
 * @param onLongClick Called when the row is long-pressed.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaRow(
    file: FileItem,
    imageLoader: ImageLoader,
    modifier: Modifier = Modifier,
    highlight: String = "",
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null
) {
    val highlightColor = MaterialTheme.colorScheme.primary
    val name = remember(file.name, highlight, highlightColor) {
        highlightedName(file.name, highlight, highlightColor)
    }

    Column(modifier = modifier) {
        ListItem(
            headlineContent = {
                Text(file.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
            },
            supportingContent = { Text(formatBytes(file.sizeBytes)) },
            leadingContent = { RowThumbnail(file, imageLoader) },
            modifier = if (onClick != null) {
                Modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick)
            } else {
                Modifier
            }
        )
        HorizontalDivider()
    }
}

/**
 * Highlights all occurrences of [highlight] in [name] with [highlightColor]
 */
private fun highlightedName(name: String, query: String, color: Color): AnnotatedString {
    val words = query.trim().lowercase().split(Regex("\\s+")).filter { it.isNotEmpty() }
    if (words.isEmpty()) return AnnotatedString(name)

    val lowerName = name.lowercase()
    // A few characters change length when lowercased, which would shift the
    // highlight positions. Skip highlighting rather than highlight the wrong text
    if (lowerName.length != name.length) return AnnotatedString(name)

    return buildAnnotatedString {
        append(name)
        for (word in words) {
            var start = lowerName.indexOf(word)
            while (start >= 0) {
                addStyle(
                    SpanStyle(color = color, fontWeight = FontWeight.Bold),
                    start,
                    start + word.length
                )
                start = lowerName.indexOf(word, start + word.length)
            }
        }
    }
}

/**
 * A thumbnail for a media file.
 */
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