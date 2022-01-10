package com.example.tmdbclient.movie.list.ui

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
import com.example.tmdbclient.movie.list.domain.MovieListRepository
import com.example.tmdbclient.profile.ui.ProfileViewModel
import com.example.tmdbclient.shared.TmdbBasePaths
import kotlinx.coroutines.launch

@Composable
fun MovieListScreen(
    profileVM: ProfileViewModel,
    navController: NavController
) {

    val viewModel: MovieListViewModel = viewModel()

    val state by viewModel.getState().collectAsState()

    when (state) {
        is InitialState -> {
            LoadingState()
        }
        is Display -> {
            DisplayState(state = state as Display, profileVM = profileVM, navController = navController)
        }
        is Error -> {
            ErrorState(state = state as Error)
        }
    }
}

@Composable
fun LoadingState() {

    LoadingIndicator()

    val viewModel: MovieListViewModel = viewModel()

    LaunchedEffect(key1 = Unit) {

        val action = MovieListState.Action.LoadInitial
        viewModel.handleAction(action)
    }
}

@Composable
fun DisplayState(state: Display, profileVM: ProfileViewModel, navController: NavController) {

    val viewModel: MovieListViewModel = viewModel()

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
    if((listState.firstVisibleItemIndex > state.movies.size.div(columnCount).minus(5)) and (state.error != null)) {
        run = true
    }

    var isLoadingMore: Boolean by remember { mutableStateOf(false) }

    Log.d("BLABLA", "Index -> ${listState.firstVisibleItemIndex}, Size -> ${state.movies.size}, Should load more -> $run")
    if(run) {

        SideEffect {


            coroutineScope.launch {
                isLoadingMore = true
                val action = MovieListState.Action.LoadMore
                viewModel.handleAction(action)
                isLoadingMore = false
            }

        }

//            run = false
    }

    LazyColumn {
        items(state.movies.chunked(columnCount)) { list ->
            Row {
                list.forEach { movie ->
                    ContentItem(movie = movie, navController = navController, modifier = Modifier.weight(1f))
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
fun ContentItem(
    movie: MovieListRepository.Movie,
    navController: NavController,
    modifier: Modifier
) {

    val onMovieClick = {
        navController.navigate("movie/${movie.id}")
    }

    Column(modifier = modifier
        .padding(2.dp)
        .border(BorderStroke(1.dp, Color.LightGray))
        .clickable(enabled = true, onClick = onMovieClick)) {

        Image(
            painter = rememberImagePainter(data = TmdbBasePaths.TMDB_POSTER_W300 + movie.posterPath),
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
fun ListFooter(error: Exception?, isLoadingMore: Boolean) {

    Log.d("FOOTER", "Loading more -> $isLoadingMore")

    val coroutineScope = rememberCoroutineScope()

//        var isLoadingMore: Boolean by remember { mutableStateOf(false) }

    val viewModel: MovieListViewModel = viewModel()

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
                        val action = MovieListState.Action.LoadMore
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
fun ErrorState(state: Error) {

    val coroutineScope = rememberCoroutineScope()

    val viewModel: MovieListViewModel = viewModel()

    Column {
        Text(
            text = state.exception.message
                ?: "Something went wrong"
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