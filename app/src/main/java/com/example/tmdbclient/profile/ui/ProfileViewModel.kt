package com.example.tmdbclient.profile.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.tmdbclient.profile.domain.ProfileInteractor
import com.example.tmdbclient.shared.ServiceLocator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext


class ProfileViewModel : ViewModel() {

    private val ioDispatcher = Dispatchers.IO

    private val state: MutableStateFlow<ProfileState> = MutableStateFlow(ProfileState.Initial)
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

    sealed class Action {

        class SignIn(
            val username: String, val password: String,
            val onResult: (String) -> Unit
        ): Action()

        class Restore(
            val sessionId: String,
            val onResult: (String) -> Unit
        ): Action()

        class SignOut(val onResult: (String) -> Unit) : Action()
    }

    protected val interactor = ProfileInteractor(
        dataSource = ServiceLocator.getProfileInteractorDataSource()
    )

    abstract fun handle(action: Action): ProfileState

    object Initial: ProfileState() {

        override fun handle(action: Action): ProfileState {

            return when(action) {
                is Action.Restore -> {
                    restoreSession(action)
                }
                else -> {
                    Log.e(this.javaClass.name, "$this cannot handle $action")
                    this
                }
            }
        }

        private fun restoreSession(action: Action.Restore): ProfileState {

            return interactor.fetchAccountDetails(action.sessionId).fold(
                onSuccess = { session ->
                    action.onResult("Successfully restored session")
                    ActiveSession(
                        sessionId = session.sessionId,
                        userId = session.userId,
                        username = session.username,
                        name = session.name
                    )
                },
                onFailure = {
                    action.onResult("Failed to restore session")
                    NoSession
                }
            )
        }
    }

    data class ActiveSession(
        val sessionId: String,
        val userId: Int,
        val username: String,
        val name: String
    ) : ProfileState() {

        override fun handle(action: Action): ProfileState {

            return when(action) {
                is Action.SignOut -> {
                    signOut(action)
                }
                else -> {
                    Log.e(this.javaClass.name, "$this cannot handle $action")
                    this
                }
            }
        }

        private fun signOut(action: Action.SignOut): ProfileState {

            return interactor.signOut(sessionId).fold(
                onSuccess = {
                    action.onResult("Successfully signed out")
                    NoSession
                },
                onFailure = { throwable ->
                    action.onResult((throwable as Exception).message ?: "Failed to sign out")
                    this
                }
            )
        }
    }

    object NoSession : ProfileState() {

        override fun handle(action: Action): ProfileState {

            return when(action) {
                is Action.SignIn -> {
                    signIn(action)
                }
                else -> {
                    Log.e(this.javaClass.name, "$this cannot handle $action")
                    this
                }
            }
        }

        private fun signIn(action: Action.SignIn): ProfileState {

            return interactor.signIn(action.username, action.password).fold(
                onSuccess = { session ->
                    action.onResult("Successfully signed in")
                    ActiveSession(
                        sessionId = session.sessionId,
                        userId = session.userId,
                        username = session.username,
                        name = session.name
                    )
                },
                onFailure = {
                    action.onResult("Failed to sign in")
                    this
                }
            )
        }
    }
}