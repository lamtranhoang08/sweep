package com.example.sweep.screens

import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.ImageLoader
import coil.compose.AsyncImage

/**
 * Full-screen viewer for a single image or video.
 *
 * Images are drawn with Coil. Videos use the framework [VideoView] wrapped
 * in [AndroidView], the simplest option that needs no extra dependency.
 *
 * @param uri Content Uri of the item to show.
 * @param onBack Called when the back arrow is tapped.
 */
// TODO: Swipe left/right to move between items (HorizontalPager) instead of
//  returning to the grid after every photo.
// TODO: Pinch-to-zoom and double-tap-to-zoom for images.
// TODO: Replace VideoView with Media3 ExoPlayer for better format support,
//  fullscreen controls, and lifecycle handling (pause when app is backgrounded).
// TODO: No file actions (share/delete) from the full-screen viewer yet; reuse
//  FileActionsHost here.
@Composable
fun MediaViewerScreen(
    uri: Uri,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // Ask Android for the file's MIME type instead of passing an extra
    // "isVideo" argument through navigation.
    val isVideo = remember(uri) {
        context.contentResolver.getType(uri)?.startsWith("video/") == true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (isVideo) {
            AndroidView(
                factory = { ctx ->
                    VideoView(ctx).apply {
                        val controller = MediaController(ctx)
                        controller.setAnchorView(this)
                        setMediaController(controller)
                        setVideoURI(uri)
                        setOnPreparedListener { start() }
                    }
                },
                onRelease = { it.stopPlayback() },
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)
            )
        } else {
            AsyncImage(
                model = uri,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }

        IconButton(
            onClick = onBack,
            modifier = Modifier
                .statusBarsPadding()
                .padding(8.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
    }
}