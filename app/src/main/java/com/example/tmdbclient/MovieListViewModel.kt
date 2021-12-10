package com.example.tmdbclient

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import kotlinx.coroutines.Dispatchers

class MovieListViewModel : ViewModel() {

    private val backend = Backend
    private val ioDispatcher = Dispatchers.IO

    val movieList: LiveData<List<Movie>> by lazy {
        liveData(ioDispatcher) {
            emit(backend.getPopularMoviesByPage())
        }
    }
}