package com.example.tmdbclient.tvshow.list

class TvShowListRepository(private val backend: TvShowListBackendContract) {

    interface TvShowListBackendContract {
        fun fetchPopularShows(page: Int = 1): List<TvShow>
    }

    data class TvShow(
        val id: Int,
        val title: String,
        val posterPath: String
    )

    fun fetchPopularMovies(page: Int = 1): List<TvShow> {
        return backend.fetchPopularShows(page)
    }
}