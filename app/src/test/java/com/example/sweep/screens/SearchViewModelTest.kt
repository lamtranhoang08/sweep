package com.example.sweep.screens

import com.example.sweep.FakeFileRepository
import com.example.sweep.MainDispatcherRule
import com.example.sweep.data.StorageCategory
import com.example.sweep.keepCollecting
import com.example.sweep.testFile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val library = listOf(
        testFile("holiday-beach.jpg"),
        testFile("my-holiday.mp4", StorageCategory.VIDEO),
        testFile("IMG_001.jpg")
    )

    /** Builds the ViewModel and plays the screen's part by collecting its results. */
    private fun TestScope.createViewModel(
        repository: FakeFileRepository = FakeFileRepository(library)
    ): SearchViewModel {
        val viewModel = SearchViewModel(repository, StandardTestDispatcher(testScheduler))
        keepCollecting(viewModel.results)
        runCurrent()
        return viewModel
    }

    private fun SearchViewModel.resultNames(): List<String> = results.value.items.map { it.name }

    @Test
    fun `starts with no results`() = runTest {
        val viewModel = createViewModel()

        assertEquals(SearchResults(query = ""), viewModel.results.value)
    }

    @Test
    fun `waits for a pause in typing before searching`() = runTest {
        val viewModel = createViewModel()

        viewModel.onQueryChange("holiday")
        advanceTimeBy(200)
        // Still inside the debounce window, so the results belong to the old query.
        assertEquals("", viewModel.results.value.query)

        advanceTimeBy(100)
        runCurrent()
        assertEquals("holiday", viewModel.results.value.query)
        assertEquals(listOf("holiday-beach.jpg", "my-holiday.mp4"), viewModel.resultNames())
    }

    @Test
    fun `clearing the field empties the results immediately`() = runTest {
        val viewModel = createViewModel()
        viewModel.onQueryChange("holiday")
        advanceTimeBy(300)
        runCurrent()
        assertEquals(2, viewModel.results.value.items.size)

        viewModel.onQueryChange("")
        runCurrent() // no time advances: a blank query skips the debounce

        assertEquals(SearchResults(query = ""), viewModel.results.value)
    }

    @Test
    fun `choosing a type filter updates the results immediately`() = runTest {
        val viewModel = createViewModel()
        viewModel.onQueryChange("holiday")
        advanceTimeBy(300)
        runCurrent()

        viewModel.onCategorySelect(StorageCategory.VIDEO)
        runCurrent() // chips are not debounced

        assertEquals(listOf("my-holiday.mp4"), viewModel.resultNames())
    }

    @Test
    fun `results update when the library changes while searching`() = runTest {
        val repository = FakeFileRepository(library)
        val viewModel = createViewModel(repository)
        viewModel.onQueryChange("holiday")
        advanceTimeBy(300)
        runCurrent()

        repository.files.value = library + testFile("holiday-sunset.jpg")
        runCurrent()

        assertEquals(
            listOf("holiday-beach.jpg", "holiday-sunset.jpg", "my-holiday.mp4"),
            viewModel.resultNames()
        )
    }
}