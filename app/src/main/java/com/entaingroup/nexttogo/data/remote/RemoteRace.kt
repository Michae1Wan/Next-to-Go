package com.entaingroup.nexttogo.data.remote

import com.google.gson.JsonElement

data class RemoteRace(
    val race_id: String,
    val race_name: String,
    val race_number: Int,
    val meeting_id: String,
    val meeting_name: String,
    val category_id: String,
    val advertised_start: JsonElement
)