package com.example.sweep.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import com.example.sweep.data.FileHasher
import com.example.sweep.data.FileItem
import com.example.sweep.data.FileRepositoryImpl
import com.example.sweep.data.FileSystemDataSource
import com.example.sweep.data.MediaStoreDataSource
import com.example.sweep.data.StorageCategory

/**
 * Grid of image and video thumbnails.
 *
 * @param onItemClick Called when a thumbnail is tapped. Defaults to a no-op for now;
 * it will navigate to the full-screen viewer once that route is set up.
 */
@Composable
fun GalleryScreen(
    onItemClick: (FileItem) -> Unit = {}
)  {
    val context = LocalContext.current

    // TODO: Same duplication as StorageScreen/BrowseScreen: each screen builds
    // its own repository. Replace with a shared AppContainer
    val repository = remember {
        FileRepositoryImpl(
            mediaStoreDataSource = MediaStoreDataSource(context.applicationContext),
            fileSystemDataSource = FileSystemDataSource(context.applicationContext),
            fileHasher = FileHasher(context.applicationContext)
        )
    }

    val viewModel: GalleryViewModel = viewModel(
        factory = GalleryViewModel.Factory(repository)
    )

    val items by viewModel.mediaItems.collectAsStateWithLifecycle()

    // Coil can't draw a frame from a video file out of the box. This loader
    // adds the video decoder (from the coil-video dependency) so video items get a thumbnail too
    val imageLoader = remember {
        ImageLoader.Builder(context).components { add(VideoFrameDecoder.Factory()) }
            .build()
    }

    if (items.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No media found")
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 110.dp),
            contentPadding = PaddingValues(4.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(items, key = { it.uri.toString() }) { item ->
                MediaThumbnail(
                    item = item,
                    imageLoader = imageLoader,
                    onClick = { onItemClick(item) }
                )
            }
        }
    }
}

@Composable
private fun MediaThumbnail(
    item: FileItem,
    imageLoader: ImageLoader,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(2.dp)
            .aspectRatio(1f)
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = item.uri,
            imageLoader = imageLoader,
            contentDescription = item.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        if (item.category == StorageCategory.VIDEO) {
            Icon(
                imageVector = Icons.Default.PlayCircle,
                contentDescription = "Video",
                tint = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}