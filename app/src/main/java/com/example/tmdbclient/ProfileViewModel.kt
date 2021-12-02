package com.example.tmdbclient

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

class ProfileViewModel : ViewModel() {

    private val backgroundScheduler: Scheduler = Schedulers.io()
    private val foregroundScheduler: Scheduler = AndroidSchedulers.mainThread()

    private val backend = Backend()

    private var _profile: MutableLiveData<GuestSession> = MutableLiveData()
    val profile: LiveData<GuestSession> by lazy {
        Single.fromCallable { backend.createGuestSession() }
            .subscribeOn(backgroundScheduler)
            .observeOn(foregroundScheduler)
            .subscribe(
                { _profile.value = it },
                { Log.e("BLABLA", it.localizedMessage) }
            )
        _profile
    }

    fun spec() {

    }
}