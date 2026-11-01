package com.example.sweep.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.decode.VideoFrameDecoder
import com.example.sweep.data.FileHasher
import com.example.sweep.data.FileItem
import com.example.sweep.data.FileRepositoryImpl
import com.example.sweep.data.FileSystemDataSource
import com.example.sweep.data.MediaStoreDataSource
import com.example.sweep.data.StorageCategory
import com.example.sweep.screens.components.FileActionsHost
import com.example.sweep.screens.components.MediaRow
import com.example.sweep.util.openFile

/**
 * Type filters shown as chips,
 * null category means ALL types
 */
private val CATEGORY_FILTERS: List<Pair<StorageCategory?, String>> = listOf(
    null to "All",
    StorageCategory.IMAGE to "Images",
    StorageCategory.VIDEO to "Videos",
    StorageCategory.AUDIO to "Audio"
)

/**
 * Full-screen search over the indexed media library.
 *
 * Opens with the keyboard up. Results update as user types
 * Narrow down search result and show the matched text highlighted
 *
 * @param onMediaClick Called for images and videos, which open in the viewer
 */
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onMediaClick: (FileItem) -> Unit
) {
    val context = LocalContext.current

    // TODO: Same repository and ImageLoader duplication as the other screens;
    //  replace with a shared AppContainer.
    val repository = remember {
        FileRepositoryImpl(
            mediaStoreDataSource = MediaStoreDataSource(context.applicationContext),
            fileSystemDataSource = FileSystemDataSource(context.applicationContext),
            fileHasher = FileHasher(context.applicationContext)
        )
    }

    val viewModel: SearchViewModel = viewModel(
        factory = SearchViewModel.Factory(repository)
    )

    val query by viewModel.query.collectAsStateWithLifecycle()
    val category by viewModel.category.collectAsStateWithLifecycle()
    val results by viewModel.results.collectAsStateWithLifecycle()

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components { add(VideoFrameDecoder.Factory()) }
            .build()
    }

    var selectedFile by remember { mutableStateOf<FileItem?>(null) }
    val listState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Open with the keyboard ready, but only on a fresh search: coming back
    // from the viewer should keep the keyboard out of the way of the results.
    LaunchedEffect(Unit) {
        if (query.isEmpty()) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    // Scrolling the results means the user is done typing.
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) keyboardController?.hide()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        SearchField(
            query = query,
            onQueryChange = viewModel::onQueryChange,
            onBack = onBack,
            onSearch = { keyboardController?.hide() },
            focusRequester = focusRequester
        )

        CategoryChips(
            selected = category,
            onSelect = viewModel::onCategorySelect
        )

        // The results lag the text by the debounce delay. Until they catch up,
        // an empty list means "still searching", not "nothing found".
        val resultsAreCurrent = results.query == query

        when {
            query.isBlank() -> CenteredHint("Search by file name")

            results.items.isEmpty() && resultsAreCurrent ->
                CenteredHint("No files match \"${query.trim()}\"")

            results.items.isEmpty() -> Unit

            else -> LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    val count = results.items.size
                    Text(
                        text = if (count == 1) "1 result" else "$count results",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                items(results.items, key = { it.uri.toString() }) { file ->
                    MediaRow(
                        file = file,
                        imageLoader = imageLoader,
                        highlight = results.query,
                        onClick = {
                            if (file.category == StorageCategory.AUDIO) {
                                openFile(context, file.uri)
                            } else {
                                onMediaClick(file)
                            }
                        },
                        onLongClick = { selectedFile = file }
                    )
                }
            }
        }
    }

    FileActionsHost(file = selectedFile, onDismiss = { selectedFile = null })
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit,
    onSearch: () -> Unit,
    focusRequester: FocusRequester
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 12.dp, top = 8.dp, bottom = 4.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back"
            )
        }

        TextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Search files") },
            singleLine = true,
            shape = CircleShape,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search"
                        )
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester)
        )
    }
}

@Composable
private fun CategoryChips(
    selected: StorageCategory?,
    onSelect: (StorageCategory?) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        CATEGORY_FILTERS.forEach { (category, label) ->
            FilterChip(
                selected = selected == category,
                onClick = { onSelect(category) },
                label = { Text(label) }
            )
        }
    }
}

@Composable
private fun CenteredHint(message: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}