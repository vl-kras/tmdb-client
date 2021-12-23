package com.example.tmdbclient.movie.list.logic

class MovieListRepository(private val backend: MovieListBackendContract) {

    interface MovieListBackendContract {
        fun fetchPopularMovies(page: Int = 1): List<Movie>
    }

    data class Movie(
        val id: Int,
        val title: String,
        val posterPath: String
    )

    fun fetchPopularMovies(page: Int = 1): List<Movie> {
        return backend.fetchPopularMovies(page)
    }
}