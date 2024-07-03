package com.entaingroup.nexttogo.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

const val METHOD_NEXT_RACES = "nextraces"
const val DEFAULT_RACE_RETRIEVAL_COUNT = 10

interface ApiService {
    @GET("rest/v1/racing/?method=$METHOD_NEXT_RACES")
    suspend fun getNextRaces(@Query("count") count: Int = DEFAULT_RACE_RETRIEVAL_COUNT): Response<NextRacesResponse>
}

data class NextRacesResponse(
    val status: Int,
    val data: Data
)

data class Data(
    val next_to_go_ids: List<String>,
    val race_summaries: Map<String, RemoteRace>
)