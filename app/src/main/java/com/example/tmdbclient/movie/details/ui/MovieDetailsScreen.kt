package com.example.tmdbclient.movie.details.ui

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
import com.example.tmdbclient.movie.details.domain.MovieDetailsInteractor.Companion.RATING_MAX
import com.example.tmdbclient.movie.details.domain.MovieDetailsInteractor.Companion.RATING_MIN
import com.example.tmdbclient.movie.details.domain.MovieDetailsInteractor.Companion.RATING_STEP
import com.example.tmdbclient.profile.ui.ProfileState
import com.example.tmdbclient.profile.ui.ProfileViewModel
import com.example.tmdbclient.shared.TmdbBasePaths.TMDB_POSTER_HEIGHT_WIDTH_RATIO
import com.example.tmdbclient.shared.TmdbBasePaths.TMDB_POSTER_ORIGINAL_DIRECTORY
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch

@Composable
fun MovieDetailsScreen(profileVM: ProfileViewModel, movieId: Int, navController: NavController) {

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

    val sendMessage: (String) -> Unit = {
        coroutineScope.launch {
            channel.send(it)
        }
    }

    ScreenContents(
        navController, snackbarHostState,
        movieId, profileVM, sendMessage
    )
}

@Composable
fun ScreenContents(
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    movieId: Int,
    profileVM: ProfileViewModel,
    sendMessage: (String) -> Unit
) {

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = { MovieDetailsScreenFab(navController) },
        floatingActionButtonPosition = FabPosition.Center
    ) {
        val movieDetailsVM: MovieDetailsViewModel = viewModel()
        val state by movieDetailsVM.getState().collectAsState()

        when (state) {
            is MovieDetailsState.InitialState -> {
                InitialState(movieId)
            }
            is MovieDetailsState.ErrorState -> {
                ErrorState(state as MovieDetailsState.ErrorState, movieId)
            }
            is MovieDetailsState.DisplayState -> {
                DisplayState(
                    state as MovieDetailsState.DisplayState,
                    profileVM, movieId, sendMessage
                )
            }
        }
    }

}

@Composable
fun MovieDetailsScreenFab(navController: NavController) {

    FloatingActionButton(onClick = { navController.popBackStack() } ) {
        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Go back")
    }
}

@Composable
fun MoviePoster(posterId: String, modifier: Modifier) {
    Image(
        painter = rememberImagePainter(data = TMDB_POSTER_ORIGINAL_DIRECTORY + posterId),
        contentDescription = "Movie poster",
        modifier = modifier.aspectRatio(ratio = TMDB_POSTER_HEIGHT_WIDTH_RATIO)
    )
}

