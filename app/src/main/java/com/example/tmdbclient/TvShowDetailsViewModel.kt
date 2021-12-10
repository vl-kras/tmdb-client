package com.example.tmdbclient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TvShowDetailsViewModel: ViewModel() {

    private val ioDispatcher = Dispatchers.IO
    private val backend = Backend

    private lateinit var show: TvShowDetails

    suspend fun getTvShowById(showId: Int): TvShowDetails {
        return if (::show.isInitialized) {
            show
        } else {
            withContext(viewModelScope.coroutineContext + ioDispatcher) {
                backend.getTvShowDetails(showId)
            }
        }
    }

    suspend fun rateTvShow(showId: Int, rating: Float, sessionId: String) : Boolean {
        return withContext(ioDispatcher) {
            backend.postTvShowRating(showId, rating, sessionId)
        }
    }

    suspend fun removeTvShowRating(showId: Int, sessionId: String) : Boolean {
        return withContext(ioDispatcher) {
            backend.deleteTvShowRating(showId, sessionId)
        }
    }
}