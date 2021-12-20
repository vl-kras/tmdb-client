package com.example.tmdbclient

import com.example.tmdbclient.profile.ProfileBackend
import com.example.tmdbclient.profile.ProfileRepository
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// very dirty (knows about most dependencies)
object ServiceLocator {

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder().build()
    }

    private val gsonConverterFactory by lazy {
        GsonConverterFactory.create()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(TmdbBasePaths.API_BASE_PATH)
            .addConverterFactory(gsonConverterFactory)
            .client(httpClient)
            .build()
    }

    @JvmName("getConfiguredRetrofit")
    fun getRetrofit(): Retrofit {
        return retrofit
    }

    fun getProfileRepositoryBackend() : ProfileRepository.ProfileBackendContract {
        return ProfileBackend()
    }
}