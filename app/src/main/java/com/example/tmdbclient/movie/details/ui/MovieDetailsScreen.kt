package com.example.tmdbclient.movie.details.ui

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
import com.example.tmdbclient.shared.TmdbBasePaths.TMDB_POSTER_ORIGINAL
import kotlinx.coroutines.launch

@Composable
fun MovieDetailsScreen(profileVM: ProfileViewModel, movieId: Int) {

    val movieDetailsVM: MovieDetailsViewModel = viewModel()

    val state by movieDetailsVM.getState().collectAsState()

    when (state) {
        is MovieDetailsState.Initial -> {
            InitialState(movieId)
        }
        is MovieDetailsState.Error -> {
            ErrorState(state as MovieDetailsState.Error, movieId)
        }
        is MovieDetailsState.Display -> {
            DisplayState(state as MovieDetailsState.Display, profileVM, movieId)
        }
    }
}

@Composable
fun DisplayState(state: MovieDetailsState.Display, profileVM: ProfileViewModel, movieId: Int) {

    val movie = state.content

    Column {
        Text(
            text = movie.title,
            Modifier.align(Alignment.CenterHorizontally)
        )
        Row {
            Image(
                painter = rememberImagePainter(data = TMDB_POSTER_ORIGINAL + movie.posterPath),
                contentDescription = "Movie poster",
                modifier = Modifier
                    .aspectRatio(ratio = 0.66f)
                    .weight(1f)
            )
            Column(Modifier.weight(1f)) {
                Row {
                    if (movie.isAdult) {
                        Text(text = "18+")
                    }
                    Text(text = movie.runtime.let {
                        val hours = it.div(60)
                        val minutes = it.mod(60)
                        val result = StringBuilder()
                        if (hours != 0) {
                            result.append("${hours}h")
                        }
                        if (minutes != 0) {
                            result.append("${minutes}m")
                        }
                        result.toString()
                    })
                }
                Text(text = movie.genres.joinToString { it })
                Text(text = "${movie.userScore.times(10)}%")
                Text(text = movie.tagline)

                var showRatingDialog by remember { mutableStateOf(false) }
                if (showRatingDialog) {
                    RatingDialog(
                        onChanged = { showRatingDialog = it },
                        profileVM = profileVM,
                        movieId = movieId
                    )
                }
                Log.d("PROFILE", profileVM.getState().value.toString())

                if (profileVM.getState().value is ProfileState.UserState) {
                    Button(
                        onClick = { showRatingDialog = true }
                    ) {
                        Row {
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
        Text(text = movie.overview)
    }
}

@Composable
fun RatingDialog(onChanged: (Boolean) -> Unit, profileVM: ProfileViewModel, movieId: Int) {

    val movieDetailsVM: MovieDetailsViewModel = viewModel()
    val coroutineScope = rememberCoroutineScope()

    var rating: Float by remember { mutableStateOf(0f) }

    AlertDialog(
        title = { Text(text = "Select Rating") },
        onDismissRequest = { onChanged(false) },
        confirmButton = {
            Button(
                onClick = {
                    val sessionId = (profileVM.getState().value as ProfileState.UserState).sessionId
                    val action = when (rating) {
                        0f -> MovieDetailsState.Action.DeleteRating(sessionId, movieId)
                        else -> MovieDetailsState.Action.PostRating(sessionId, movieId, rating)
                    }
                    coroutineScope.launch {
                        movieDetailsVM.handleAction(action)
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
fun ErrorState(state: MovieDetailsState.Error, movieId: Int) {

    val movieDetailsVM: MovieDetailsViewModel = viewModel()
    val coroutineScope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(false) }

    val onClick: () -> Unit = {
        coroutineScope.launch {
            isLoading = true
            movieDetailsVM.handleAction(MovieDetailsState.Action.Load(movieId))
            isLoading = false
        }
    }

    if (isLoading) {
        CircularProgressIndicator()

    } else {
        Column {
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
        movieDetailsVM.handleAction(MovieDetailsState.Action.Load(movieId))
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
