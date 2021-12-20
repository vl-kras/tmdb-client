package com.example.tmdbclient.movie.details

import androidx.lifecycle.*
import com.example.tmdbclient.Backend
import com.example.tmdbclient.MovieDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MovieDetailsViewModel: ViewModel() {

    private val ioDispatcher = Dispatchers.IO
    private val backend = Backend

    private lateinit var movie: MovieDetails

    suspend fun getMovieById(movieId: Int): MovieDetails {
        return if (::movie.isInitialized) {
            movie
        } else {
            withContext(viewModelScope.coroutineContext + ioDispatcher) {
                Backend.getMovieDetails(movieId)
            }
        }
    }

    suspend fun rateMovie(movieId: Int, rating: Float, sessionId: String) : Boolean {
        return withContext(ioDispatcher) {
            Backend.postMovieRating(movieId, rating, sessionId)
        }
    }

    suspend fun removeMovieRating(movieId: Int, sessionId: String) : Boolean {
        return withContext(ioDispatcher) {
            Backend.deleteMovieRating(movieId, sessionId)
        }
    }
}