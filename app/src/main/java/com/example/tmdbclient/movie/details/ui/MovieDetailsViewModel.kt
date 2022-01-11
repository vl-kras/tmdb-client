package com.example.tmdbclient.movie.details.ui

import androidx.lifecycle.*
import com.example.tmdbclient.movie.details.domain.MovieDetailsRepository
import com.example.tmdbclient.shared.ServiceLocator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

class MovieDetailsViewModel: ViewModel() {

    private val ioDispatcher = Dispatchers.IO

    val state: MutableStateFlow<MovieDetailsState> = MutableStateFlow(MovieDetailsState.Initial)

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

    protected val repository = MovieDetailsRepository(
        backend = ServiceLocator.movieDetailsBackend
    )

    sealed class Action(val onResult: (String) -> Unit) {
        class Load(val movieId: Int, onResult: (String) -> Unit): Action(onResult)
        class PostRating(val sessionId: String, val movieId: Int, val rating: Float, onResult: (String) -> Unit): Action(onResult)
        class DeleteRating(val sessionId: String, val movieId: Int, onResult: (String) -> Unit): Action(onResult)
    }

    abstract fun handle(action: Action): MovieDetailsState

    object Initial: MovieDetailsState() {

        override fun handle(action: Action): MovieDetailsState {
            return when (action) {
                is Action.Load -> {
                    val movieDetails = repository.fetchMovieDetails(action.movieId)
                    movieDetails.let { result ->
                        var newState: MovieDetailsState = this
                        result.onSuccess {
                            newState = Display(it)
                            action.onResult("Successfully loaded movie details")
                        }.onFailure {
                            newState = MovieDetailsState.Error(it)
                            action.onResult("Failed to load movie details")
                        }
                        newState
                    }
                }
                else -> {
                    throw IllegalArgumentException("$this cannot handle $action")
                }
            }
        }
    }

    data class Error(val exception: Throwable): MovieDetailsState() {

        override fun handle(action: Action): MovieDetailsState {
            return when (action) {
                is Action.Load -> {
                    val movieDetails = repository.fetchMovieDetails(action.movieId)
                    movieDetails.let { result ->
                        var newState: MovieDetailsState = this
                        result.onSuccess {
                            newState = Display(it)
                            action.onResult("Successfully loaded movie details")
                        }.onFailure {
                            newState = MovieDetailsState.Error(it)
                            action.onResult("Failed to load movie details")
                        }
                        newState
                    }
                }
                else -> {
                    throw IllegalArgumentException("$this cannot handle $action")
                }
            }
        }
    }

    data class Display(val content: MovieDetailsRepository.MovieDetails): MovieDetailsState() {

        override fun handle(action: Action): MovieDetailsState {
            return when (action) {
                is Action.PostRating -> {
                    repository.rateMovie(
                        action.movieId,
                        action.sessionId,
                        action.rating
                    ).let { result ->
                        result.onSuccess {
                            action.onResult("Successfully posted movie rating ${action.rating}")
                        }.onFailure {
                            action.onResult("Failed to post movie rating")
                        }
                    }
                    this
                }
                is Action.DeleteRating -> {
                    repository.removeMovieRating(
                        action.movieId,
                        action.sessionId
                    ).let { result ->
                        result.onSuccess {
                            action.onResult("Successfully deleted movie rating")
                        }.onFailure {
                            action.onResult("Failed to delete movie rating")
                        }
                    }
                    this
                }
                else -> {
                    throw IllegalArgumentException("$this cannot handle $action")
                }
            }
        }
    }
}