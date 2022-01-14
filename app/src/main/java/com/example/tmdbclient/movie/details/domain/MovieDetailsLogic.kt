package com.example.tmdbclient.movie.details.domain

class MovieDetailsInteractor(val dataSource: DataSource) {

    interface DataSource {
        fun fetchMovieDetails(movieId:Int): Result<MovieDetails>
        fun rateMovie(movieId:Int, sessionId: String, rating: Float): Result<Unit>
        fun removeMovieRating(movieId:Int, sessionId: String): Result<Unit>
    }

    fun fetchMovieDetails(movieId: Int): Result<MovieDetails> {
        return dataSource.fetchMovieDetails(movieId)
    }

    fun rateMovie(movieId:Int, sessionId: String, rating: Float): Result<Unit> {

        //rating should be in range [0.5..10 step 0.5] or else backend returns "400 Bad Request"
        return if ( (rating in 0.5f..10.0f) and (rating.mod(0.5f) == 0f) ) {
            dataSource.rateMovie(movieId, sessionId, rating)
        } else {
            Result.failure(
                exception = IllegalArgumentException("Rating must be in range [0.5..10 step 0.5]")
            )
        }
    }

    fun removeMovieRating(movieId:Int, sessionId: String): Result<Unit> {
        return dataSource.removeMovieRating(movieId, sessionId)
    }

    companion object {
        const val RATING_MAX = 10f
        const val RATING_MIN = 0f
        const val RATING_STEP = 0.5f
    }
}

data class MovieDetails(
    val title: String,
    val posterPath: String,
    val genres: List<String>,
    val tagline: String,
    val overview: String,
    val userScore: Float,
    val runtime: Int
)