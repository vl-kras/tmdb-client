package com.example.tmdbclient.tvshow.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tmdbclient.shared.ServiceLocator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException

class TvShowDetailsViewModel: ViewModel() {

    private val state = MutableLiveData<TvShowDetailsState>(TvShowDetailsState.Initial)

    fun getState(): LiveData<TvShowDetailsState> = state

    fun handleAction(action: TvShowDetailsState.Action) {
        viewModelScope.launch(Dispatchers.IO) {
            val oldState = state.value!!
            state.postValue(TvShowDetailsState.Loading)
            val newState = oldState.handle(action)
            state.postValue(newState)
        }
    }
}

sealed class TvShowDetailsState {

    protected val repository = TvShowDetailsRepository(
        backend = ServiceLocator.tvShowDetailsBackend
    )

    sealed class Action {
        data class Load(val showId: Int): Action()
        data class PostRating(val sessionId: String, val showId: Int, val rating: Float): Action()
        data class DeleteRating(val sessionId: String, val showId: Int): Action()
    }

    abstract fun handle(action: Action): TvShowDetailsState

    object Initial: TvShowDetailsState() {

        override fun handle(action: Action): TvShowDetailsState {
            return when (action) {
                is Action.Load -> {
                    try {
                        val tvShowDetails = repository.fetchTvShowDetails(action.showId)
                        TvShowDetailsState.Display(tvShowDetails)
                    }
                    catch (e: SocketTimeoutException) {
                        TvShowDetailsState.Error(e)
                    }
                }
                else -> {
                    throw IllegalArgumentException("$this cannot handle $action")
                }
            }
        }
    }

    object Loading: TvShowDetailsState() {

        override fun handle(action: Action): TvShowDetailsState = this
    }

    data class Error(val exception: Exception): TvShowDetailsState() {

        override fun handle(action: Action): TvShowDetailsState {
            return when (action) {
                is Action.Load -> {
                    val tvShowDetails = repository.fetchTvShowDetails(action.showId)
                    TvShowDetailsState.Display(tvShowDetails)
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
                    this
                }
                is Action.DeleteRating -> {
                    repository.removeTvShowRating(
                        action.showId,
                        action.sessionId
                    )
                    this
                }
                else -> {
                    throw IllegalArgumentException("$this cannot handle $action")
                }
            }
        }
    }
}
