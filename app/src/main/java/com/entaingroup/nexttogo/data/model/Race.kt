package com.entaingroup.nexttogo.data.model

import androidx.compose.runtime.Immutable
import java.time.Instant

@Immutable
data class Race(
    val raceId: String,
    val raceName: String,
    val raceNumber: Int,
    val meetingId: String,
    val meetingName: String,
    val categoryId: String,
    val advertisedStart: Instant
)

data class Category(
    val title: String,
    val iconDrawableId: Int,
    val id: String
)
