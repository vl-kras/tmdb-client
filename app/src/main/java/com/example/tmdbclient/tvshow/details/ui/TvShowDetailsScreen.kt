package com.example.tmdbclient.tvshow.details.ui

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
import com.example.tmdbclient.shared.TmdbBasePaths.TMDB_POSTER_ORIGINAL_DIRECTORY
import com.example.tmdbclient.tvshow.details.domain.TvShowDetailsInteractor.Companion.RATING_MAX
import com.example.tmdbclient.tvshow.details.domain.TvShowDetailsInteractor.Companion.RATING_MIN
import com.example.tmdbclient.tvshow.details.domain.TvShowDetailsInteractor.Companion.RATING_STEP
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch

@Composable
fun TvShowDetailsScreen(profileVM: ProfileViewModel, showId: Int, navController: NavController) {

    val viewModel: TvShowDetailsViewModel = viewModel()

    val uiState by viewModel.getState().collectAsState()

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

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            TvShowDetailsScreenFab(onClick = { navController.popBackStack() } )
        },
        floatingActionButtonPosition = FabPosition.Center
    ) {
        when (uiState) {
            is TvShowDetailsState.InitialState -> {
                InitialState(showId)
            }
            is TvShowDetailsState.ErrorState -> {
                ErrorState(uiState as TvShowDetailsState.ErrorState, showId)
            }
            is TvShowDetailsState.DisplayState -> {
                DisplayState(uiState as TvShowDetailsState.DisplayState, profileVM, sendMessage)
            }
        }
    }
}

@Composable
fun TvShowDetailsScreenFab(onClick: () -> Unit) {

    FloatingActionButton(onClick = onClick ) {
        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Go back")
    }
}

@Composable
fun DisplayState(state: TvShowDetailsState.DisplayState, profileVM: ProfileViewModel, sendMessage: (String) -> Unit) {

    val tvShow = state.content

    Column {
        TvShowTitle(
            title = tvShow.title,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Row {
            val rowContentsWeight = 1f
            PosterImage(
                urlString = TMDB_POSTER_ORIGINAL_DIRECTORY + tvShow.posterPath,
                modifier = Modifier.weight(rowContentsWeight)
            )
            Column(Modifier.weight(rowContentsWeight)) {

                TvShowStatus(status = tvShow.status)
                TvShowGenres(genres = tvShow.genres)
                TvShowRating(rating = tvShow.userScore)
                TvShowTagline(tagline = tvShow.tagline)


                val profileState = profileVM.getState().value
                if (profileState is ProfileState.ActiveSession) {
                    RatingButton(profileState, tvShow.id, sendMessage)
                }
            }
        }
        TvShowDescription(description = tvShow.overview)
    }
}

@Composable
fun RatingButton(
    profileState: ProfileState.ActiveSession,
    showId: Int, onActionResult: (String) -> Unit
) {

    var isPostingRating by remember { mutableStateOf(false) }
    var isRatingDialogShowing by remember { mutableStateOf(false) }

    if (isRatingDialogShowing) {
        RatingDialogHandler(
            setIsDialogVisible = { isRatingDialogShowing = it },
            sessionId = profileState.sessionId,
            movieId = showId,
            onActionResult = onActionResult,
            setIsPostingRating = { isPostingRating = it}
        )
    }

    Button( enabled = !isPostingRating,
        onClick = { isRatingDialogShowing = true }
    ) {
        Row {
            Icon(imageVector = Icons.Default.Star, contentDescription = "Icon: Star")
            if (isPostingRating) {
                "Posting"
            } else {
                "Give rating"
            }.let {
                Text(text = it)
            }
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

    val tvShowDetailsViewModel: TvShowDetailsViewModel = viewModel()
    val coroutineScope = rememberCoroutineScope()

    val postRating: (Float) -> Unit = { rating ->
        coroutineScope.launch {

            setIsPostingRating(true)

            val action = ratingActionBuilder(rating, sessionId, movieId, onActionResult)
            tvShowDetailsViewModel.handleAction(action)

        }.invokeOnCompletion {
            setIsPostingRating(false)
        }
    }

    RatingDialog(setIsDialogVisible, postRating)
}

fun ratingActionBuilder(
    rating: Float, sessionId: String,
    showId: Int, onActionResult: (String) -> Unit
): TvShowDetailsState.Action {

    return when(rating) {
        0f -> {
            TvShowDetailsState.Action.DeleteRating(
                sessionId, showId, onActionResult
            )
        }
        else -> {
            TvShowDetailsState.Action.PostRating(
                sessionId, showId, rating, onActionResult
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
        Text(text = "Post")
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
fun TvShowDescription(description: String) {
    Text(text = description)
}

@Composable
fun TvShowTagline(tagline: String) {
    Text(text = tagline)
}

@Composable
fun TvShowRating(rating: Float) {
    Text(text = "$rating/10")
}

@Composable
fun TvShowGenres(genres: List<String>) {
    Text(text = genres.joinToString())
}

@Composable
fun TvShowStatus(status: String) {
    //status means "Ongoing", "Dead" etc.
    Text(text = status)
}

@Composable
fun TvShowTitle(title: String, modifier: Modifier) {
    Text(text = title, modifier = modifier)
}

@Composable
fun PosterImage(urlString: String, modifier: Modifier) {
    Image(
        painter = rememberImagePainter(data = urlString),
        contentDescription = "TV Show poster",
        modifier = modifier.aspectRatio(ratio = TMDB_POSTER_HEIGHT_WIDTH_RATIO)
    )
}

@Composable
fun ErrorState(state: TvShowDetailsState.ErrorState, showId: Int) {

    val viewModel: TvShowDetailsViewModel = viewModel()

    val coroutineScope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(false) }

    val onRetry: () -> Unit = {
        coroutineScope.launch {
            isLoading = true
            viewModel.handleAction(
                TvShowDetailsState.Action.Load(showId)
            )

        }.invokeOnCompletion {
            isLoading = false
        }
    }

    if (isLoading) {
        LoadingIndicator()
    } else {
        ErrorMessageWithRetry(state.exception, onRetry)
    }
}

@Composable
fun InitialState(tvShowId: Int) {

    val viewModel: TvShowDetailsViewModel = viewModel()

    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = Unit) {
        isLoading = !isLoading
        viewModel.handleAction(TvShowDetailsState.Action.Load(tvShowId))
        isLoading = false
    }

    if (isLoading) {
        LoadingIndicator()
    }
}

@Composable
fun ErrorMessageWithRetry(error: Exception, onRetry: () -> Unit ) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Text(text = error.message ?: "Something went wrong")
            Button(onClick = onRetry ) {
                Text(text = "Retry")
            }
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