@Composable
fun DisplayState(
    state: MovieDetailsState.DisplayState,
    profileVM: ProfileViewModel,
    movieId: Int,
    onActionResult: (String) -> Unit
) {

    val movieDetails = state.movieDetails
    val profileState = profileVM.getState().collectAsState()

    Column(Modifier.fillMaxSize()) {
        Text(
            text = movieDetails.title,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Row {
            MoviePoster(posterId = movieDetails.posterPath, modifier = Modifier.weight(1f))

            Column(Modifier.weight(1f)) {
                MovieRuntime(runtime = movieDetails.runtime)
                MovieGenres(genres = movieDetails.genres)
                MovieRating(rating = movieDetails.userScore)
                MovieTagline(tagline = movieDetails.tagline)

                if (profileState.value is ProfileState.UserState) {
                    RatingButton(
                        profileState.value as ProfileState.UserState,
                        movieId, onActionResult
                    )
                }
            }
        }
        MovieDescription(description = movieDetails.overview)

    }
}

const val MINUTES_IN_HOUR = 60

@Composable
fun MovieDescription(description: String) {
    Text(text = description)
}

@Composable
fun MovieTagline(tagline: String) {
    Text(text = tagline)
}

@Composable
fun MovieRating(rating: Float) {
    Text(text = "$rating/10")
}

@Composable
fun MovieGenres(genres: List<String>) {
    Text(text = genres.joinToString() )
}

@Composable
fun MovieRuntime(runtime: Int) {

    val hours = runtime.div(MINUTES_IN_HOUR)
    val minutes = runtime.mod(MINUTES_IN_HOUR)

    val formattedRuntime = StringBuilder().also {
        if (hours != 0) {
            it.append("${hours}h")
        }
        if (minutes != 0) {
            it.append("${minutes}m")
        }
    }.toString()

    Text(text = formattedRuntime)
}

@Composable
fun RatingButton(profileState: ProfileState.UserState, movieId: Int, onActionResult: (String) -> Unit) {
    var isPostingRating by remember { mutableStateOf(false) }
    var isRatingDialogShowing by remember { mutableStateOf(false) }

    if (isRatingDialogShowing) {
        RatingDialogHandler(
            setIsDialogVisible = { isRatingDialogShowing = it },
            sessionId = profileState.sessionId,
            movieId = movieId,
            onActionResult = onActionResult,
            setIsPostingRating = { isPostingRating = it}
        )
    }

    Button( enabled = !isPostingRating,
        onClick = { isRatingDialogShowing = true }
    ) {
        if (!isPostingRating) {
            Row {
                Icon(imageVector = Icons.Default.Star, contentDescription = "Icon: Star")
                Text(text = "Give Rating")
            }
        } else {
            LinearProgressIndicator()
        }
    }
}

//TODO learn to use composable context

@Composable
fun RatingDialogHandler(
    setIsDialogVisible: (Boolean) -> Unit,
    sessionId: String, movieId: Int,
    onActionResult: (String) -> Unit,
    setIsPostingRating: (Boolean) -> Unit
) {

    val movieDetailsVM: MovieDetailsViewModel = viewModel()
    val coroutineScope = rememberCoroutineScope()

    val postRating: (Float) -> Unit = { rating ->
        coroutineScope.launch {

            setIsPostingRating(true)

            val action = ratingActionBuilder(rating, sessionId, movieId, onActionResult)
            movieDetailsVM.handleAction(action)

        }.invokeOnCompletion {
            setIsPostingRating(false)
        }
    }

    RatingDialog(setIsDialogVisible, postRating)
}

fun ratingActionBuilder(
    rating: Float, sessionId: String,
    movieId: Int, onActionResult: (String) -> Unit
): MovieDetailsState.Action {

    return when(rating) {
        0f -> {
            MovieDetailsState.Action.DeleteRating(
                sessionId, movieId, onActionResult
            )
        }
        else -> {
            MovieDetailsState.Action.PostRating(
                sessionId, movieId, rating, onActionResult
            )
        }
    }
}

@Composable
fun RatingDialog(setIsDialogVisible: (Boolean) -> Unit, postRating: (Float) -> Unit) {

    //initial value is average possible rating, which should be 5f
    var rating by remember { mutableStateOf((RATING_MAX + RATING_MIN) / 2 ) }

    val onConfirm: () -> Unit = {
        postRating(rating)
        setIsDialogVisible(false)
    }
    val onCancel: () -> Unit = {
        setIsDialogVisible(false)
    }

    AlertDialog(
        title = { RatingDialogTitle() },
        onDismissRequest = onCancel,
        confirmButton = { ConfirmButton(onClick = onConfirm) },
        dismissButton = { CancelButton(onClick = onCancel) },
        text = {
            RatingDialogContent(rating, setRating = { rating = it })
        }
    )
}

@Composable
fun CancelButton(onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text(text = "Cancel")
    }
}

@Composable
fun ConfirmButton(onClick: () -> Unit) {
    TextButton(
        onClick = {
            onClick()
        }
    ) {
        Text(text = "Confirm")
    }
}

@Composable
fun RatingDialogTitle() {
    Text(
        text = "Select Rating",
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

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

    val ratingAsString = if (rating != RATING_MIN) {
        rating.toString()
    } else {
        "Delete rating"
    }
    Text(ratingAsString)
}

@Composable
fun DecreaseRatingButton(rating: Float, setRating: (Float) -> Unit) {
    IconButton(
        onClick = { setRating(rating - RATING_STEP) },
        content = { DecreaseRatingIcon() }
    )
}

@Composable
fun DecreaseRatingIcon() {
    Icon(
        imageVector = Icons.Default.ArrowBack,
        contentDescription = "Decrease rating"
    )
}

@Composable
fun IncreaseRatingIcon() {
    Icon(
        imageVector = Icons.Default.ArrowForward,
        contentDescription = "Increase rating"
    )
}

@Composable
fun IncreaseRatingButton(rating: Float, setRating: (Float) -> Unit) {
    IconButton(
        onClick = { setRating(rating + RATING_STEP) },
        content = { IncreaseRatingIcon() }
    )
}

@Composable
fun ErrorState(state: MovieDetailsState.ErrorState, movieId: Int) {

    val movieDetailsVM: MovieDetailsViewModel = viewModel()
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    val retryAction: () -> Unit = {
        coroutineScope.launch {

            isLoading = true
            val action = MovieDetailsState.Action.Load(movieId)
            movieDetailsVM.handleAction(action)

        }.invokeOnCompletion {
            isLoading = false
        }
    }
    ErrorStateView(isLoading, exception = state.exception, onRetry = retryAction, )
}

@Composable
fun ErrorStateView(isLoading: Boolean, exception: Exception, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!isLoading) {
            Text(text = exception.message ?: "Something went wrong")
            Button(onClick = onRetry ) {
                Text(text = "Retry")
            }
        } else {
            LoadingIndicator()
        }
    }
}

@Composable
fun InitialState(movieId: Int) {

    val movieDetailsVM: MovieDetailsViewModel = viewModel()
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = Unit) {

        isLoading = true
        val action = MovieDetailsState.Action.Load(movieId)
        movieDetailsVM.handleAction(action)

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