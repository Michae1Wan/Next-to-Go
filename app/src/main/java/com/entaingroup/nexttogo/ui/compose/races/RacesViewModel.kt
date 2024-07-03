package com.entaingroup.nexttogo.ui.compose.races

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.entaingroup.nexttogo.DEFAULT_RACE_DISPLAY_COUNT
import com.entaingroup.nexttogo.data.RaceRepository
import com.entaingroup.nexttogo.data.model.Category
import com.entaingroup.nexttogo.data.model.Race
import com.entaingroup.nexttogo.network.NetworkResponse
import com.entaingroup.nexttogo.ui.BaseViewState
import com.entaingroup.nexttogo.utils.updateState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class RacesViewModel (private val raceRepository: RaceRepository): ViewModel() {
    private val _viewState = MutableStateFlow(RacesViewState())
    val viewState = _viewState.asStateFlow()

    private var _selectedCategoryFilters = emptyList<Category>()

    init {
        getRaces()
    }

    fun getRaces() {
        viewModelScope.launch {
            raceRepository.getNextRaces().collect {result ->
                when (result) {
                    is NetworkResponse.Success -> {
                        if (result.data == null) {
                            _viewState.updateState {
                                copy(isLoading = false, showError = true)
                            }
                        }
                        else onRacesLoaded(result.data)
                    }
                    is NetworkResponse.Loading -> _viewState.updateState {
                        copy(isLoading = true, showError = false)
                    }
                    else -> _viewState.updateState {
                        copy(isLoading = false, showError = true)
                    }
                }
            }
        }
    }

    fun onRaceFinished(race: Race) {
        val remainingRaces = _viewState.value.races - race
        Timber.d("R${race.raceNumber} ${race.meetingName} finished, ${remainingRaces.size} races remains")
        // get more races if needed
        if (remainingRaces.size < DEFAULT_RACE_DISPLAY_COUNT) getRaces()
        else _viewState.updateState { copy(races = remainingRaces) }
    }

    /**
     * Updates the showFilters state.
     */
    fun showFilters(show: Boolean) {
        _viewState.updateState { copy(showFilters = show) }
    }

    /**
     * Adds or removes a category from the selected category filters.
     *
     * @param category The category to add or remove.
     */
    fun toggleFilter(category: Category) {
        Timber.d("Toggling filter: ${category.title}")
        with (_viewState.value.selectedCategoryFilters) {
            val existingCategory = this.firstOrNull { it.id == category.id }
            _selectedCategoryFilters = if (existingCategory == null) this + category else this.minusElement(category)
            _viewState.updateState {
                copy(
                    selectedCategoryFilters = _selectedCategoryFilters
                )
            }
        }
    }

    fun clearFilters() {
        _viewState.updateState { copy(selectedCategoryFilters = emptyList()) }
    }

    private fun onRacesLoaded(races: List<Race>) {
        _viewState.updateState {
            copy(
                races = races,
                selectedCategoryFilters = this.selectedCategoryFilters,
                isLoading = false,
                showError = false
            )
        }
    }
}

data class RacesViewState(
    override val isLoading: Boolean = true,
    override val showError: Boolean = false,
    val races: List<Race> = emptyList(),
    val selectedCategoryFilters: List<Category> = emptyList(),
    val showFilters: Boolean = false,
): BaseViewState() {
    /**
     * Gets the next number of races from the list. The list is sorted by
     * the advertised start time and filtered by the selected categories.
     *
     * @param numRaces The number of races to get. Default is DEFAULT_RACE_DISPLAY_COUNT.
     *
     * @return The first number of races from the list, or the full
     * list if the number of races is less than provided numRaces.
     */
    fun getNextRaces(numRaces: Int = DEFAULT_RACE_DISPLAY_COUNT): List<Race> {
        val list = races.sortedWith(compareBy({ it.advertisedStart }, { it.meetingName }))
            .subList(0, if (numRaces <= races.size) numRaces else races.size)
        return if(selectedCategoryFilters.isEmpty()) list
            else list.filter { race ->
                selectedCategoryFilters.any { it.id == race.categoryId }
            }
    }
}

