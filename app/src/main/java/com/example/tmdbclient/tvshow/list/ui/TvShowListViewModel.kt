package com.example.tmdbclient.tvshow.list.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.tmdbclient.shared.ServiceLocator
import com.example.tmdbclient.tvshow.list.domain.TvShow
import com.example.tmdbclient.tvshow.list.domain.TvShowListInteractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

class TvShowListViewModel : ViewModel() {

    private val ioDispatcher = Dispatchers.IO

    val state: MutableStateFlow<TvShowListState> = MutableStateFlow(TvShowListState.InitialState)

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
        class LoadMore(val onResult: (Result<Unit>) -> Unit): Action()
    }

    protected val interactor = TvShowListInteractor(
        dataSource = ServiceLocator.getTvShowListInteractorDataSource()
    )

    abstract fun handle(action: Action): TvShowListState

    object InitialState: TvShowListState() {

        override fun handle(action: Action): TvShowListState {

            return when(action) {
                is Action.Load -> {
                    loadInitial()
                }
                else -> {
                    Log.e(this.javaClass.name, "$this cannot handle $action")
                    this
                }
            }
        }

        private fun loadInitial(): TvShowListState {

            return interactor.fetchPopularShows()
                .fold(
                    onSuccess = { showList ->
                        DisplayState(shows = showList)
                    },
                    onFailure = { throwable ->
                        ErrorState(throwable as Exception)
                    }
                )
        }
    }

    data class DisplayState(
        val shows: List<TvShow> = emptyList(),
        val canLoadMore: Boolean = true
    ): TvShowListState() {

        override fun handle(action: Action): TvShowListState {

            return when(action) {
                is Action.LoadMore -> {
                    loadMoreShows(action)
                }
                else -> {
                    Log.e(this.javaClass.name, "$this cannot handle $action")
                    this
                }
            }
        }

        private fun loadMoreShows(action: Action.LoadMore): TvShowListState {

            val nextPage = this.shows.size.div(TV_SHOW_LIST_PAGE_SIZE).plus(1)

            return interactor.fetchPopularShows(nextPage).fold(
                onSuccess = { updates ->
                    action.onResult(Result.success(Unit))
                    this.copy(shows = this.shows + updates)
                },
                onFailure = { throwable ->
                    action.onResult(Result.failure(throwable as Exception))
                    this
                }
            )
        }
    }

    data class ErrorState(val exception: Exception): TvShowListState() {

        override fun handle(action: Action): TvShowListState {

            return when(action) {
                is Action.Load -> {
                    loadInitial()
                }
                else -> {
                    throw IllegalArgumentException("$this cannot handle $action")
                }
            }
        }

        private fun loadInitial(): TvShowListState {

            return interactor.fetchPopularShows()
                .fold(
                    onSuccess = { showList ->
                        DisplayState(shows = showList)
                    },
                    onFailure = { throwable ->
                        ErrorState(throwable as Exception)
                    }
                )
        }
    }

    companion object {
        const val TV_SHOW_LIST_PAGE_SIZE = 20
    }
}