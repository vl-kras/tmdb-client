package com.example.tmdbclient.tvshow.list.domain

class TvShowListInteractor(private val dataSource: DataSource) {

    interface DataSource {
        fun fetchPopularShows(page: Int = 1): Result<List<TvShow>>
    }

    fun fetchPopularShows(page: Int = 1): Result<List<TvShow>> {
        return dataSource.fetchPopularShows(page)
    }
}

data class TvShow(
    val id: Int,
    val title: String,
    val posterPath: String
)