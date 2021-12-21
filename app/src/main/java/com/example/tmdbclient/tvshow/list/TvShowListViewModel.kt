package com.example.tmdbclient.tvshow.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tmdbclient.ServiceLocator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TvShowListViewModel : ViewModel() {

    private val observableState = MutableLiveData<TvShowListState>(TvShowListState.Initial)

    fun getState(): LiveData<TvShowListState> = observableState

    fun handleAction(action: TvShowListState.Action) {

        viewModelScope.launch(Dispatchers.IO) {

            val oldState = observableState.value!!
            observableState.postValue(TvShowListState.Loading)

            val newState = oldState.handle(action)
            observableState.postValue(newState)
        }
    }
}

sealed class TvShowListState {

    sealed class Action {
        object Load: Action()
    }

    protected val repository = TvShowListRepository(ServiceLocator.tvShowListBackend)

    abstract fun handle(action: Action): TvShowListState

    object Initial: TvShowListState() {

        override fun handle(action: Action): TvShowListState {

            return when(action) {
                is Action.Load -> {
                    Display(tvShowList = repository.fetchPopularMovies())
                }
                else -> {
                    throw IllegalArgumentException("$this cannot handle $action")
                }
            }
        }
    }

    object Loading: TvShowListState() {

        override fun handle(action: Action) = this
    }

    data class Display(val tvShowList: List<TvShowListRepository.TvShow>): TvShowListState() {

        override fun handle(action: Action) = this
    }

    data class Error(val exception: Exception): TvShowListState() {

        override fun handle(action: Action): TvShowListState {

            return when(action) {
                is Action.Load -> {
                    Display(tvShowList = repository.fetchPopularMovies())
                }
                else -> {
                    throw IllegalArgumentException("$this cannot handle $action")
                }
            }
        }
    }
}