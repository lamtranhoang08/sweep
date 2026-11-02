package com.example.sweep.screens

import com.example.sweep.FakeFileRepository
import com.example.sweep.MainDispatcherRule
import com.example.sweep.data.DuplicateGroup
import com.example.sweep.data.FileItem
import com.example.sweep.data.StorageCategory
import com.example.sweep.keepCollecting
import com.example.sweep.testFile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CleanupViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    @Test
    fun `both lists start in the loading state`() = runTest {
        val viewModel = CleanupViewModel(FakeFileRepository())

        assertEquals(LoadState.Loading, viewModel.largeFiles.value)
        assertEquals(LoadState.Loading, viewModel.duplicateGroups.value)
    }

    @Test
    fun `lists the largest files`() = runTest {
        val big = testFile("big.mp4", StorageCategory.VIDEO, sizeBytes = 5_000)
        val small = testFile("small.jpg", sizeBytes = 10)

        val repository = FakeFileRepository().apply {
            largeFilesFlow = flowOf(listOf(big, small))
        }

        val viewModel = CleanupViewModel(repository)

        keepCollecting(viewModel.largeFiles)
        runCurrent()

        assertEquals(
            LoadState.Success(listOf(big, small)),
            viewModel.largeFiles.value
        )
    }

    @Test
    fun `lists duplicate groups`() = runTest {
        val group = DuplicateGroup(
            "abc",
            listOf(
                testFile("a.jpg"),
                testFile("b.jpg")
            )
        )

        val repository = FakeFileRepository().apply {
            duplicateGroupsFlow = flowOf(listOf(group))
        }

        val viewModel = CleanupViewModel(repository)

        keepCollecting(viewModel.duplicateGroups)
        runCurrent()

        assertEquals(
            LoadState.Success(listOf(group)),
            viewModel.duplicateGroups.value
        )
    }

    @Test
    fun `a failed duplicate scan does not break the large files list`() = runTest {
        val repository = FakeFileRepository().apply {
            largeFilesFlow = flowOf(listOf(testFile("a.jpg")))

            duplicateGroupsFlow = flow {
                throw IllegalStateException("scan failed")
            }
        }

        val viewModel = CleanupViewModel(repository)

        keepCollecting(viewModel.largeFiles)
        keepCollecting(viewModel.duplicateGroups)
        runCurrent()

        assertTrue(viewModel.largeFiles.value is LoadState.Success<List<FileItem>>)
        assertEquals(
            LoadState.Error("scan failed"),
            viewModel.duplicateGroups.value
        )
    }
}