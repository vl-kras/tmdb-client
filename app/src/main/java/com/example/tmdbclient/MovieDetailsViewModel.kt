package com.example.tmdbclient

import androidx.lifecycle.*
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
                backend.getMovieDetails(movieId)
            }
        }
    }
}