package com.example.sweep

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import coil.ImageLoader
import coil.decode.VideoFrameDecoder
import com.example.sweep.data.FileHasher
import com.example.sweep.data.FileRepository
import com.example.sweep.data.FileRepositoryImpl
import com.example.sweep.data.FileSystemDataSource
import com.example.sweep.data.MediaStoreDataSource
import com.example.sweep.data.FolderPreferences

/**
 * Builds the app's shared dependencies once and hands them out.
 *
 * This is manual dependency injection: the one place that knows how the
 * pieces are wired together. Screens ask for repository instead of
 * constructing their own, and tests can build a container around fakes.
 *
 * Everything is created lazily, so nothing is built until first use.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    /** Remembers the folder picked in Browse across restarts. */
    val folderPreferences: FolderPreferences by lazy { FolderPreferences(appContext) }

    /** Exposed as the interface so callers can't depend on the implementation. */
    val repository: FileRepository by lazy {
        FileRepositoryImpl(
            mediaStoreDataSource = MediaStoreDataSource(appContext),
            fileSystemDataSource = FileSystemDataSource(appContext),
            fileHasher = FileHasher(appContext)
        )
    }

    /**
     * One image loader for the whole app, so thumbnails decoded on one tab
     * are already in the memory cache when another tab shows them. Includes
     * the video decoder so video files get a thumbnail frame.
     */
    val imageLoader: ImageLoader by lazy {
        ImageLoader.Builder(appContext)
            .components { add(VideoFrameDecoder.Factory()) }
            .build()
    }
}

/**
 * Gives composables access to the app's [AppContainer]. Provided once in
 * MainActivity; reading it anywhere else without that fails loudly.
 */
val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("No AppContainer provided. Wrap the UI in CompositionLocalProvider(LocalAppContainer provides ...).")
}