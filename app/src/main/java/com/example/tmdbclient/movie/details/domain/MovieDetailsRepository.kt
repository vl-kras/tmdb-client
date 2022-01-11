package com.example.tmdbclient.movie.details.domain

class MovieDetailsRepository(val backend: MovieDetailsBackendContract) {

    interface MovieDetailsBackendContract {
        fun fetchMovieDetails(movieId:Int): Result<MovieDetails>
        fun rateMovie(movieId:Int, sessionId: String, rating: Float): Result<Unit>
        fun removeMovieRating(movieId:Int, sessionId: String): Result<Unit>
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

    fun fetchMovieDetails(movieId: Int): Result<MovieDetails> {
        return backend.fetchMovieDetails(movieId)
    }

    fun rateMovie(movieId:Int, sessionId: String, rating: Float): Result<Unit> {

        //rating should be in range [0.5..10 step 0.5] or else backend returns "400 Bad Request"
        return if ((rating in 0.5f..10.0f) and (rating.mod(0.5f) == 0f)) {
            backend.rateMovie(movieId, sessionId, rating)
        } else {
            throw IllegalArgumentException("Rating must be in range [0.5..10 step 0.5]")
        }
    }

    fun removeMovieRating(movieId:Int, sessionId: String): Result<Unit> {
        return backend.removeMovieRating(movieId, sessionId)
    }
}
