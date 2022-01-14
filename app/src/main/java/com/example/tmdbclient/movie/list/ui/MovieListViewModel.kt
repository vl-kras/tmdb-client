package com.example.tmdbclient.movie.list.ui

import android.util.Log
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

    private val state: MutableStateFlow<MovieListState> = MutableStateFlow(MovieListState.InitialState())
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

    protected val interactor = MovieListInteractor(
        dataSource = ServiceLocator.getMovieListInteractorDataSource()
    )

    abstract fun handle(action: Action) : MovieListState

    class InitialState: MovieListState() {

        override fun handle(action: Action): MovieListState {

            return when (action) {
                is Action.LoadInitial -> {
                    loadInitial()
                }
                else -> {
                    Log.e(this.javaClass.name, "$this cannot handle $action")
                    this
                }
            }
        }

        private fun loadInitial(): MovieListState {

            return interactor.fetchPopularMovies().fold(
                onSuccess = { movieList ->
                    DisplayState(movies = movieList)
                },
                onFailure = { throwable ->
                    ErrorState(throwable as Exception)
                }
            )
        }
    }

    class ErrorState(val exception: Exception): MovieListState() {

        override fun handle(action: Action): MovieListState {

            return when (action) {
                is Action.LoadInitial -> {
                    loadInitial()
                }
                else -> {
                    Log.e(this.javaClass.name, "$this cannot handle $action")
                    this
                }
            }
        }

        private fun loadInitial(): MovieListState {

            return interactor.fetchPopularMovies().fold(
                onSuccess = { movieList ->
                    DisplayState(movies = movieList)
                },
                onFailure = { throwable ->
                    ErrorState(throwable as Exception)
                }
            )
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
                    Log.e(this.javaClass.name, "$this cannot handle $action")
                    this
                }
            }
        }

        private fun loadMore(action: Action.LoadMore): DisplayState {

            val nextPage = this.movies.count().div(MOVIE_LIST_PAGE_SIZE).inc()

            return interactor.fetchPopularMovies(nextPage).fold(
                onSuccess = { updates ->
                    action.onResult(Result.success(Unit))
                    this.copy(movies = this.movies + updates)
                },
                onFailure = { throwable ->
                    action.onResult(Result.failure(throwable as Exception))
                    this
                }
            )
        }
    }

    companion object {
        const val MOVIE_LIST_PAGE_SIZE = 20
    }
}