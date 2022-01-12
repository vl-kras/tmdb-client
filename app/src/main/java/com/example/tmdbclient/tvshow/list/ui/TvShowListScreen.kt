package com.example.tmdbclient.tvshow.list.ui

import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.example.tmdbclient.shared.TmdbBasePaths.TMDB_POSTER_W300_DIRECTORY
import com.example.tmdbclient.tvshow.list.domain.TvShowListRepository
import kotlinx.coroutines.launch

@Composable
fun TvShowListScreen(
    navController: NavController

) {
    val viewModel: TvShowListViewModel = viewModel()
    val uiState by viewModel.getState().collectAsState()

    when (uiState) {
        is TvShowListState.InitialLoading -> {
            LoadingState()
        }
        is TvShowListState.Display -> {
            DisplayState(state = uiState as TvShowListState.Display, navController = navController)
        }
        is TvShowListState.Error -> {
            ErrorState(state = uiState as TvShowListState.Error)
        }
    }
}

@Composable
fun LoadingState() {

    LoadingIndicator()

    val viewModel: TvShowListViewModel = viewModel()

    LaunchedEffect(key1 = Unit) {

        val action = TvShowListState.Action.Load
        viewModel.handleAction(action)
    }
}

@Composable
fun DisplayState(state: TvShowListState.Display, navController: NavController) {

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

    LazyColumn {
        items(state.content.chunked(columnCount)) { list ->
            Row {
                list.forEach { show ->
                    ContentItem(show = show, navController = navController, modifier = Modifier.weight(1f))
                }

            }
        }
        item {
            ListFooter(state.error, isLoadingMore)
        }
    }

    SnackbarHost(hostState = snackbarHostState)

//    AnimatedVisibility(visible = showButton) {
//        Box {
//            Button(
//                onClick = {
//                    coroutineScope.launch {
//                        listState.animateScrollToItem(index = 0)
//                    }
//                },
//                modifier = Modifier.align(Alignment.BottomEnd)
//            ) {
//                Text(text = "Scroll to top")
//            }
//        }
//    }
}

@Composable
fun ContentItem(show: TvShowListRepository.TvShow, navController: NavController, modifier: Modifier) {

    val onShowClick = {
        navController.navigate("tv/${show.id}")
    }

        Column(modifier = modifier
            .padding(2.dp)
            .border(BorderStroke(1.dp, Color.LightGray))
            .clickable(enabled = true, onClick = onShowClick)) {
            Image(
                painter = rememberImagePainter(data = TMDB_POSTER_W300_DIRECTORY + show.posterPath),
                contentDescription = "TV Show Poster",
                modifier = Modifier.aspectRatio(ratio =0.66f)
            )
            Text(
                text = show.title,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
            )
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
        LoadingIndicator()
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

@Composable
fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}