package com.example.tmdbclient.movie

import android.util.Log
import androidx.lifecycle.*
import com.example.tmdbclient.Backend
import com.example.tmdbclient.Movie
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

//TODO implement exception handling!

class MovieListViewModel : ViewModel() {


    private val ioDispatcher = Dispatchers.IO

    private val movies = MutableLiveData<MovieListState>(MovieListState.Initial)

    fun getMovies(): LiveData<MovieListState> = movies

    fun handleAction(action: MovieListState.Action) {
        try {
            movies.postValue(MovieListState.Loading)

            viewModelScope.launch {
                val newState: MovieListState = withContext(ioDispatcher) {

                    movies.value!!.handle(action)

                }
                movies.postValue(newState)
            }
//            movies.postValue(MovieListState.Loading)
//            val newState = withContext(ioDispatcher) {
//                movies.value!!.handle(action)
//            }
//            movies.postValue(newState)
        }
        catch (e: IOException) {
            movies.postValue(MovieListState.Error(e))
        }
        catch (e: SocketTimeoutException) {
            movies.postValue(MovieListState.Error(e))
        }
        catch (e: UnknownHostException) {
            movies.postValue(MovieListState.Error(e))
        }
    }
}

sealed class MovieListState {

    protected val backend = Backend

    sealed class Action {
        object LoadMovies: Action()
        object Retry: Action()
    }

    abstract fun handle(action: Action) : MovieListState

    object Initial: MovieListState() {

        override fun handle(action: Action): MovieListState {

            return when (action) {
                is Action.LoadMovies -> {
                    Display(movies = backend.getPopularMoviesByPage())
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

    class Display(val movies: List<Movie>): MovieListState() {

        override fun handle(action: Action): MovieListState = this
    }

    class Error(val exception: Exception): MovieListState() {

        override fun handle(action: Action): MovieListState {

            return when (action) {
                is Action.Retry -> {
                    Display(movies = backend.getPopularMoviesByPage())
                }
                else -> {
                    throw IllegalArgumentException("$this cannot handle $action")
                }
            }
        }
    }
}