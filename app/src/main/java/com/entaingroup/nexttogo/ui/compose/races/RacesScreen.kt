package com.entaingroup.nexttogo.ui.compose.races

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.entaingroup.nexttogo.R
import com.entaingroup.nexttogo.RACE_COUNTDOWN_DURATION_SECONDS
import com.entaingroup.nexttogo.RACE_COUNTDOWN_ELAPSE_LIMIT_SECONDS
import com.entaingroup.nexttogo.TIME_SECONDS_IN_MINUTE
import com.entaingroup.nexttogo.data.model.Category
import com.entaingroup.nexttogo.data.model.Race
import com.entaingroup.nexttogo.ui.compose.common.ErrorScreen
import com.entaingroup.nexttogo.ui.compose.common.LoadingScreen
import com.entaingroup.nexttogo.ui.theme.NextToGoTheme
import com.entaingroup.nexttogo.utils.secondsToTimeRemaining
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber
import java.time.Instant

/**
 * Creates a list of available categories from the resource xml.
 * An empty list is returned if the number of titles, category IDs and icon
 * drawable IDs do not match.
 *
 * @return A list of filters defined in the resource xml, or an empty list
 * if the resources do not match up correctly.
 */
@Composable
fun getCategoryFilters(): List<Category> {
    val categoryFilters = mutableListOf<Category>()

    // note: the order and number of filter titles must match the category IDs
    // and drawable IDs
    val filterTitles = stringArrayResource(id = R.array.filter_titles)
    val filterIds = stringArrayResource(id = R.array.filter_ids)
    val filterIcons = LocalContext.current.resources.obtainTypedArray(R.array.filter_icons)
    // check if the number of filter titles, ids and icons all match
    if (!listOf(filterTitles.size, filterIds.size).all { it == filterIcons.length() }) {
        Timber.e("Number of category titles, IDs and icons do not match!")
    } else {
        // create the available filters
        filterTitles.forEachIndexed { index, title ->
            categoryFilters.add(
                Category(
                    title,
                    filterIcons.getResourceId(index, android.R.drawable.ic_menu_close_clear_cancel),
                    filterIds[index]
                )
            )
        }
        filterIcons.recycle()
    }
    return categoryFilters
}

/**
 * Countdown from current time to the provided target time. The remaining time
 * in seconds is passed to the provided content. Time is checked every minute.
 * The countdown starts when targetTime is less than RACE_COUNTDOWN_DURATION_SECONDS
 * and stops when RACE_COUNTDOWN_ELAPSE_LIMIT_SECONDS has passed the targetTime.
 *
 * @param targetTime The target time to count down to.
 * @param onFinished Called when the countdown is finished.
 * @param content The content that uses the remaining time.
 */
@Composable
fun Countdown(targetTime: Instant, onFinished: () -> Unit, content: @Composable (remainingTime: Long) -> Unit) {
    var remainingTime by remember {
        mutableLongStateOf(targetTime.minusSeconds(Instant.now().epochSecond).epochSecond)
    }

    content.invoke(remainingTime)

    var isRunning by remember { mutableStateOf(false) }
    LifecycleResumeEffect(Unit) {
        isRunning = true
        onPauseOrDispose { isRunning = false }
    }

    LaunchedEffect(isRunning) {
        // start countdown when the target time range is reached
        while (isRunning) {
            val currentTime = Instant.now()
            with (targetTime.minusMillis(currentTime.toEpochMilli()).toEpochMilli()) {
                remainingTime = this / 1000L
                // add 1 for rounding
                if (remainingTime > 0 || this > 0) remainingTime += 1
                Timber.d("Target time: $targetTime, current time: $currentTime, remaining: $remainingTime seconds")
                when {
                    // wait until the countdown starts, checking every minute
                    remainingTime > RACE_COUNTDOWN_DURATION_SECONDS -> {
                        delay(this % (TIME_SECONDS_IN_MINUTE * 1000))
                    }
                    // starting countdown and continue until elapse limit is reached
                    remainingTime in (RACE_COUNTDOWN_ELAPSE_LIMIT_SECONDS + 1)..RACE_COUNTDOWN_DURATION_SECONDS -> {
                        // attempting to sync the timer with the system clock
                        delay(this.mod(1000L))
                    }
                    else -> {
                        isRunning = false
                        Timber.d("Stopping timer.")
                    }
                }
            }
        }
        onFinished()
    }
}

