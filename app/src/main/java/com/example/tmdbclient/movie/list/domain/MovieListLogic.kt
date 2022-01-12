package com.example.tmdbclient.movie.list.domain

class MovieListInteractor(private val dataSource: DataSource) {

    interface DataSource {
        fun fetchPopularMovies(page: Int = FIRST_PAGE): Result<List<Movie>>
    }

    fun fetchPopularMovies(page: Int = FIRST_PAGE): Result<List<Movie>> {
        return dataSource.fetchPopularMovies(page)
    }

    companion object {
        const val FIRST_PAGE = 1
    }
}

data class Movie(
    val id: Int,
    val title: String,
    val posterPath: String
)