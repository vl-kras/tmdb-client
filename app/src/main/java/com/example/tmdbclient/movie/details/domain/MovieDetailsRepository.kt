package com.example.tmdbclient.movie.details.domain

class MovieDetailsRepository(val backend: MovieDetailsBackendContract) {

    interface MovieDetailsBackendContract {
        fun fetchMovieDetails(movieId:Int): MovieDetails
        fun rateMovie(movieId:Int, sessionId: String, rating: Float)
        fun removeMovieRating(movieId:Int, sessionId: String)
    }

    data class MovieDetails(
        val title: String,
        val posterPath: String,
        val genres: List<String>,
        val isAdult: Boolean,
        val tagline: String,
        val overview: String,
        val userScore: Float,
        val runtime: Int
    )

    fun fetchMovieDetails(movieId: Int): MovieDetails {
        return backend.fetchMovieDetails(movieId)
    }

    fun rateMovie(movieId:Int, sessionId: String, rating: Float) {

        //rating should be in range [0.5..10 step 0.5] or else backend returns "400 Bad Request"
        if ((rating in 0.5f..10.0f) and (rating.mod(0.5f) == 0f)) {
            backend.rateMovie(movieId, sessionId, rating)
        } else {
            throw IllegalArgumentException("Rating must be in range [0.5..10 step 0.5]")
        }
    }

    fun removeMovieRating(movieId:Int, sessionId: String) {
        backend.removeMovieRating(movieId, sessionId)
    }
}
