package com.example.tmdbclient.movie.list

import androidx.lifecycle.*
import com.example.tmdbclient.ServiceLocator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.UnknownHostException

class MovieListViewModel : ViewModel() {

    private val ioDispatcher = Dispatchers.IO

    private val observableState = MutableLiveData<MovieListState>(MovieListState.Initial)

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
        object LoadMovies: Action()
        object Retry: Action()
    }

    abstract fun handle(action: Action) : MovieListState

    object Initial: MovieListState() {

        override fun handle(action: Action): MovieListState {

            return when (action) {
                is Action.LoadMovies -> {
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

    class Display(val movies: List<MovieListRepository.Movie>): MovieListState() {

        override fun handle(action: Action): MovieListState = this
    }

    class Error(val exception: Exception): MovieListState() {

        override fun handle(action: Action): MovieListState {

            return when (action) {
                is Action.Retry -> {
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