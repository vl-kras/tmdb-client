package com.example.tmdbclient.shared

import com.example.tmdbclient.movie.details.MovieDetailsBackend
import com.example.tmdbclient.movie.details.MovieDetailsRepository
import com.example.tmdbclient.movie.list.MovieListBackend
import com.example.tmdbclient.movie.list.logic.MovieListRepository
import com.example.tmdbclient.profile.ProfileBackend
import com.example.tmdbclient.profile.ProfileRepository
import com.example.tmdbclient.tvshow.details.TvShowDetailsBackend
import com.example.tmdbclient.tvshow.details.TvShowDetailsRepository
import com.example.tmdbclient.tvshow.list.TvShowListBackend
import com.example.tmdbclient.tvshow.list.TvShowListRepository
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// dependency configurator (very dirty)
object ServiceLocator {

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder().build()
    }

    private val gsonConverterFactory by lazy {
        GsonConverterFactory.create()
    }

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(TmdbBasePaths.API_BASE_PATH)
            .addConverterFactory(gsonConverterFactory)
            .client(httpClient)
            .build()
    }

    val profileRepositoryBackend: ProfileRepository.ProfileBackendContract by lazy {
        ProfileBackend()
    }

    val movieListBackend: MovieListRepository.MovieListBackendContract by lazy {
        MovieListBackend()
    }

    val movieDetailsBackend: MovieDetailsRepository.MovieDetailsBackendContract by lazy {
        MovieDetailsBackend()
    }

    val tvShowListBackend: TvShowListRepository.TvShowListBackendContract by lazy {
        TvShowListBackend()
    }

    val tvShowDetailsBackend: TvShowDetailsRepository.TvShowDetailsBackendContract by lazy {
        TvShowDetailsBackend()
    }
}