package com.example.tmdbclient.profile.ui

import androidx.lifecycle.ViewModel
import com.example.tmdbclient.profile.domain.ProfileRepository
import com.example.tmdbclient.shared.ServiceLocator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext


class ProfileViewModel : ViewModel() {

    private val ioDispatcher = Dispatchers.IO

    val state: MutableStateFlow<ProfileState> = MutableStateFlow(ProfileState.InitialState)

    fun getState(): StateFlow<ProfileState> = state

    suspend fun handleAction(action: ProfileState.Action) {
        state.update { state ->
            withContext(ioDispatcher) {
                state.handle(action)
            }
        }
    }
}

sealed class ProfileState {

    sealed class Action(val onResult: (Result<String>) -> Unit) {
        class SignIn(val username: String, val password: String, onResult: (Result<String>) -> Unit): Action(onResult)
        class Restore(val sessionId: String, onResult: (Result<String>) -> Unit): Action(onResult)
        class SignOut(onResult: (Result<String>) -> Unit) : Action(onResult)
    }

    protected val repository = ProfileRepository(
        backend = ServiceLocator.profileRepositoryBackend
    )

    abstract fun handle(action: Action): ProfileState

    object InitialState: ProfileState() {

        override fun handle(action: Action): ProfileState {
            return when(action) {
                is Action.Restore -> {
                    try {
                        val session = repository.fetchAccountDetails(action.sessionId)
                        UserState(
                            sessionId = session.sessionId,
                            userId = session.userId,
                            username = session.username,
                            name = session.name
                        )
                    } catch (e: NoSuchElementException) {
                        EmptyState
                    }

                }
                else -> {
                    throw IllegalArgumentException("$this can't handle $action")
                }
            }
        }
    }

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
                    action.onResult(Result.success("Successfully logged out"))
                    EmptyState
                }
                else -> {
                    this
//                    throw IllegalArgumentException("$this can't handle $action")
                }
            }
        }
    }

    object EmptyState : ProfileState() {

        override fun handle(action: Action): ProfileState {

            return when(action) {
                is Action.SignIn -> {
                    val session = repository.signIn(action.username, action.password)
                    action.onResult(Result.success("Successfully logged in"))
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