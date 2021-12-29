package com.example.tmdbclient.tvshow.details

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import coil.compose.rememberImagePainter
import com.example.tmdbclient.R
import com.example.tmdbclient.shared.TmdbBasePaths
import com.example.tmdbclient.profile.ProfileState
import com.example.tmdbclient.profile.ProfileViewModel
import com.example.tmdbclient.shared.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class TvShowDetailsFragment : Fragment() {

    private val profileVM: ProfileViewModel by activityViewModels()
    private val tvShowDetailsVM: TvShowDetailsViewModel by viewModels()

    private val tvShowId: Int by lazy {
        val args: TvShowDetailsFragmentArgs by navArgs()
        args.showId
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply {
            setContent {
                MyApplicationTheme {
                    TvShowDetailsScreen()
                }
            }
        }
    }

    @Composable
    fun TvShowDetailsScreen() {

        val uiState by tvShowDetailsVM.getState().observeAsState()

        when (uiState) {
            is TvShowDetailsState.Initial -> {
                InitialState()
            }
            is TvShowDetailsState.Error -> {
                ErrorState(uiState as TvShowDetailsState.Error)
            }
            is TvShowDetailsState.Display -> {
                DisplayState(uiState as TvShowDetailsState.Display)
            }
        }
    }

    @Composable
    fun DisplayState(state: TvShowDetailsState.Display) {

        val tvShow = state.content

        Column {
            Text(
                text = tvShow.title,
                Modifier.align(Alignment.CenterHorizontally)
            )
            Row {
                Image(
                    painter = rememberImagePainter(data = TmdbBasePaths.TMDB_POSTER_ORIGINAL + tvShow.posterPath),
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
                            onChanged = { showRatingDialog = it }
                        )
                    }
                    Log.d("PROFILE", profileVM.getProfile().value.toString())

                    if (profileVM.getProfile().value is ProfileState.UserState) {
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
    fun RatingDialog(onChanged: (Boolean) -> Unit) {

        val coroutineScope = rememberCoroutineScope()


        var rating: Float by remember { mutableStateOf(0f) }


        AlertDialog(
            title = { Text(text = "Select Rating") },
            onDismissRequest = { onChanged(false) },
            confirmButton = {
                Button(
                    onClick = {
                        val sessionId = (profileVM.getProfile().value as ProfileState.UserState).sessionId
                        val action = when (rating) {
                            0f -> TvShowDetailsState.Action.DeleteRating(sessionId, tvShowId)
                            else -> TvShowDetailsState.Action.PostRating(sessionId, tvShowId, rating)
                        }
                        coroutineScope.launch {
                            tvShowDetailsVM.handleAction(action)
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
    fun ErrorState(state: TvShowDetailsState.Error) {

        val coroutineScope = rememberCoroutineScope()

        var isLoading by remember { mutableStateOf(false) }

        val onRetry: () -> Unit = {
            coroutineScope.launch {
                isLoading = true
                tvShowDetailsVM.handleAction(TvShowDetailsState.Action.Load(tvShowId))
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
    fun InitialState() {

        var isLoading by remember { mutableStateOf(false) }

        LaunchedEffect(key1 = Unit) {
            isLoading = !isLoading
            tvShowDetailsVM.handleAction(TvShowDetailsState.Action.Load(tvShowId))
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

    @Preview
    @Composable
    fun MyPreview() {
        MaterialTheme {
            ErrorMessageWithRetry(
                error = Exception("Something went wrong"),
                onRetry = {  }
            )
        }
    }
}