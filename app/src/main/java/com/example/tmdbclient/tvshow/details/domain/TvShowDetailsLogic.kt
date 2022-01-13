package com.example.tmdbclient.tvshow.details.domain

class TvShowDetailsInteractor(val dataSource: DataSource) {

    interface DataSource {
        fun fetchTvShowDetails(showId: Int): Result<TvShowDetails>
        fun rateTvShow(showId: Int, sessionId: String, rating: Float): Result<Unit>
        fun removeTvShowRating(showId: Int, sessionId: String): Result<Unit>
    }

    fun fetchTvShowDetails(showId: Int): Result<TvShowDetails> {
        return dataSource.fetchTvShowDetails(showId)
    }

    fun rateTvShow(showId: Int, sessionId: String, rating: Float): Result<Unit> {

        //rating should be in range [0.5..10 step 0.5] or else backend returns "400 Bad Request"
        return if ((rating in 0.5f..10.0f) and (rating.mod(0.5f) == 0f)) {
            dataSource.rateTvShow(showId, sessionId, rating)
        } else {
            Result.failure(IllegalArgumentException("Rating must be in range [0.5..10 step 0.5]"))
        }
    }

    fun removeTvShowRating(showId: Int, sessionId: String): Result<Unit> {
        return dataSource.removeTvShowRating(showId, sessionId)
    }

    companion object {
        const val RATING_MAX = 10f
        const val RATING_MIN = 0f
        const val RATING_STEP = 0.5f
    }
}

data class TvShowDetails(
    val id: Int,
    val title: String,
    val posterPath: String,
    val status: String,
    val genres: List<String>,
    val userScore: Float,
    val tagline: String,
    val overview: String
)