@Composable
fun RacesScreen(viewModel: RacesViewModel = koinViewModel()) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()

    if (state.isLoading) {
        LoadingScreen()
    }
    else if (state.showError) {
        ErrorScreen { viewModel.getRaces() }
    }
    else {
        // create the available filters
        val categoryFilters = getCategoryFilters()
        if (categoryFilters.isEmpty()) {
            ErrorScreen("Failed to load filters") {}
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
            ) {
                RaceFilters(
                    modifier = Modifier
                        .fillMaxWidth(),
                    options = categoryFilters,
                    showOptions = state.showFilters,
                    onMenuToggled = { viewModel.showFilters(it) },
                    selectedCategories = state.selectedCategoryFilters,
                    onClearOptions = { viewModel.clearFilters() },
                    onOptionChosen = { viewModel.toggleFilter(it) },
                )
                RaceList(
                    modifier = Modifier
                        .fillMaxSize(),
                    races = state.getNextRaces(),
                    categories = categoryFilters,
                    onRaceFinished = { race ->
                        Timber.d("Timer for ${race.raceId} stopped.")
                        viewModel.onRaceFinished(race)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RaceFilters(
    modifier: Modifier = Modifier,
    options: List<Category>,
    showOptions: Boolean = false,
    selectedCategories: List<Category> = emptyList(),
    onMenuToggled: (Boolean) -> Unit = {},
    onClearOptions: () -> Unit = {},
    onOptionChosen: (Category) -> Unit = {},
) {
    var expanded by remember { mutableStateOf(showOptions) }
    val selectedOptionsList = remember { mutableStateListOf<Category>()}

    // set up currently selected categories
    selectedOptionsList.clear()
    selectedCategories.forEach{
        selectedOptionsList.add(it)
    }

    Timber.d("Currently selected category: $selectedOptionsList")

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = it
            onMenuToggled(it)
        },
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ExposedDropdownMenuDefaults.textFieldColors().unfocusedContainerColor),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .weight(1f),
                readOnly = true,
                singleLine = true,
                value = stringResource(id = R.string.title_filters),
                onValueChange = {},
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                colors = ExposedDropdownMenuDefaults.textFieldColors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
            )
            TooltipBox(
                positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                tooltip = {
                    PlainTooltip {
                        Text("Clear filters")
                    }
                },
                state = rememberTooltipState(),
            ) {
                IconButton(
                    onClick = onClearOptions
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                onMenuToggled(false)
            },
        ) {
            for (option in options) {
                // use derivedStateOf to evaluate if the category is checked
                val checked = remember {
                    derivedStateOf{ selectedOptionsList.any { it.id == option.id } }
                }.value

                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = { _ ->
                                    onOptionChosen(option)
                                },
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                painter = painterResource(option.iconDrawableId),
                                contentDescription = option.title,
                                modifier = Modifier.size(30.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = option.title)
                        }
                    },
                    onClick = {
                        onOptionChosen(option)
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

@Composable
fun RaceList(
    modifier: Modifier = Modifier,
    races: List<Race>,
    categories: List<Category>,
    onRaceFinished: (race: Race) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        items(items = races, key = { race -> race.raceId }) { race ->
            Timber.d("Composing RaceItem for $race")
            RaceItem(
                race,
                categories.firstOrNull { category ->
                    category.id == race.categoryId }?.iconDrawableId
                    // default icon when the matching category is not found
                    ?: android.R.drawable.ic_menu_close_clear_cancel,
                onRaceFinished
            )
        }
    }
}

@Composable
fun RaceItem(race: Race, iconDrawableId: Int, onRaceFinished: (race: Race) -> Unit) {
    val formattedRaceText = stringResource(
        id = R.string.race_display, race.raceNumber, race.meetingName)

    Row (modifier = Modifier
        .fillMaxWidth()
        .background(MaterialTheme.colorScheme.primary)
        .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
    ){
        Icon(
            painter = painterResource(iconDrawableId),
            contentDescription = formattedRaceText,
            modifier = Modifier.size(30.dp),
            tint = MaterialTheme.colorScheme.onPrimary,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            modifier = Modifier
                .weight(1f),
            text = formattedRaceText,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onPrimary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Countdown(
            targetTime = race.advertisedStart,
            onFinished = {
                onRaceFinished(race)
            }
        ){ remainingTime ->
            Timber.d("Composing countdown")
            Text(
                text = remainingTime.secondsToTimeRemaining(),
                style = MaterialTheme.typography.titleMedium,
                color =
                    if (remainingTime <= RACE_COUNTDOWN_DURATION_SECONDS) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}

//@PreviewFontScale
//@PreviewScreenSizes
@PreviewLightDark
@Composable
fun PreviewRaceScreen() {
    val mockRaces = mutableListOf<Race>()

    for (i in 0..4) {
        mockRaces.add(
            Race(
                i.toString(),
                "Race ${i+1}",
                i,
                "",
                "Beverly Hills",
                stringArrayResource(id = R.array.filter_ids)[i%3],
                Instant.now().plusSeconds(TIME_SECONDS_IN_MINUTE * i.toLong()))
        )
    }
    NextToGoTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            RaceFilters(
                modifier = Modifier
                    .fillMaxWidth(),
                options = emptyList(),
                showOptions = true,
            ) {}
            RaceList(
                modifier = Modifier
                    .fillMaxSize(),
                races = mockRaces,
                categories = getCategoryFilters()
            ) {}
        }
    }
}