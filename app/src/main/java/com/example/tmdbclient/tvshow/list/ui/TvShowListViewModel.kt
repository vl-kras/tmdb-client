package com.example.tmdbclient.tvshow.list.ui

import androidx.lifecycle.ViewModel
import com.example.tmdbclient.shared.ServiceLocator
import com.example.tmdbclient.tvshow.list.domain.TvShowListRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class TvShowListViewModel : ViewModel() {

    private val ioDispatcher = Dispatchers.IO

    val state: MutableStateFlow<TvShowListState> = MutableStateFlow(TvShowListState.InitialLoading)

    fun getState(): StateFlow<TvShowListState> = state

    suspend fun handleAction(action: TvShowListState.Action) {
        state.update { state ->
            withContext(ioDispatcher) {
                state.handle(action)
            }
        }
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
            return "Display, error -> ${this.error}, more? -> ${this.canLoadMore}"
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