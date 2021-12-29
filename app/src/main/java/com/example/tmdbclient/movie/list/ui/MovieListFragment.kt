package com.example.tmdbclient.movie.list.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.navArgument
import coil.compose.rememberImagePainter
import com.example.tmdbclient.movie.details.MovieDetailsScreen
import com.example.tmdbclient.movie.list.logic.MovieListRepository
import com.example.tmdbclient.profile.ProfileScreen
import com.example.tmdbclient.profile.ProfileViewModel
import com.example.tmdbclient.shared.Screen
import com.example.tmdbclient.shared.TmdbBasePaths
import com.example.tmdbclient.shared.theme.MyApplicationTheme
import com.example.tmdbclient.tvshow.list.TvShowList
import com.example.tmdbclient.tvshow.list.TvShowListFragmentDirections
import com.example.tmdbclient.tvshow.list.TvShowListState
import com.example.tmdbclient.tvshow.list.TvShowListViewModel
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.launch

class MovieListFragment : Fragment() {

    @ExperimentalMaterialApi
    @ExperimentalFoundationApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply {
            setContent {
                MyApplicationTheme {
//                    MovieList()
                }
            }
        }
    }
}

sealed class MovieScreen(val route: String, val label: String) {
    object List: MovieScreen("movielist", "Movie List")
    object Details: MovieScreen("moviedetails", "Movie Details")
}

@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
fun MovieList(
    profileVM: ProfileViewModel
) {

    val viewModel: MovieListViewModel = viewModel()




    val uiState by viewModel.getMovies().observeAsState()

    when (uiState) {
        is InitialState -> {
            LoadingState()
        }
        is Display -> {
            DisplayState(state = uiState as Display, profileVM = profileVM)
        }
        is Error -> {
            ErrorState(state = uiState as Error)
        }
    }
}


@Composable
fun LoadingState() {

    CircularProgressIndicator()

    val viewModel: MovieListViewModel = viewModel()

    LaunchedEffect(key1 = Unit) {

        val action = MovieListState.Action.LoadInitial
        viewModel.handleAction(action)
    }
}

@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun DisplayState(state: Display, profileVM: ProfileViewModel) {

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


    LazyVerticalGrid(state = listState, cells = GridCells.Fixed(columnCount)) {
        items(state.movies) { movie ->
            ContentItem(movie, profileVM)
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


@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
fun ContentItem(
    movie: MovieListRepository.Movie,
    profileVM: ProfileViewModel
) {

    // TODO sort out navigation / make multiple backstacks work

    val navController = rememberNavController()
    NavHost(navController, startDestination = MovieScreen.Details.route) {
//        composable(MovieScreen.List.route) { MovieList(profileVM = profileVM) }
        composable("details/{movieId}", arguments = listOf(navArgument("movieId") { type = NavType.IntType })
        {  })
        composable(MovieScreen.Details.route) { MovieDetailsScreen(profileVM = profileVM, movieId = movie.id) }
    }

    val onShowClick = {
        navController.navigate()
        val action = MovieListFragmentDirections.showMovieDetails(movie.id)

        navController.navigate(action)
    }

    Card(onClick = onShowClick, modifier = Modifier.padding(4.dp)) {
        Column {
//                GlideImage(
//                    imageModel = TmdbBasePaths.TMDB_POSTER_W300 + movie.posterPath,
//                )
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
        CircularProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        )
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