package com.example.tmdbclient

import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException


class ProfileViewModel : ViewModel() {

    sealed class AppSession {
        class UserSession(val sessionId: String, val details: UserAccount) : AppSession()
        object NoSession : AppSession()
    }

    private val ioDispatcher = Dispatchers.IO
    private val backend = Backend

    private var _profile: MutableLiveData<AppSession> = MutableLiveData(AppSession.NoSession)
    val profile: LiveData<AppSession> = _profile

    fun signIn(username: String, password: String) : Boolean {
        return try {
            viewModelScope.launch {
                val sessionId = withContext(ioDispatcher) {
                    val token = backend.createRequestToken()
                    backend.validateTokenWithLogin(username, password, token)
                    backend.createSession(token)
                }
                setActiveSession(sessionId)
            }
            true
        } catch (e: IOException) {
            false
        }
    }

    suspend fun setActiveSession(sessionId: String) {
        if (sessionId.isNotBlank()) {
            val accountDetails = withContext(ioDispatcher) {
                backend.getAccountDetails(sessionId)
            }
            _profile.value = AppSession . UserSession (
                sessionId,
                accountDetails
            )
        }
    }

    fun signOut() : Boolean {
        val result: Boolean
        if (_profile.value is AppSession.UserSession) {
            result = try {
                val sessionId = (_profile.value as AppSession.UserSession).sessionId
                if (sessionId.isNotBlank()) {
                    viewModelScope.launch(ioDispatcher) {
                        backend.deleteSession(sessionId)
                    }
                    _profile.value = AppSession.NoSession
                }
                true
            } catch (e: IOException) {
                false
            }
        } else {
            result = true
        }
        return result
    }
}