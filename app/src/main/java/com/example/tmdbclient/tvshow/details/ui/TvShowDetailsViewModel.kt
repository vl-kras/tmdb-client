package com.example.tmdbclient.tvshow.details.ui

import androidx.lifecycle.ViewModel
import com.example.tmdbclient.shared.ServiceLocator
import com.example.tmdbclient.tvshow.details.domain.TvShowDetailsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException

class TvShowDetailsViewModel: ViewModel() {

    private val ioDispatcher = Dispatchers.IO

    val state: MutableStateFlow<TvShowDetailsState> = MutableStateFlow(TvShowDetailsState.Initial)

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

    protected val repository = TvShowDetailsRepository(
        backend = ServiceLocator.tvShowDetailsBackend
    )

    sealed class Action(val onResult: (String) -> Unit) {
        class Load(val showId: Int, onResult: (String)-> Unit): Action(onResult)
        class PostRating(val sessionId: String, val showId: Int, val rating: Float, onResult: (String) -> Unit): Action(onResult)
        class DeleteRating(val sessionId: String, val showId: Int, onResult: (String) -> Unit): Action(onResult)
    }

    abstract fun handle(action: Action): TvShowDetailsState

    object Initial: TvShowDetailsState() {

        override fun handle(action: Action): TvShowDetailsState {
            return when (action) {
                is Action.Load -> {
                    try {
                        val tvShowDetails = repository.fetchTvShowDetails(action.showId)
                        action.onResult("WASD")
                        Display(tvShowDetails)
                    }
                    catch (e: SocketTimeoutException) {
                        action.onResult
                        Error(e)
                    }
                }
                else -> {
                    throw IllegalArgumentException("$this cannot handle $action")
                }
            }
        }
    }

    data class Error(val exception: Exception): TvShowDetailsState() {

        override fun handle(action: Action): TvShowDetailsState {
            return when (action) {
                is Action.Load -> {
                    val tvShowDetails = repository.fetchTvShowDetails(action.showId)
                    action.onResult
                    Display(tvShowDetails)
                }
                else -> {
                    throw IllegalArgumentException("$this cannot handle $action")
                }
            }
        }
    }

    data class Display(val content: TvShowDetailsRepository.TvShowDetails): TvShowDetailsState() {

        override fun handle(action: Action): TvShowDetailsState {
            return when (action) {
                is Action.PostRating -> {
                    repository.rateTvShow(
                        action.showId,
                        action.sessionId,
                        action.rating
                    )
                    action.onResult
                    this
                }
                is Action.DeleteRating -> {
                    repository.removeTvShowRating(
                        action.showId,
                        action.sessionId
                    )
                    action.onResult
                    this
                }
                else -> {
                    throw IllegalArgumentException("$this cannot handle $action")
                }
            }
        }
    }
}
