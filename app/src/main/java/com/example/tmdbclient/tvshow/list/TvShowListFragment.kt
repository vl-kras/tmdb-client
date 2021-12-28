package com.example.tmdbclient.tvshow.list

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.tmdbclient.shared.TmdbBasePaths.TMDB_POSTER_W300
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tmdbclient.shared.theme.MyApplicationTheme
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.launch

class TvShowListFragment : Fragment() {

    //TODO switch to ui-domain-data package structure

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


    @Composable
    fun LoadingState() {

        CircularProgressIndicator()

        val viewModel: TvShowListViewModel = viewModel()

        LaunchedEffect(key1 = Unit) {

            val action = TvShowListState.Action.Load
            viewModel.handleAction(action)
        }
    }

    @ExperimentalMaterialApi
    @ExperimentalFoundationApi
    @Composable
    fun DisplayState(state: TvShowListState.Display) {

        val viewModel: TvShowListViewModel = viewModel()

        val listState = rememberLazyListState() // ???

        val snackbarHostState = remember { SnackbarHostState() }

        val coroutineScope = rememberCoroutineScope()

        val error by remember { mutableStateOf(state.error) }

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

        val columnCount = 2

        var run by remember { mutableStateOf(false) }
        if((listState.firstVisibleItemIndex > state.content.size.div(columnCount).minus(5)) and (state.error != null)) {
            run = true
        }

        var isLoadingMore: Boolean by remember { mutableStateOf(false) }

        Log.d("BLABLA", "Index -> ${listState.firstVisibleItemIndex}, Size -> ${state.content.size}, Should load more -> $run")
        if(run) {

            SideEffect {


                coroutineScope.launch {
                    isLoadingMore = true
                    val action = TvShowListState.Action.LoadMore
                    viewModel.handleAction(action)
                    isLoadingMore = false
                }

            }

//            run = false
        }


        LazyVerticalGrid(state = listState, cells = GridCells.Fixed(columnCount)) {
            items(state.content) { show ->
                ContentItem(show)
            }
            item {
                ListFooter(state.error, isLoadingMore)

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
                GlideImage(
                    imageModel = TMDB_POSTER_W300 + show.posterPath,
                )
                Text(
                    text = show.title,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                )
            }
        }
    }

    @Composable
    fun ListFooter(error: Exception?, isLoadingMore: Boolean) {

        Log.d("FOOTER", "Loading more -> $isLoadingMore")

        val coroutineScope = rememberCoroutineScope()

//        var isLoadingMore: Boolean by remember { mutableStateOf(false) }

        val viewModel: TvShowListViewModel = viewModel()

        if (isLoadingMore.not()) {

            Column(modifier = Modifier.fillMaxWidth()) {
                val buttonText: String = if (error != null) {
                    Text(text = "${error.message}") //  <------ !!!
                    "Retry"
                } else {
                    "Load more"
                }
                Button(
                    onClick = {
                        coroutineScope.launch {
//                            isLoadingMore = true
                            val action = TvShowListState.Action.LoadMore
                            viewModel.handleAction(action)
//                            isLoadingMore = false
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(text = buttonText)
                }
            }
        } else {
            CircularProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            )
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

