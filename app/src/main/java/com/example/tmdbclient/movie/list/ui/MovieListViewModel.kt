package com.example.tmdbclient.movie.list.ui

import androidx.lifecycle.*
import com.example.tmdbclient.shared.ServiceLocator
import com.example.tmdbclient.shared.TmdbBasePaths.MOVIE_LIST_PAGE_SIZE
import com.example.tmdbclient.movie.list.logic.MovieListRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class MovieListViewModel : ViewModel() {

    private val ioDispatcher = Dispatchers.IO

    private val observableState = MutableLiveData<MovieListState>(InitialState())


    fun getMovies(): LiveData<MovieListState> = observableState

    suspend fun handleAction(action: MovieListState.Action) {

            val oldState = observableState.value!!

            val newState: MovieListState = withContext(ioDispatcher) {
                oldState.handle(action)
            }
            observableState.postValue(newState)
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
}

class InitialState: MovieListState() {


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

data class Display(
    val movies: List<MovieListRepository.Movie>,
    val error: Exception? = null, //i'd rather use optional
): MovieListState() {

    override fun handle(action: Action): MovieListState {

        return when (action) {

            is Action.LoadMore -> {
                try {
                    val nextPage = this.movies.count().div(MOVIE_LIST_PAGE_SIZE).inc()
                    val updates = repository.fetchPopularMovies(nextPage)

                    this.copy(movies = this.movies + updates)
                }
                catch (e: UnknownHostException) {

                    this.copy(error = e)
                }
                catch (e: SocketTimeoutException) {

                    this.copy(error = e)
                }
            }

            else -> {
                this.copy(error = Exception("This action is not allowed"))
            }
        }
    }
}