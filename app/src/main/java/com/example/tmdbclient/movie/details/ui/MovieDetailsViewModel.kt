package com.example.tmdbclient.movie.details.ui

import android.util.Log
import androidx.lifecycle.*
import com.example.tmdbclient.movie.details.domain.MovieDetails
import com.example.tmdbclient.movie.details.domain.MovieDetailsInteractor
import com.example.tmdbclient.shared.ServiceLocator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

class MovieDetailsViewModel: ViewModel() {

    private val ioDispatcher = Dispatchers.IO

    val state: MutableStateFlow<MovieDetailsState> = MutableStateFlow(MovieDetailsState.InitialState())

    fun getState(): StateFlow<MovieDetailsState> = state

    suspend fun handleAction(action: MovieDetailsState.Action) {
        state.update { state ->
            withContext(ioDispatcher) {
                state.handle(action)
            }
        }
    }
}

sealed class MovieDetailsState {

    sealed class Action {
        class Load(val movieId: Int): Action()
        class PostRating(val sessionId: String, val movieId: Int, val rating: Float, val onResult: (String) -> Unit): Action()
        class DeleteRating(val sessionId: String, val movieId: Int, val onResult: (String) -> Unit): Action()
    }

    protected val interactor = MovieDetailsInteractor(
        dataSource = ServiceLocator.movieDetailsBackend
    )

    abstract fun handle(action: Action): MovieDetailsState

    class InitialState: MovieDetailsState() {

        override fun handle(action: Action): MovieDetailsState {

            return when (action) {
                is Action.Load -> {
                    loadMovieDetails(action)
                }
                else -> {
                    Log.e(this.javaClass.name, "$this cannot handle $action")
                    this
                }
            }
        }

        private fun loadMovieDetails(action: Action.Load): MovieDetailsState {

            val movieDetailsRequest = interactor.fetchMovieDetails(action.movieId)
            return movieDetailsRequest.let { result ->
                var newState: MovieDetailsState = this
                result.onSuccess {
                    newState = DisplayState(it)
                }.onFailure {
                    newState = ErrorState(it as Exception)
                }
                newState
            }
        }
    }

    class ErrorState(val exception: Exception): MovieDetailsState() {

        override fun handle(action: Action): MovieDetailsState {

            return when (action) {
                is Action.Load -> {
                    loadMovieDetails(action)
                }
                else -> {
                    Log.e(this.javaClass.name, "$this cannot handle $action")
                    this
                }
            }
        }

        private fun loadMovieDetails(action: Action.Load): MovieDetailsState {

            val movieDetailsRequest = interactor.fetchMovieDetails(action.movieId)
            return movieDetailsRequest.let { result ->
                var newState: MovieDetailsState = this
                result.onSuccess {
                    newState = DisplayState(it)
                }.onFailure {
                    newState = ErrorState(it as Exception)
                }
                newState
            }
        }
    }

    class DisplayState(val movieDetails: MovieDetails): MovieDetailsState() {

        override fun handle(action: Action): MovieDetailsState {

            return when (action) {
                is Action.PostRating -> {
                    postRating(action)
                    this
                }
                is Action.DeleteRating -> {
                    removeRating(action)
                    this
                }
                else -> {
                    Log.e(this.javaClass.name, "$this cannot handle $action")
                    this
                }
            }
        }

        private fun postRating(action: Action.PostRating) {

            interactor.rateMovie(
                action.movieId,
                action.sessionId,
                action.rating
            ).fold(
                onSuccess = {
                    action.onResult("Successfully posted rating")
                },
                onFailure = {
                    action.onResult("Failed to post rating")
                }
            )
        }

        private fun removeRating(action: Action.DeleteRating) {

            interactor.removeMovieRating(
                action.movieId,
                action.sessionId
            ).fold(
                onSuccess = {
                    action.onResult("Successfully posted rating")
                },
                onFailure = {
                    action.onResult("Failed to post rating")
                }
            )
        }
    }
}