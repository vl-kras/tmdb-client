package com.example.tmdbclient.tvshow.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.tmdbclient.shared.TmdbBasePaths.TMDB_POSTER_W300
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tmdbclient.shared.theme.MyApplicationTheme
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.launch

class TvShowListFragment : Fragment() {

    @ExperimentalFoundationApi
    @ExperimentalMaterialApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply {
            setContent {
                MyApplicationTheme {
                    TvShowList()
                }
            }
        }
    }

    @ExperimentalFoundationApi
    @ExperimentalMaterialApi
    @Composable
    fun TvShowList(
        viewModel: TvShowListViewModel = viewModel()
    ) {
        val uiState by viewModel.getState().observeAsState()

        when (uiState) {
            is TvShowListState.InitialLoading -> {
                LoadingState()
            }
            is TvShowListState.Display -> {
                DisplayState(state = uiState as TvShowListState.Display)
            }
            is TvShowListState.Error -> {
                ErrorState(state = uiState as TvShowListState.Error)
            }
        }
    }

    /* TODO refactor model into single class type
     * if (hasContent)
     *     show content
     *     if (hasError)
     *         showError as snackbar
     * else
     *     if (hasError)
     *         showError as text + retryButton
     *     else
     *        startLoadingItems (cause it's initial state)
     *
     */

    @Composable
    fun LoadingState() {

        val viewModel: TvShowListViewModel = viewModel()

        LaunchedEffect(key1 = "") {

                val action = TvShowListState.Action.Load
                viewModel.handleAction(action)

        }

        CircularProgressIndicator(modifier = Modifier.defaultMinSize())
    }

    @ExperimentalMaterialApi
    @ExperimentalFoundationApi
    @Composable
    fun DisplayState(state: TvShowListState.Display) {

        val viewModel: TvShowListViewModel = viewModel()

        LaunchedEffect(key1 = "") {

            val action = TvShowListState.Action.Load
            viewModel.handleAction(action)

        }

        val listState = rememberLazyListState() // ???

        val snackbarHostState = remember { SnackbarHostState() }

        val coroutineScope = rememberCoroutineScope()

        val error = state.error

        state.error?.let {
            LaunchedEffect(key1 = state.error) {
                snackbarHostState.showSnackbar(
                    message = it.message ?: "Something went wrong"
                )
            }
        }

        val showButton by remember {
            derivedStateOf {
                listState.firstVisibleItemIndex > 0
            }
        }

        //TODO replace with lazyColumn styled as grid

        LazyVerticalGrid(state = listState, cells = GridCells.Fixed(2)) {
            items(state.content) { show ->
                ContentItem(show)
            }
            item {
                ListFooter()
            }
        }
        SnackbarHost(hostState = snackbarHostState)

        AnimatedVisibility(visible = showButton) {
            Box {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            listState.animateScrollToItem(index = 0)
                        }
                    },
                    modifier = Modifier.align(Alignment.BottomEnd)
                ) {
                    Text(text = "Scroll to top")
                }
            }
        }
    }

    @ExperimentalMaterialApi
    @Composable
    fun ContentItem(show: TvShowListRepository.TvShow) {

        val onShowClick = {
            val action = TvShowListFragmentDirections.showTvshowDetails(show.id)
            findNavController().navigate(action)
        }

        Card(onClick = onShowClick, modifier = Modifier.padding(4.dp)) {
            Column {
                LaunchedEffect(key1 = show) {
                    launch {  }
                }
                GlideImage(imageModel = TMDB_POSTER_W300 + show.posterPath)
                Text(text = show.title, modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }
    }

    @Composable
    fun ListFooter() {

        val coroutineScope = rememberCoroutineScope()

        var isLoadingMore: Boolean? by remember { mutableStateOf(false) }

        isLoadingMore?.let { isLoading ->
            if (isLoading.not()) {
                val viewModel: TvShowListViewModel = viewModel()
                Button(
                    onClick = {
                        coroutineScope.launch {
                            isLoadingMore = true
//                        delay(1000)
                            val action = TvShowListState.Action.LoadMore
                            viewModel.handleAction(action)
                            isLoadingMore = false
                        }

                    }
                ) {
                    Text(text = "Load more")
                }
            }
            else {
                LinearProgressIndicator()
            }
        }
    }

    @Composable
    fun ErrorState(state: TvShowListState.Error) {

        val coroutineScope = rememberCoroutineScope()


        val viewModel: TvShowListViewModel = viewModel()

        Column {
            Text(
                text = state.exception.message
                    ?: "Something went wrong"
            )
            Button(
                onClick = {
                    coroutineScope.launch {
                        val action = TvShowListState.Action.Load
                        viewModel.handleAction(action)
                    }

                }
            ) {
                Text(text = "Try again")
            }
        }
    }
}

