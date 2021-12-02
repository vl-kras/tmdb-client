package com.example.tmdbclient

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

class MovieViewModel : ViewModel() {

    private val backgroundScheduler: Scheduler = Schedulers.io()
    private val foregroundScheduler: Scheduler = AndroidSchedulers.mainThread()

    private val backend = Backend()

    private var _movieList: MutableLiveData<List<Movie>> = MutableLiveData()
    val movieList: LiveData<List<Movie>> by lazy {
        Single.fromCallable { backend.getPopularMoviesByPage() }
            .subscribeOn(backgroundScheduler)
            .observeOn(foregroundScheduler)
            .subscribe(
                { _movieList.value = it },
                { Log.e("BLABLA", it.localizedMessage) }
            )
        _movieList
    }
}