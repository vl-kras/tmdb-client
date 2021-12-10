package com.example.tmdbclient

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import kotlinx.coroutines.Dispatchers

class TvShowListViewModel : ViewModel() {

    private val backend = Backend
    private val ioDispatcher = Dispatchers.IO

    val showList: LiveData<List<TvShow>> by lazy {
        liveData(ioDispatcher) {
            emit(backend.getPopularTvShowsByPage())
        }
    }
}