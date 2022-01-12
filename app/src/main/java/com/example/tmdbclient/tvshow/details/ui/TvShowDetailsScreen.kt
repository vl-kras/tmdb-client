package com.example.tmdbclient.tvshow.details.ui

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberImagePainter
import com.example.tmdbclient.R
import com.example.tmdbclient.profile.ui.ProfileState
import com.example.tmdbclient.profile.ui.ProfileViewModel
import com.example.tmdbclient.shared.TmdbBasePaths
import kotlinx.coroutines.launch

@Composable
fun TvShowDetailsScreen(profileVM: ProfileViewModel, showId: Int) {
    val viewModel: TvShowDetailsViewModel = viewModel()

    val uiState by viewModel.getState().collectAsState()

    when (uiState) {
        is TvShowDetailsState.Initial -> {
            InitialState(showId)
        }
        is TvShowDetailsState.Error -> {
            ErrorState(uiState as TvShowDetailsState.Error, showId)
        }
        is TvShowDetailsState.Display -> {
            DisplayState(uiState as TvShowDetailsState.Display, profileVM)
        }
    }
}

@Composable
fun DisplayState(state: TvShowDetailsState.Display, profileVM: ProfileViewModel) {

    val tvShow = state.content

    Column {
        Text(
            text = tvShow.title,
            Modifier.align(Alignment.CenterHorizontally)
        )
        Row {
            Image(
                painter = rememberImagePainter(data = TmdbBasePaths.TMDB_POSTER_ORIGINAL_DIRECTORY + tvShow.posterPath),
                contentDescription = "Show poster",
                modifier = Modifier
                    .aspectRatio(ratio = 0.66f)
                    .weight(1f)
            )
            Column(Modifier.weight(1f)) {
                Text(text = tvShow.status)
                Text(text = tvShow.genres.joinToString { it })
                Text(text = "${tvShow.userScore.times(10)}%")
                Text(text = tvShow.tagline)

                var showRatingDialog by remember { mutableStateOf(false) }
                if (showRatingDialog) {
                    RatingDialog(
                        onChanged = { showRatingDialog = it },
                        profileVM = profileVM,
                        showId = tvShow.id
                    )
                }
                Log.d("PROFILE", profileVM.getState().value.toString())

                if (profileVM.getState().value is ProfileState.ActiveSession) {
                    Button(
                        onClick = { showRatingDialog = true }
                    ) {
                        Row() {
                            Image(
                                painter = painterResource(id = R.drawable.ic_baseline_star_24),
                                contentDescription = "Rating Star"
                            )
                            Text(text = "Give Rating")
                        }
                    }
                }
            }
        }
        Text(text = tvShow.overview)
    }
}

@Composable
fun RatingDialog(onChanged: (Boolean) -> Unit, profileVM: ProfileViewModel, showId: Int) {

    val coroutineScope = rememberCoroutineScope()


    var rating: Float by remember { mutableStateOf(0f) }

    val viewModel: TvShowDetailsViewModel = viewModel()



    AlertDialog(
        title = { Text(text = "Select Rating") },
        onDismissRequest = { onChanged(false) },
        confirmButton = {
            Button(
                onClick = {
                    val sessionId = (profileVM.getState().value as ProfileState.ActiveSession).sessionId
                    val action = when (rating) {
                        0f ->  {
                            TvShowDetailsState.Action.DeleteRating(
                                sessionId,
                                showId
                            ) { resultString ->

                                Log.d("ACTION", resultString)
                            }
                        }
                        else -> {
                            TvShowDetailsState.Action.PostRating(
                                sessionId,
                                showId,
                                rating
                            ) { resultString ->

                                Log.d("ACTION", resultString)
                            }
                        }
                    }
                    coroutineScope.launch {
                        viewModel.handleAction(action)
                    }
                }
            ) {
                Text(text = "Post Rating")
            }
        },
        dismissButton = {
            Button(onClick = { onChanged(false) }) {
                Text(text = "Cancel")
            }
        },
        text = {
            Row {
                TextButton(
                    onClick = {
                        rating = 0f
                    }
                ) {
                    Text(text = "Delete my rating")
                }
                for(r in 5..100 step 5) {
                    TextButton(
                        onClick = {
                            rating = (r / 10).toFloat()
                        }
                    ) {
                        Text(text = (r / 10).toString() )
                    }
                }
            }
        }
    )
}

@Composable
fun ErrorState(state: TvShowDetailsState.Error, showId: Int) {

    val viewModel: TvShowDetailsViewModel = viewModel()

    val coroutineScope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(false) }

    val onRetry: () -> Unit = {
        coroutineScope.launch {
            isLoading = true
            viewModel.handleAction(
                TvShowDetailsState.Action.Load(showId)
                { resultString ->

                    Log.d("ACTION", resultString)
                }
            )
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
        viewModel.handleAction(TvShowDetailsState.Action.Load(tvShowId) { resultString ->

            Log.d("ACTION", resultString)
        })
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