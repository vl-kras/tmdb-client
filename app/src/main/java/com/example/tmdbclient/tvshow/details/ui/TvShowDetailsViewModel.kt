package com.example.tmdbclient.tvshow.details.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.tmdbclient.shared.ServiceLocator
import com.example.tmdbclient.tvshow.details.domain.TvShowDetails
import com.example.tmdbclient.tvshow.details.domain.TvShowDetailsInteractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException

class TvShowDetailsViewModel: ViewModel() {

    private val ioDispatcher = Dispatchers.IO

    val state: MutableStateFlow<TvShowDetailsState> = MutableStateFlow(TvShowDetailsState.InitialState)

    fun getState(): StateFlow<TvShowDetailsState> = state

    suspend fun handleAction(action: TvShowDetailsState.Action) {
        state.update { state ->
            withContext(ioDispatcher) {
                state.handle(action)
            }
        }
    }
}

sealed class TvShowDetailsState {

    protected val interactor = TvShowDetailsInteractor(
        dataSource = ServiceLocator.getTvShowDetailsInteractorDataSource()
    )

    sealed class Action {
        class Load(val showId: Int): Action()
        class PostRating(val sessionId: String, val showId: Int, val rating: Float, val onResult: (String) -> Unit): Action()
        class DeleteRating(val sessionId: String, val showId: Int, val onResult: (String) -> Unit): Action()
    }

    abstract fun handle(action: Action): TvShowDetailsState

    object InitialState: TvShowDetailsState() {

        override fun handle(action: Action): TvShowDetailsState {

            return when (action) {
                is Action.Load -> {
                    loadTvShowDetails(action)
                }
                else -> {
                    Log.e(this.javaClass.name, "$this cannot handle $action")
                    this
                }
            }
        }

        private fun loadTvShowDetails(action: Action.Load): TvShowDetailsState {

            return interactor.fetchTvShowDetails(action.showId)
                .fold(
                    onSuccess = { tvShowDetails ->
                        DisplayState(content = tvShowDetails)
                    },
                    onFailure = { throwable ->
                        ErrorState(throwable as Exception)
                    }
                )
        }
    }

    data class ErrorState(val exception: Exception): TvShowDetailsState() {

        override fun handle(action: Action): TvShowDetailsState {

            return when (action) {
                is Action.Load -> {
                    loadTvShowDetails(action)
                }
                else -> {
                    Log.e(this.javaClass.name, "$this cannot handle $action")
                    this
                }
            }
        }

        private fun loadTvShowDetails(action: Action.Load): TvShowDetailsState {

            return interactor.fetchTvShowDetails(action.showId)
                .fold(
                    onSuccess = { tvShowDetails ->
                        DisplayState(content = tvShowDetails)
                    },
                    onFailure = { throwable ->
                        ErrorState(throwable as Exception)
                    }
                )
        }
    }

    data class DisplayState(val content: TvShowDetails): TvShowDetailsState() {

        override fun handle(action: Action): TvShowDetailsState {

            return when (action) {
                is Action.PostRating -> {
                    postRating(action)
                }
                is Action.DeleteRating -> {
                    deleteRating(action)
                }
                else -> {
                    Log.e(this.javaClass.name, "$this cannot handle $action")
                    this
                }
            }
        }

        private fun postRating(action: Action.PostRating): TvShowDetailsState {

            return interactor.rateTvShow(
                action.showId,
                action.sessionId,
                action.rating
            ).fold(
                onSuccess = {
                    action.onResult("Successfully posted rating")
                    this
                },
                onFailure = {
                    action.onResult("Failed to post rating")
                    this
                }
            )
        }

        private fun deleteRating(action: Action.DeleteRating): TvShowDetailsState {

            return interactor.removeTvShowRating(
                action.showId,
                action.sessionId
            ).fold(
                onSuccess = {
                    action.onResult("Successfully deleted rating")
                    this
                },
                onFailure = {
                    action.onResult("Failed to delete rating")
                    this
                }
            )
        }
    }
}
