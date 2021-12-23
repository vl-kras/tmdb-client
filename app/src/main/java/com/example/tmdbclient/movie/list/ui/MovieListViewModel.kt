package com.example.tmdbclient.movie.list.ui

import android.util.Log
import androidx.lifecycle.*
import com.example.tmdbclient.shared.ServiceLocator
import com.example.tmdbclient.shared.TmdbBasePaths.MOVIE_LIST_PAGE_SIZE
import com.example.tmdbclient.movie.list.logic.MovieListRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.UnknownHostException

class MovieListViewModel : ViewModel() {

    private val ioDispatcher = Dispatchers.IO

    private val observableState = MutableLiveData<MovieListState>(MovieListState.Initial(isInTransition = false))

    fun getMovies(): LiveData<MovieListState> = observableState

    fun handleAction(action: MovieListState.Action) {

        viewModelScope.launch {
            val oldState = observableState.value!!
            observableState.postValue(MovieListState.Loading)
            val newState: MovieListState = withContext(ioDispatcher) {
                oldState.handle(action)
            }
            observableState.postValue(newState)
        }
    }
}

sealed class MovieListState {

    protected val repository = MovieListRepository(
        backend = ServiceLocator.movieListBackend
    )

    sealed class Action {
        object LoadInitial: Action()
        object LoadMore: Action()
    }

    abstract fun handle(action: Action) : MovieListState

    data class Initial(val isInTransition: Boolean): MovieListState() {

        override fun handle(action: Action): MovieListState {

            return when (action) {
                is Action.LoadInitial -> {
                    if (isInTransition) {
                        //TODO refactor loading state into something else
                        this.copy(isInTransition = false)

                    }
                    try {
                        Display(movies = repository.fetchPopularMovies())
                    }
                    catch (e: UnknownHostException) {
                        Error(e)
                    }
                }
                else -> {
                    throw IllegalArgumentException("$this cannot handle $action")
                }
            }
        }
    }

    object Loading: MovieListState() {

        override fun handle(action: Action): MovieListState = this
    }

    data class Display(val movies: List<MovieListRepository.Movie>): MovieListState() {

        override fun handle(action: Action): MovieListState {

            return when (action) {
                is Action.LoadMore -> {
                    Log.d("BLABLA", "COPYING STATE")
                    val nextPage = this.movies.count().div(MOVIE_LIST_PAGE_SIZE).plus(1)
                    val updates = repository.fetchPopularMovies(nextPage)
                    Display(movies = this.movies + updates)
                }
                else -> {
                    throw IllegalArgumentException("$this cannot handle $action")
                }
            }
        }
    }

    data class Error(val exception: Exception): MovieListState() {

        override fun handle(action: Action): MovieListState {

            return when (action) {
                is Action.LoadInitial -> {
                    try {
                        Display(movies = repository.fetchPopularMovies())
                    }
                    catch (e: UnknownHostException) {
                        Error(e)
                    }
                }
                else -> {
                    throw IllegalArgumentException("$this cannot handle $action")
                }
            }
        }
    }
}