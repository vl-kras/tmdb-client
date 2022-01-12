package com.example.tmdbclient.movie.list.ui

import androidx.lifecycle.ViewModel
import com.example.tmdbclient.movie.list.domain.Movie
import com.example.tmdbclient.movie.list.domain.MovieListInteractor
import com.example.tmdbclient.shared.ServiceLocator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

class MovieListViewModel : ViewModel() {

    private val ioDispatcher = Dispatchers.IO

    val state: MutableStateFlow<MovieListState> = MutableStateFlow(MovieListState.InitialState())
    fun getState(): StateFlow<MovieListState> = state

    suspend fun handleAction(action: MovieListState.Action) {
        state.update { state ->
            withContext(ioDispatcher) {
                state.handle(action)
            }
        }
    }
}

sealed class MovieListState {

    sealed class Action {
        object LoadInitial: Action()
        class LoadMore(val onResult: (Result<Unit>) -> Unit): Action()
    }

    protected val repository = MovieListInteractor(
        dataSource = ServiceLocator.movieListDataSource
    )

    abstract fun handle(action: Action) : MovieListState

    class InitialState: MovieListState() {

        override fun handle(action: Action): MovieListState {

            return when (action) {
                is Action.LoadInitial -> {
                    loadInitial()
                }
                else -> {
                    throw IllegalArgumentException("$this cannot handle $action")
                }
            }
        }

        private fun loadInitial(): MovieListState {

            val result = repository.fetchPopularMovies()

            return if (result.isSuccess) {
                val movieList = result.getOrDefault(emptyList())
                DisplayState(movies = movieList)

            } else {
                val error = result.exceptionOrNull() as Exception
                ErrorState(error)
            }
        }
    }

    class ErrorState(val exception: Exception): MovieListState() {

        override fun handle(action: Action): MovieListState {

            return when (action) {
                is Action.LoadInitial -> {
                    loadInitial()
                }
                else -> {
                    throw IllegalArgumentException("$this cannot handle $action")
                }
            }
        }

        private fun loadInitial(): MovieListState {

            val result = repository.fetchPopularMovies()

            return if (result.isSuccess) {
                val movieList = result.getOrDefault(emptyList())
                DisplayState(movies = movieList)

            } else {
                val error = result.exceptionOrNull() as Exception
                ErrorState(error)
            }
        }
    }

    data class DisplayState(
        val movies: List<Movie>,
    ): MovieListState() {

        override fun handle(action: Action): MovieListState {

            return when (action) {

                is Action.LoadMore -> {
                    loadMore(action)
                }
                else -> {
                    throw IllegalArgumentException("$this cannot handle $action")
                }
            }
        }

        private fun loadMore(action: Action.LoadMore): DisplayState {

            fun onDataRequestSuccess(updates: List<Movie>): DisplayState {
                action.onResult(Result.success(Unit))
                return this.copy(movies = this.movies + updates)
            }

            fun onDataRequestFailure(exception: Exception): DisplayState {
                action.onResult(Result.failure(exception))
                return this
            }

            val nextPage = this.movies.count().div(MOVIE_LIST_PAGE_SIZE).inc()

            val newDataRequest = repository.fetchPopularMovies(nextPage)

            return if (newDataRequest.isSuccess) {
                val updates = newDataRequest.getOrDefault(emptyList())
                onDataRequestSuccess(updates)
            }
            else {
                val exception: Exception = (newDataRequest.exceptionOrNull() as Exception?)
                    ?: Exception("Something went wrong")
                onDataRequestFailure(exception)
            }
        }
    }

    companion object {
        const val MOVIE_LIST_PAGE_SIZE = 20
    }
}