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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.example.tmdbclient.R
import com.example.tmdbclient.movie.details.domain.MovieDetailsInteractor.Companion.RATING_MAX
import com.example.tmdbclient.movie.details.domain.MovieDetailsInteractor.Companion.RATING_MIN
import com.example.tmdbclient.movie.details.domain.MovieDetailsInteractor.Companion.RATING_STEP
import com.example.tmdbclient.profile.ui.ProfileState
import com.example.tmdbclient.profile.ui.ProfileViewModel
import com.example.tmdbclient.shared.LocalProfileVM
import com.example.tmdbclient.shared.TmdbBasePaths.TMDB_POSTER_HEIGHT_WIDTH_RATIO
import com.example.tmdbclient.shared.TmdbBasePaths.TMDB_POSTER_ORIGINAL_DIRECTORY
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch

@Composable
fun MovieDetailsScreen(movieId: Int, navController: NavController) {

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
        movieId, sendMessage
    )
}

@Composable
private fun ScreenContents(
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    movieId: Int,
    sendMessage: (String) -> Unit
) {

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            MovieDetailsScreenFab(onClick = { navController.popBackStack() })
        },
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
                    movieId, sendMessage
                )
            }
        }
    }
}

@Composable
private fun MovieDetailsScreenFab(onClick: () -> Unit) {

    FloatingActionButton(onClick = onClick ) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = stringResource(R.string.go_back_arrow_description)
        )
    }
}

@Composable
private fun MoviePoster(posterId: String, modifier: Modifier) {
    Image(
        painter = rememberImagePainter(data = TMDB_POSTER_ORIGINAL_DIRECTORY + posterId),
        contentDescription = stringResource(R.string.movie_poster_description),
        modifier = modifier.aspectRatio(ratio = TMDB_POSTER_HEIGHT_WIDTH_RATIO)
    )
}

@Composable
private fun DisplayState(
    state: MovieDetailsState.DisplayState,
    movieId: Int,
    onActionResult: (String) -> Unit
) {

    val movieDetails = state.movieDetails
    val profileState = LocalProfileVM.current.getState().collectAsState()

    Column(Modifier.fillMaxSize()) {
        Text(
            text = movieDetails.title,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Row {
            val rowContentsWeight = 1f
            MoviePoster(
                posterId = movieDetails.posterPath,
                modifier = Modifier.weight(rowContentsWeight)
            )

            Column(Modifier.weight(rowContentsWeight)) {

                MovieRuntime(runtime = movieDetails.runtime)
                MovieGenres(genres = movieDetails.genres)
                MovieRating(rating = movieDetails.userScore)
                MovieTagline(tagline = movieDetails.tagline)

                if (profileState.value is ProfileState.ActiveSession) {
                    RatingButton(
                        profileState.value as ProfileState.ActiveSession,
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
private fun MovieDescription(description: String) {
    Text(text = description)
}

@Composable
private fun MovieTagline(tagline: String) {
    Text(text = tagline)
}

@Composable
private fun MovieRating(rating: Float) {
    Text(text = "$rating/10")
}

@Composable
private fun MovieGenres(genres: List<String>) {
    Text(text = genres.joinToString() )
}

@Composable
private fun MovieRuntime(runtime: Int) {

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
private fun RatingButton(
    profileState: ProfileState.ActiveSession,
    movieId: Int, onActionResult: (String) -> Unit
) {

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
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = stringResource(R.string.rating_button_icon_description)
                )
                Text(text = stringResource(R.string.rating_button_label))
            }
        } else {
            LinearProgressIndicator()
        }
    }
}

//TODO learn to use composable context

@Composable
private fun RatingDialogHandler(
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

private fun ratingActionBuilder(
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
private fun RatingDialog(setIsDialogVisible: (Boolean) -> Unit, postRating: (Float) -> Unit) {

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
private fun CancelButton(onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text(text = stringResource(R.string.cancel_button_label))
    }
}

@Composable
private fun ConfirmButton(onClick: () -> Unit) {
    TextButton(
        onClick = {
            onClick()
        }
    ) {
        Text(text = stringResource(R.string.confirm_rating_button_text))
    }
}

@Composable
private fun RatingDialogTitle() {
    Text(
        text = stringResource(R.string.rating_dialog_title),
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun RatingDialogContent(rating: Float, setRating: (Float)-> Unit) {

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
private fun SelectedRating(rating: Float) {

    val ratingAsString = if (rating != RATING_MIN) {
        rating.toString()
    } else {
        stringResource(R.string.delete_rating_option_text)
    }
    Text(ratingAsString)
}

@Composable
private fun DecreaseRatingButton(rating: Float, setRating: (Float) -> Unit) {
    IconButton(
        onClick = { setRating(rating - RATING_STEP) },
        content = { DecreaseRatingIcon() }
    )
}

@Composable
private fun DecreaseRatingIcon() {
    Icon(
        imageVector = Icons.Default.ArrowBack,
        contentDescription = stringResource(R.string.decrease_rating_button_description)
    )
}

@Composable
private fun IncreaseRatingIcon() {
    Icon(
        imageVector = Icons.Default.ArrowForward,
        contentDescription = stringResource(R.string.increase_rating_button_description)
    )
}

@Composable
private fun IncreaseRatingButton(rating: Float, setRating: (Float) -> Unit) {
    IconButton(
        onClick = { setRating(rating + RATING_STEP) },
        content = { IncreaseRatingIcon() }
    )
}

@Composable
private fun ErrorState(state: MovieDetailsState.ErrorState, movieId: Int) {

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
private fun ErrorStateView(isLoading: Boolean, exception: Exception, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!isLoading) {
            Text(text = exception.message ?: stringResource(R.string.generic_exception_message))
            Button(onClick = onRetry ) {
                Text(text = stringResource(R.string.retry_button_text))
            }
        } else {
            LoadingIndicator()
        }
    }
}

@Composable
private fun InitialState(movieId: Int) {

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
private fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}