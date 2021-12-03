package com.example.tmdbclient

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.lang.Exception
import kotlin.concurrent.thread

class ProfileViewModel : ViewModel() {

    private val backgroundScheduler: Scheduler = Schedulers.io()
    private val foregroundScheduler: Scheduler = AndroidSchedulers.mainThread()

    private val backend = Backend()

    private var _profile: MutableLiveData<Session> = MutableLiveData()
    val profile: LiveData<Session> = _profile

    //TODO possibly doing work on main thread, probably Promise.get()
    fun createSession(username: String, password: String) : String {
        val token = Single.fromCallable {
            with(backend) {
                val token = createRequestToken()
                validateTokenWithLogin(username, password, token)
                createSession(token)
            }
        }
            .subscribeOn(backgroundScheduler).toFuture()
//            .observeOn(foregroundScheduler)
//            .subscribe(
//                { _profile.value = Session(true, it) },
//                { Log.e("BLABLA", it.localizedMessage) }
//            )
//        Log.d("BLABLA", profile.value?.sessionId ?: "null")

        return token.get().also {
            Log.d("BLABLA", it)
        } ?: throw Exception("Nothing here")
    }

    fun setSession(id: String?) {
        if (id != null) {
            _profile.value = Session(
                sessionId = id,
            )
        }
    }

    //TODO make profile null and make the view observe it properly
    fun logout() : Boolean {
        val sessionId = _profile.value?.sessionId
        var logoutSuccessful = false

        Single.fromCallable {
            if (!sessionId.isNullOrBlank()) {
                backend.deleteSession(sessionId)
                _profile.value = Session(false, "no session")
            }
        }
            .subscribeOn(backgroundScheduler)
            .observeOn(foregroundScheduler)
            .subscribe(
                { logoutSuccessful = true },
                { logoutSuccessful = false }
            )
        return logoutSuccessful
    }
}