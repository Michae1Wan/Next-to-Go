package com.entaingroup.nexttogo

import com.entaingroup.nexttogo.data.RaceRepository
import com.entaingroup.nexttogo.data.model.Category
import com.entaingroup.nexttogo.data.model.Race
import com.entaingroup.nexttogo.data.remote.DEFAULT_RACE_RETRIEVAL_COUNT
import com.entaingroup.nexttogo.network.NetworkResponse
import com.entaingroup.nexttogo.ui.compose.races.RacesViewModel
import junit.framework.TestCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.junit.MockitoRule
import java.time.Instant

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class RacesViewModelTest: TestCase() {
    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var raceRepository: RaceRepository
    private lateinit var viewModel: RacesViewModel

    @Before
    public override fun setUp() {
        raceRepository = mock(RaceRepository::class.java)
    }

    @Test
    fun `RacesViewModel emits loading state when retrieving races`() = runTest {
        try {
            doReturn(
                flowOf(NetworkResponse.Loading<List<Race>>())
            ).`when`(raceRepository).getNextRaces()

            viewModel = RacesViewModel(raceRepository)
            assertTrue(viewModel.viewState.value.isLoading)
            assertFalse(viewModel.viewState.value.showError)

        } catch (exception: Exception) {
            fail()
        }
    }

    @Test
    fun `RacesViewModel emits success state after races retrieved`() = runTest {
        try {
            doReturn(
                flowOf(NetworkResponse.Success(createMockRaces()))
            ).`when`(raceRepository).getNextRaces()

            viewModel = RacesViewModel(raceRepository)
            assertTrue(viewModel.viewState.value.races.size == DEFAULT_RACE_RETRIEVAL_COUNT)
            assertTrue(viewModel.viewState.value.getNextRaces().size == DEFAULT_RACE_DISPLAY_COUNT)
            assertFalse(viewModel.viewState.value.isLoading)
            assertFalse(viewModel.viewState.value.showError)

        } catch (exception: Exception) {
            fail()
        }
    }

    @Test
    fun `RacesViewModel emits error state on API error`() = runTest {
        try {
            doReturn(
                flowOf(NetworkResponse.Error<List<Race>>("error"))
            ).`when`(raceRepository).getNextRaces()

            viewModel = RacesViewModel(raceRepository)
            assertFalse(viewModel.viewState.value.isLoading)
            assertTrue(viewModel.viewState.value.showError)

        } catch (exception: Exception) {
            fail()
        }
    }

    @Test
    fun `RacesViewModel sets showFilters state correctly`() = runTest {
        try {
            doReturn(
                flowOf(NetworkResponse.Success(createMockRaces()))
            ).`when`(raceRepository).getNextRaces()

            viewModel = RacesViewModel(raceRepository)
            viewModel.showFilters(true)
            assertTrue(viewModel.viewState.value.showFilters)
            viewModel.showFilters(false)
            assertFalse(viewModel.viewState.value.showFilters)

        } catch (exception: Exception) {
            fail()
        }
    }

    @Test
    fun `RacesViewModel filter races correctly`() = runTest {
        try {
            // mock test data
            val categoryIds = listOf("0", "1", "2")
            val mockCategories = createMockCategories(categoryIds)
            val numRaces = 9

            doReturn(
                flowOf(NetworkResponse.Success(createMockRaces(numRaces, categoryIds)))
            ).`when`(raceRepository).getNextRaces()

            viewModel = RacesViewModel(raceRepository)

            categoryIds.forEachIndexed { index, _ ->
                val category = mockCategories[index]
                viewModel.toggleFilter(category)
                val races = viewModel.viewState.value.getNextRaces(numRaces)
                assertTrue(races.size == categoryIds.size * (index + 1))
                assertTrue(races.any {race -> race.categoryId == category.id})
            }

            categoryIds.forEachIndexed { index, _ ->
                val category = mockCategories[index]
                viewModel.toggleFilter(category)
                val races = viewModel.viewState.value.getNextRaces(numRaces)
                // all race should show when there is no filter
                if (viewModel.viewState.value.selectedCategoryFilters.isEmpty()) {
                    assertTrue(races.size == numRaces)
                }
                else {
                    assertTrue(races.size == numRaces - categoryIds.size * (index + 1))
                    assertFalse(races.any {race -> race.categoryId == category.id})
                }
            }

        } catch (exception: Exception) {
            fail()
        }
    }
}

/**
 * Creates mock race list for testing. The number of races is 10 by default.
 */
fun createMockRaces(number: Int = DEFAULT_RACE_RETRIEVAL_COUNT, categoryIds: List<String> = emptyList()): List<Race> {
    val mockRaces = mutableListOf<Race>()
    for (i in 0 until number) {
        mockRaces.add(
            Race(
                i.toString(),
                "Race ${i + 1}",
                i,
                "",
                "Beverly Hills",
                if (categoryIds.isEmpty()) "0" else categoryIds[i % categoryIds.size],
                Instant.now().plusSeconds(TIME_SECONDS_IN_MINUTE * i.toLong())
            )
        )
    }
    return mockRaces
}

fun createMockCategories(categoryIds: List<String>): List<Category> {
    val mockCategories = mutableListOf<Category>()
    categoryIds.forEach { id ->
        mockCategories.add(Category("", 0, id))
    }
    return mockCategories
}