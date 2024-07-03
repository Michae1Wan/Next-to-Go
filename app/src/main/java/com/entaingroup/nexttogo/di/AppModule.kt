package com.entaingroup.nexttogo.di

import com.entaingroup.nexttogo.API_BASE_URL
import com.entaingroup.nexttogo.data.RaceRepository
import com.entaingroup.nexttogo.data.remote.ApiService
import com.entaingroup.nexttogo.ui.compose.races.RacesViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val networkModule = module {
    single {
        Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                OkHttpClient.Builder().addInterceptor(
                    HttpLoggingInterceptor().apply {
                        setLevel(HttpLoggingInterceptor.Level.BODY)
                    }
                ).build()
            ).build()
    }
    single { get<Retrofit>().create(ApiService::class.java) }
}

val repositoryModule = module {
    single { RaceRepository(get())}
}

val viewModelModule = module {
    viewModel { RacesViewModel(get()) }
}