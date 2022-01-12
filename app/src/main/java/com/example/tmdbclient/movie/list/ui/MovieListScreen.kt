package com.example.tmdbclient.movie.list.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.example.tmdbclient.movie.list.domain.Movie
import com.example.tmdbclient.shared.TmdbBasePaths
import kotlinx.coroutines.launch

@Composable
fun MovieListScreen(navController: NavController) {

    val viewModel: MovieListViewModel = viewModel()
    val state by viewModel.getState().collectAsState()

    when (state) {
        is MovieListState.InitialState -> {
            InitialState()
        }
        is MovieListState.DisplayState -> {
            DisplayState(
                state = state as MovieListState.DisplayState,
                navController = navController
            )
        }
        is MovieListState.ErrorState -> {
            ErrorState(state = state as MovieListState.ErrorState)
        }
    }
}

@Composable
fun InitialState() {

    val viewModel: MovieListViewModel = viewModel()

    //start loading first page
    LaunchedEffect(key1 = Unit) {
        val action = MovieListState.Action.LoadInitial
        viewModel.handleAction(action)
    }

    LoadingIndicator()
}

@Composable
fun DisplayState(state: MovieListState.DisplayState, navController: NavController) {

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val isUpButtonVisible by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0
        }
    }

    val onFabClick: () -> Unit = {
        coroutineScope.launch {
            listState.animateScrollToItem(index = 0) //scroll to the top
        }
    }

    Scaffold(
        floatingActionButton = {
            if (isUpButtonVisible) {
                FloatingActionButton(onClick = onFabClick) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Go back"
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) {
        MovieList(listState, state.movies, navController)
    }
}

@Composable
fun MovieList(listState: LazyListState, listContents: List<Movie>, navController: NavController) {

    val columnCount = 2 // 1 for vertical list, 2+ for grid list

    var isLoadingMore by remember { mutableStateOf(false) }
    var loadingResult: Result<Unit> by remember { mutableStateOf(Result.success(Unit)) }

    LazyColumn(state = listState) {
        items(listContents.chunked(columnCount)) { list ->
            Row {
                list.forEach { movie ->
                    MovieListItem(
                        movie = movie,
                        navController = navController,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        item {
            ListFooter(
                isLoadingMore, { isLoadingMore = it },
                loadingResult, { loadingResult = it }
            )
        }
    }
}

@Composable
fun MovieListItem(
    movie: Movie,
    navController: NavController,
    modifier: Modifier
) {

    val onMovieClick = {
        navController.navigate("movie/${movie.id}")
    }

    Column(modifier = modifier
        .padding(2.dp)
        .border(BorderStroke(1.dp, Color.LightGray))
        .clickable(enabled = true, onClick = onMovieClick)
    ) {
        val posterUrl = TmdbBasePaths.TMDB_POSTER_W300_DIRECTORY + movie.posterPath
        Image(
            painter = rememberImagePainter(data = posterUrl),
            contentDescription = "Movie Poster",
            modifier = Modifier.aspectRatio(ratio =0.66f)
        )
        Text(
            text = movie.title,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun ListFooter(
    isLoading: Boolean, setLoadingStatus: (Boolean) -> Unit,
    loadingResult: Result<Unit>, setLoadingResult: (Result<Unit>) -> Unit
) {

    val coroutineScope = rememberCoroutineScope()
    val viewModel: MovieListViewModel = viewModel()

    //action to load next page
    val loadMore: () -> Unit = {
        coroutineScope.launch {
            setLoadingStatus(true)
            val action = MovieListState.Action.LoadMore(
                onResult = { setLoadingResult(it) }
            )
            viewModel.handleAction(action)
        }.invokeOnCompletion {
            setLoadingStatus(false)
        }
    }

    //auto-pagination, load more when footer becomes visible (reached the end of the list)
    SideEffect {
        loadMore.invoke()
    }

    //actual footer
    if (!isLoading) {
        NotLoadingFooter(loadMore, loadingResult)
    } else {
        LoadingIndicator()
    }
}

@Composable
fun NotLoadingFooter(onButtonClick: () -> Unit , loadingResult: Result<Unit>) {

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val buttonText: String =
            if (loadingResult.isSuccess) {
                "Load more"
            } else {
                Text(text = loadingResult.exceptionOrNull()?.message ?: "Something went wrong")
                "Retry"
            }
        Button(
            onClick = onButtonClick,
        ) {
            Text(text = buttonText)
        }
    }
}

@Composable
fun ErrorState(state: MovieListState.ErrorState) {

    val coroutineScope = rememberCoroutineScope()
    val viewModel: MovieListViewModel = viewModel()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = state.exception.message ?: "Something went wrong",
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Button(
            onClick = {
                coroutineScope.launch {
                    val action = MovieListState.Action.LoadInitial
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