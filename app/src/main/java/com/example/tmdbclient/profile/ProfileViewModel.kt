package com.example.tmdbclient.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tmdbclient.ServiceLocator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.SocketTimeoutException


class ProfileViewModel : ViewModel() {
    private val ioDispatcher = Dispatchers.IO

    private var profile: MutableLiveData<ProfileState> = MutableLiveData(ProfileState.EmptyState)

    suspend fun handleAction(action: ProfileState.Action) {
        try {
            profile.postValue(ProfileState.Loading)
            val newState = withContext(ioDispatcher) {
                profile.value!!.handle(action)
            }
            profile.postValue(newState)
        }
        catch (e: IOException) {
            profile.postValue(ProfileState.Error(e))
        }
        catch (e: SocketTimeoutException) {
            profile.postValue(ProfileState.Error(e))
        }
    }

    fun getProfile(): LiveData<ProfileState> = profile
}

sealed class ProfileState {

    sealed class Action {
        data class SignIn(val username: String, val password: String): Action()
        data class Restore(val sessionId: String): Action()
        object SignOut: Action()
    }

    protected val repository = ProfileRepository(
        backend = ServiceLocator.profileRepositoryBackend
    )

    abstract fun handle(action: Action): ProfileState

    class UserState(
        val sessionId: String,
        val userId: Int,
        val username: String,
        val name: String
    ) : ProfileState() {

        override fun handle(action: Action): ProfileState {
            return when(action) {
                is Action.SignOut -> {
                    repository.signOut(sessionId)
                    EmptyState
                }
                else -> {
                    throw IllegalArgumentException("$this can't handle $action")
                }
            }
        }
    }

    object EmptyState : ProfileState() {

        override fun handle(action: Action): ProfileState {

            return when(action) {
                is Action.SignIn -> {
                    val session = repository.signIn(action.username, action.password)
                    UserState(
                        sessionId = session.sessionId,
                        userId = session.userId,
                        username = session.username,
                        name = session.name
                    )
                }
                is Action.Restore -> {
                    val session = repository.fetchAccountDetails(action.sessionId)
                    UserState(
                        sessionId = session.sessionId,
                        userId = session.userId,
                        username = session.username,
                        name = session.name
                    )
                }
                else -> {
                    throw IllegalArgumentException("$this can't handle $action")
                }
            }
        }
    }

    object Loading : ProfileState() {
        override fun handle(action: Action): ProfileState = this
    }

    class Error(val exception: Exception): ProfileState() {

        override fun handle(action: Action): ProfileState {
            return when(action) {
                is Action.SignIn -> {
                    val session = repository.signIn(action.username, action.password)
                    UserState(
                        sessionId = session.sessionId,
                        userId = session.userId,
                        username = session.username,
                        name = session.name
                    )
                }
                else -> {
                    throw IllegalArgumentException("$this can't handle $action")
                }
            }
        }
    }
}