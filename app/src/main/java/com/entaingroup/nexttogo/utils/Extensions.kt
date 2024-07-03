package com.entaingroup.nexttogo.utils

import com.entaingroup.nexttogo.TIME_SECONDS_IN_DAY
import com.entaingroup.nexttogo.TIME_SECONDS_IN_HOUR
import com.entaingroup.nexttogo.TIME_SECONDS_IN_MINUTE
import com.entaingroup.nexttogo.network.NetworkResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Response
import java.time.Instant
import kotlin.math.ceil

fun <T> MutableStateFlow<T>.updateState(action: T.() -> T) {
    value = action(value)
}

/**
 * Creates an API flow on Dispatcher.IO that that emits network responses and uses the mapper
 * to convert the received data of success response.
 */
fun <T, V> apiFlow(mapper: (T?) -> V?, call: suspend () -> Response<T>?) : Flow<NetworkResponse<V?>> {
    return flow {
        emit(NetworkResponse.Loading())
        val response = call()
        emit(NetworkResponse.Success(mapper.invoke(response?.body())))

    }.catch { e ->
        emit(NetworkResponse.Error(e.message ?: "Unknown Error"))
    }.flowOn(Dispatchers.IO)
}

fun Long.epochToInstant(): Instant = Instant.ofEpochSecond(this)

fun Long.secondsToTimeRemaining(): String {
    return when {
        this >= TIME_SECONDS_IN_DAY -> "${ceil(this/TIME_SECONDS_IN_DAY.toDouble())} d"
        this >= TIME_SECONDS_IN_HOUR -> "${this/TIME_SECONDS_IN_HOUR} h"
        this >= TIME_SECONDS_IN_MINUTE -> "${
            // use 2 for any duration between 2 minutes and 1 minute
            if(this in (TIME_SECONDS_IN_MINUTE+1)..TIME_SECONDS_IN_MINUTE*2) 2
            else this/TIME_SECONDS_IN_MINUTE
        } m"
        else -> "$this s"
    }
}