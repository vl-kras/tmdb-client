package com.example.tmdbclient.shared

import com.example.tmdbclient.movie.details.data.MovieDetailsBackend
import com.example.tmdbclient.movie.details.domain.MovieDetailsInteractor
import com.example.tmdbclient.movie.list.data.MovieListBackend
import com.example.tmdbclient.movie.list.domain.MovieListInteractor
import com.example.tmdbclient.profile.data.ProfileBackend
import com.example.tmdbclient.profile.domain.ProfileInteractor
import com.example.tmdbclient.tvshow.details.data.TvShowDetailsBackend
import com.example.tmdbclient.tvshow.details.domain.TvShowDetailsRepository
import com.example.tmdbclient.tvshow.list.data.TvShowListBackend
import com.example.tmdbclient.tvshow.list.domain.TvShowListRepository
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

    //TODO switch dataSource getters to factory methods

    val profileRepositoryDataSource: ProfileInteractor.DataSource by lazy {
        ProfileBackend()
    }

    val movieListDataSource: MovieListInteractor.DataSource by lazy {
        MovieListBackend()
    }

    val movieDetailsBackend: MovieDetailsInteractor.DataSource by lazy {
        MovieDetailsBackend()
    }

    val tvShowListBackend: TvShowListRepository.TvShowListBackendContract by lazy {
        TvShowListBackend()
    }

    val tvShowDetailsBackend: TvShowDetailsRepository.TvShowDetailsBackendContract by lazy {
        TvShowDetailsBackend()
    }
}