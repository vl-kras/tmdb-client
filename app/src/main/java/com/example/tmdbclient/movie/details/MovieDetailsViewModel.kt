package com.example.tmdbclient.movie.details

import androidx.lifecycle.*
import com.example.tmdbclient.shared.ServiceLocator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import kotlin.Exception

class MovieDetailsViewModel: ViewModel() {

    private val state = MutableLiveData<MovieDetailsState>(MovieDetailsState.Initial)

    fun getState(): LiveData<MovieDetailsState> = state

    fun handleAction(action: MovieDetailsState.Action) {
        viewModelScope.launch(Dispatchers.IO) {
            val oldState = state.value!!
            state.postValue(MovieDetailsState.Loading)
            val newState = oldState.handle(action)
            state.postValue(newState)
        }
    }
}

sealed class MovieDetailsState {

    protected val repository = MovieDetailsRepository(
        backend = ServiceLocator.movieDetailsBackend
    )

    sealed class Action {
        data class Load(val movieId: Int): Action()
        data class PostRating(val sessionId: String, val movieId: Int, val rating: Float): Action()
        data class DeleteRating(val sessionId: String, val movieId: Int): Action()
    }

    abstract fun handle(action: Action): MovieDetailsState

    object Initial: MovieDetailsState() {

        override fun handle(action: Action): MovieDetailsState {
            return when (action) {
                is Action.Load -> {
                    try {
                        val movieDetails = repository.fetchMovieDetails(action.movieId)
                        MovieDetailsState.Display(movieDetails)
                    }
                    catch (e: SocketTimeoutException) {
                        MovieDetailsState.Error(e)
                    }
                }
                else -> {
                    throw IllegalArgumentException("$this cannot handle $action")
                }
            }
        }
    }

    object Loading: MovieDetailsState() {

        override fun handle(action: Action): MovieDetailsState = this
    }

    data class Error(val exception: Exception): MovieDetailsState() {

        override fun handle(action: Action): MovieDetailsState {
            return when (action) {
                is Action.Load -> {
                    val movieDetails = repository.fetchMovieDetails(action.movieId)
                    MovieDetailsState.Display(movieDetails)
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
                    )
                    this
                }
                is Action.DeleteRating -> {
                    repository.removeMovieRating(
                        action.movieId,
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