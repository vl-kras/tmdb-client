package com.example.tmdbclient

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

class TvViewModel : ViewModel() {

    private val backgroundScheduler: Scheduler = Schedulers.io()
    private val foregroundScheduler: Scheduler = AndroidSchedulers.mainThread()

    private val backend = Backend()

    private var _showList: MutableLiveData<List<TvShow>> = MutableLiveData()
    val showList: LiveData<List<TvShow>> by lazy {
        Single.fromCallable { backend.getPopularTvShowsByPage() }
        .subscribeOn(backgroundScheduler)
        .observeOn(foregroundScheduler)
        .subscribe(
            { _showList.value = it },
            { Log.e("BLABLA", it.localizedMessage) }
        )
        _showList
    }
}