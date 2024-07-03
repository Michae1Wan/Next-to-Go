package com.entaingroup.nexttogo.data

import com.entaingroup.nexttogo.data.remote.ApiService
import com.entaingroup.nexttogo.data.remote.DEFAULT_RACE_RETRIEVAL_COUNT
import com.entaingroup.nexttogo.utils.apiFlow

class RaceRepository(private val apiService: ApiService) {

    suspend fun getNextRaces(count: Int = DEFAULT_RACE_RETRIEVAL_COUNT) = apiFlow(
        { result ->
            result?.data?.race_summaries?.map { it.value.toRace() } ?: emptyList()
        },
        {
            apiService.getNextRaces(count = count)
        }
    )

}