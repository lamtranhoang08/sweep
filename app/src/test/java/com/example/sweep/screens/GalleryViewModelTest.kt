package com.example.sweep.screens

import com.example.sweep.FakeFileRepository
import com.example.sweep.MainDispatcherRule
import com.example.sweep.data.StorageCategory
import com.example.sweep.keepCollecting
import com.example.sweep.testFile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class GalleryViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private fun GalleryViewModel.shownNames(): List<String> =
        (mediaItems.value as LoadState.Success).data.map { it.name }

    @Test
    fun `starts in the loading state`() = runTest {
        val viewModel = GalleryViewModel(FakeFileRepository())

        assertEquals(LoadState.Loading, viewModel.mediaItems.value)
    }

    @Test
    fun `shows images and videos but not audio`() = runTest {
        val repository = FakeFileRepository(
            listOf(
                testFile("a.jpg", StorageCategory.IMAGE),
                testFile("b.mp4", StorageCategory.VIDEO),
                testFile("c.mp3", StorageCategory.AUDIO)
            )
        )
        val viewModel = GalleryViewModel(repository)
        keepCollecting(viewModel.mediaItems)
        runCurrent()

        assertEquals(listOf("a.jpg", "b.mp4"), viewModel.shownNames())
    }

    @Test
    fun `updates when the library changes`() = runTest {
        val repository = FakeFileRepository(listOf(testFile("a.jpg")))
        val viewModel = GalleryViewModel(repository)
        keepCollecting(viewModel.mediaItems)
        runCurrent()
        assertEquals(listOf("a.jpg"), viewModel.shownNames())

        repository.files.value = repository.files.value + testFile("b.jpg")
        runCurrent()

        assertEquals(listOf("a.jpg", "b.jpg"), viewModel.shownNames())
    }

    @Test
    fun `reports an error when the library fails to load`() = runTest {
        val repository = FakeFileRepository().apply { filesFailure = IOException("disk gone") }
        val viewModel = GalleryViewModel(repository)
        keepCollecting(viewModel.mediaItems)
        runCurrent()

        assertEquals(LoadState.Error("disk gone"), viewModel.mediaItems.value)
    }
}