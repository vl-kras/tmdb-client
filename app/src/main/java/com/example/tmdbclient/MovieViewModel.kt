package com.example.tmdbclient

import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers

class MovieViewModel : ViewModel() {

    private val backend = Backend
    private val ioDispatcher = Dispatchers.IO

    val movieList: LiveData<List<Movie>> by lazy {
        liveData(ioDispatcher) {
            emit(backend.getPopularMoviesByPage())
        }
    }
}