package com.example.tmdbclient.tvshow.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.tmdbclient.Backend
import com.example.tmdbclient.TvShow
import kotlinx.coroutines.Dispatchers

class TvShowListViewModel : ViewModel() {

    //TODO refactor into Finite State Machine

    private val backend = Backend
    private val ioDispatcher = Dispatchers.IO

    val showList: LiveData<List<TvShow>> by lazy {
        liveData(ioDispatcher) {
            emit(Backend.getPopularTvShowsByPage())
        }
    }
}