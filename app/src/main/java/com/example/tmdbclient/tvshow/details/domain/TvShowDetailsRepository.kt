package com.example.tmdbclient.tvshow.details.domain

class TvShowDetailsRepository(val backend: TvShowDetailsBackendContract) {

    interface TvShowDetailsBackendContract {
        fun fetchTvShowDetails(showId: Int): TvShowDetails
        fun rateTvShow(showId: Int, sessionId: String, rating: Float)
        fun removeTvShowRating(showId: Int, sessionId: String)
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

    fun fetchTvShowDetails(showId: Int): TvShowDetails {
        return backend.fetchTvShowDetails(showId)
    }

    fun rateTvShow(showId: Int, sessionId: String, rating: Float) {

        //rating should be in range [0.5..10 step 0.5] or else backend returns "400 Bad Request"
        if ((rating in 0.5f..10.0f) and (rating.mod(0.5f) == 0f)) {
            backend.rateTvShow(showId, sessionId, rating)
        } else {
            throw IllegalArgumentException("Rating must be in range [0.5..10 step 0.5]")
        }
    }

    fun removeTvShowRating(showId: Int, sessionId: String) {
        backend.removeTvShowRating(showId, sessionId)
    }
}