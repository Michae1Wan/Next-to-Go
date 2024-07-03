package com.entaingroup.nexttogo.data

import com.entaingroup.nexttogo.data.model.Race
import com.entaingroup.nexttogo.data.remote.RemoteRace
import com.entaingroup.nexttogo.utils.epochToInstant

fun RemoteRace.toRace() = Race(
    raceId= race_id,
    raceName = race_name,
    raceNumber = race_number,
    meetingId = meeting_id,
    meetingName = meeting_name,
    categoryId = category_id,
    advertisedStart = advertised_start.asJsonObject.get("seconds").asLong.epochToInstant()
)