package com.example.tmdbclient.movie.details.ui

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.example.tmdbclient.profile.ui.ProfileState
import com.example.tmdbclient.profile.ui.ProfileViewModel
import com.example.tmdbclient.shared.TmdbBasePaths.TMDB_POSTER_HEIGHT_WIDTH_RATIO
import com.example.tmdbclient.shared.TmdbBasePaths.TMDB_POSTER_ORIGINAL
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch

@Composable
fun MovieDetailsScreen(profileVM: ProfileViewModel, movieId: Int, navController: NavController) {

    val movieDetailsVM: MovieDetailsViewModel = viewModel()

    val state by movieDetailsVM.getState().collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    val coroutineScope = rememberCoroutineScope()

    val channel = Channel<String>()
    SideEffect {
        coroutineScope.launch {
            channel.consumeEach { message ->
                snackbarHostState.showSnackbar(message)
            }
        }
    }

    val onActionResult: (String) -> Unit = {
        coroutineScope.launch {
            channel.send(it)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.popBackStack() }) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Go back")
            }
        },
        floatingActionButtonPosition = FabPosition.Center,

        ) {

        when (state) {
            is MovieDetailsState.Initial -> {
                InitialState(movieId)
            }
            is MovieDetailsState.Error -> {
                ErrorState(state as MovieDetailsState.Error, movieId)
            }
            is MovieDetailsState.Display -> {
                DisplayState(state as MovieDetailsState.Display, profileVM, movieId, onActionResult)
            }
        }
    }
}

@Composable
fun DisplayState(state: MovieDetailsState.Display, profileVM: ProfileViewModel, movieId: Int, onActionResult: (String) -> Unit) {

    val movieDetails = state.content

    Column(Modifier.fillMaxSize()) {
        Text(
            text = movieDetails.title,
            Modifier.align(Alignment.CenterHorizontally)
        )
        Row {
            Image(
                painter = rememberImagePainter(data = TMDB_POSTER_ORIGINAL + movieDetails.posterPath),
                contentDescription = "Movie poster",
                modifier = Modifier
                    .aspectRatio(ratio = TMDB_POSTER_HEIGHT_WIDTH_RATIO)
                    .weight(1f)
            )
            Column(Modifier.weight(1f)) {
                Row {
                    if (movieDetails.isAdult) {
                        Text(text = "18+")
                    }
                    Text(text = formatRuntime(runtime = movieDetails.runtime))
                }
                Text(text = movieDetails.genres.joinToString { it })
                Text(text = "${movieDetails.userScore.times(10)}%")
                Text(text = movieDetails.tagline)

                val profileState = profileVM.getState().collectAsState()

                if (profileState.value is ProfileState.UserState) {
                    var isPostingRating by remember { mutableStateOf(false) }
                    var showRatingDialog by remember { mutableStateOf(false) }
                    if (showRatingDialog) {
                        RatingDialog(
                            visible = { showRatingDialog = it },
                            sessionId = (profileState.value as ProfileState.UserState).sessionId,
                            movieId = movieId,
                            onActionResult = onActionResult,
                            isPostingRating = { isPostingRating = it}
                        )
                    }

                    Button( enabled = !isPostingRating,
                        onClick = { showRatingDialog = true }
                    ) {
                        Row {
                            Icon(imageVector = Icons.Default.Star, contentDescription = "Icon: Star")
                            Text(text = "Give Rating")
                        }
                    }
                }
            }
        }
        Text(text = movieDetails.overview)
    }
}

//TODO learn to use composable context

@Composable
fun formatRuntime(runtime: Int): String {

    val hours = runtime.div(60)
    val minutes = runtime.mod(60)

    return StringBuilder().also {
        if (hours != 0) {
            it.append("${hours}h")
        }
        if (minutes != 0) {
            it.append("${minutes}m")
        }
    }.toString()
}

@Composable
fun RatingDialog(visible: (Boolean) -> Unit, sessionId: String, movieId: Int, onActionResult: (String) -> Unit, isPostingRating: (Boolean) -> Unit) {

    val movieDetailsVM: MovieDetailsViewModel = viewModel()
    val coroutineScope = rememberCoroutineScope()

    val postRating: (Float) -> Unit = { rating ->
        coroutineScope.launch {
            isPostingRating(true)
            val action = when (rating) {
                0f -> MovieDetailsState.Action.DeleteRating(sessionId, movieId, onActionResult)
                else -> MovieDetailsState.Action.PostRating(sessionId, movieId, rating, onActionResult)
            }
            movieDetailsVM.handleAction(action)
        }.invokeOnCompletion {
            isPostingRating(false)
        }
    }

    var rating by remember { mutableStateOf((RATING_MAX + RATING_MIN) / 2 ) } //start with average rating, which is 5f

    val onRatingChange: (Float) -> Unit = { newRating ->
        if ((newRating >= RATING_MIN) and (newRating <= RATING_MAX) and (newRating.mod(RATING_STEP).equals(0f))) {
            rating = newRating
        }
    }
    AlertDialog(
        title = { Text(text = "Select Rating", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
        onDismissRequest = { visible(false) },
        confirmButton = {
            TextButton(
                onClick = {
                    postRating(rating)
                    visible(false)
                }
            ) {
                Text(text = "Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = { visible(false) }) {
                Text(text = "Cancel")
            }
        },
        text = {
            RatingDialogContent(rating, onRatingChange)
        }
    )
}

const val RATING_MAX = 10f
const val RATING_MIN = 0f
const val RATING_STEP = 0.5f

@Composable
fun RatingDialogContent(rating: Float, setRating: (Float)-> Unit) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {

        if (rating != RATING_MIN) {
            DecreaseRatingButton(rating, setRating)
        }

        SelectedRating(rating)

        if (rating != RATING_MAX) {
            IncreaseRatingButton(rating, setRating)
        }
    }
}

@Composable
fun SelectedRating(rating: Float) {
    Text(
        text = if (rating != RATING_MIN) {
            rating.toString()
        } else {
            "Delete rating"
        }
    )
}

@Composable
fun DecreaseRatingButton(rating: Float, setRating: (Float) -> Unit) {
    IconButton(
        onClick = {
            setRating(rating - RATING_STEP)
        }
    ) {
        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Decrease rating")
    }
}

@Composable
fun IncreaseRatingButton(rating: Float, setRating: (Float) -> Unit) {
    IconButton(
        onClick = {
            setRating(rating + RATING_STEP)
        }
    ) {
        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Increase rating")
    }
}

@Composable
fun ErrorState(state: MovieDetailsState.Error, movieId: Int) {

    val movieDetailsVM: MovieDetailsViewModel = viewModel()
    val coroutineScope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(false) }

    val onClick: () -> Unit = {
        coroutineScope.launch {
            isLoading = true
            movieDetailsVM.handleAction(MovieDetailsState.Action.Load(movieId, ::logResult))
            isLoading = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Text(text = state.exception.message ?: "Something went wrong")
            Button(onClick = onClick ) {
                Text(text = "Retry")
            }
        }
    }
}

@Composable
fun InitialState(movieId: Int) {

    val movieDetailsVM: MovieDetailsViewModel = viewModel()
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = Unit) {
        isLoading = !isLoading
        movieDetailsVM.handleAction(MovieDetailsState.Action.Load(movieId, ::logResult))
        isLoading = false
    }

    if (isLoading) {
        LoadingIndicator()
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

fun logResult(message: String) {
    Log.i("ACTION LOG", message)
}
