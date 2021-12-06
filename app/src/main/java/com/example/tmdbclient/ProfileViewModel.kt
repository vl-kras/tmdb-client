package com.example.tmdbclient

import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val ioDispatcher = Dispatchers.IO
    private val backend = Backend

    private var _profile: MutableLiveData<Session> = MutableLiveData()
    val profile: LiveData<Session> = _profile
    val account: LiveData<UserAccount> by lazy {
        liveData(ioDispatcher) {
            var isRunning = true
            while(isRunning) {
                try {
                    emit(backend.getAccountDetails(profile.value?.sessionId ?: throw Exception("No session id")))
                    isRunning = false
                } catch (e: Exception) {
                    kotlinx.coroutines.delay(15_000)
                }
            }
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch(ioDispatcher) {
            val sessionId = with(backend) {
                val token = createRequestToken()
                validateTokenWithLogin(username, password, token)
                createSession(token)
            }
            _profile.postValue(Session(true, sessionId))
        }
    }

    fun setSession(id: String?) {
        if (!id.isNullOrBlank()) {
            _profile.value = Session(
                sessionId = id,
            )
        }
    }

    fun logout() {
        val sessionId = _profile.value?.sessionId
        Log.d("BLABLA", "Session = $sessionId")

        if(!sessionId.isNullOrBlank()) {
            viewModelScope.launch(ioDispatcher) {
                backend.deleteSession(sessionId)
            }
            _profile.value = Session(false, "")
            Log.d("BLABLA", "Session = ${_profile.value}")
        }
    }
}