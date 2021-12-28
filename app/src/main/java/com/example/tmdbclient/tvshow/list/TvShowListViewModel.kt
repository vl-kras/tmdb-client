package com.example.tmdbclient.tvshow.list

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tmdbclient.shared.ServiceLocator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class TvShowListViewModel : ViewModel() {

    private val ioDispatcher = Dispatchers.IO

    private val observableState = MutableLiveData<TvShowListState>(TvShowListState.InitialLoading)

    fun getState(): LiveData<TvShowListState> = observableState

    suspend fun handleAction(action: TvShowListState.Action) {

        val oldState = observableState.value!!
        val newState: TvShowListState = withContext(ioDispatcher) {
            oldState.handle(action)
        }
        Log.d("STATE", newState.toString())

        observableState.postValue(newState)
    }
}

sealed class TvShowListState {

    sealed class Action {
        object Load: Action()
        object LoadMore: Action()
    }

    protected val repository = TvShowListRepository(ServiceLocator.tvShowListBackend)

    abstract fun handle(action: Action): TvShowListState

    object InitialLoading: TvShowListState() {

        override fun handle(action: Action): TvShowListState {

            return when(action) {
                is Action.Load -> {
                    try {
                        Display(content = repository.fetchPopularShows())
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
        val content: List<TvShowListRepository.TvShow> = emptyList(),
        val error: Exception? = null,
        val canLoadMore: Boolean = true
    ): TvShowListState() {

        override fun toString(): String {
            return " Display, error -> ${this.error}, more? -> ${this.canLoadMore}"
        }

        override fun handle(action: Action): TvShowListState {

            return when(action) {
                is Action.LoadMore -> {
                    try {
                        val nextPage = this.content.size.div(20).plus(1)
                        val updates = repository.fetchPopularShows(nextPage)
                        this.copy(
                            content = this.content + updates,
                            error = null
                        )
                    }
                    catch (e: IOException) {
                        this.copy(error = e, canLoadMore = false)
                    }
                    catch (e: UnknownHostException) {
                        this.copy(error = e)
                    }
                    catch (e: SocketTimeoutException) {
                        this.copy(error = e)
                    }
                }
                else -> {
                    throw IllegalArgumentException("$this cannot handle $action")
                }
            }
        }
    }

    data class Error(val exception: Exception): TvShowListState() {

        override fun handle(action: Action): TvShowListState {

            return when(action) {
                is Action.Load -> {
                    try {
                        Display(content = repository.fetchPopularShows())
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