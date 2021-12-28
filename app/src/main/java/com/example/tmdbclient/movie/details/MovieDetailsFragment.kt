package com.example.tmdbclient.movie.details

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.tmdbclient.R
import com.example.tmdbclient.shared.TmdbBasePaths.TMDB_POSTER_ORIGINAL
import com.example.tmdbclient.databinding.FragmentMovieDetailsBinding
import com.example.tmdbclient.profile.ProfileState
import com.example.tmdbclient.profile.ProfileViewModel
import com.example.tmdbclient.shared.theme.MyApplicationTheme
import com.google.android.material.snackbar.Snackbar
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.launch
import java.io.IOException
import kotlin.text.StringBuilder

class MovieDetailsFragment : Fragment() {

    private val profileVM: ProfileViewModel by activityViewModels()
    private val movieDetailsVM: MovieDetailsViewModel by viewModels()

    private var _binding : FragmentMovieDetailsBinding? = null
    private val binding get() = _binding!!

    private val args: MovieDetailsFragmentArgs by navArgs()
    private val movieId: Int by lazy {
        args.movieId
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

//        _binding = FragmentMovieDetailsBinding.inflate(inflater, container, false)
        return ComposeView(requireContext()).apply {
            setContent {
                MyApplicationTheme {
                    MovieDetailsScreen()
                }
            }
        }
    }

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        binding.retryButton.setOnClickListener {
//            val action = MovieDetailsState.Action.Load(args.movieId)
////            movieDetailsVM.handleAction(action)
//        }
//
//        movieDetailsVM.getState().observe(viewLifecycleOwner) { state ->
//            observeStateChanges(state)
//        }
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//
//    private fun observeStateChanges(state: MovieDetailsState) {
//        configureViews(state)
//        displayViews(state)
//    }
//
//    private fun configureViews(state: MovieDetailsState) {
//        when (state) {
//            is MovieDetailsState.Initial -> {
//                val action = MovieDetailsState.Action.Load(args.movieId)
////                movieDetailsVM.handleAction(action)
//            }
//            is MovieDetailsState.Display -> {
//                setMoviePoster(state.content)
//                setMovieAdultRating(state.content)
//                setMovieRuntime(state.content)
//                configureRatingButton()
//                binding.title.text = state.content.title
//                binding.genres.text = state.content.genres.joinToString { it }
//                binding.tagline.text = state.content.tagline
//                binding.overview.text = state.content.overview
//                binding.userScore.text = "${state.content.userScore.times(10)}%"
//            }
//            is MovieDetailsState.Error -> {
//                binding.statusMessage.text = state.exception.message
//            }
//        }
//    }
//
//    private fun displayViews(state: MovieDetailsState) {
//        with(binding) {
//            title.isVisible = state is MovieDetailsState.Display
//            poster.isVisible = state is MovieDetailsState.Display
//            genres.isVisible = state is MovieDetailsState.Display
//            tagline.isVisible = state is MovieDetailsState.Display
//            overview.isVisible = state is MovieDetailsState.Display
//            userScore.isVisible = state is MovieDetailsState.Display
//            giveRating.isVisible = (state is MovieDetailsState.Display) and
//                    (profileVM.getProfile().value is ProfileState.UserState)
//
//            loadingIndicator.isVisible = (state is MovieDetailsState.Loading)
//
//            statusMessage.isVisible = state is MovieDetailsState.Error
//            retryButton.isVisible = state is MovieDetailsState.Error
//        }
//    }
//
//    private fun setMoviePoster(movie: MovieDetailsRepository.MovieDetails) {
//        Glide.with(binding.poster)
//            .load(TMDB_POSTER_ORIGINAL + movie.posterPath)
//            .into(binding.poster)
//    }
//
//    private fun setMovieAdultRating(movie: MovieDetailsRepository.MovieDetails) {
//
//        if (movie.isAdult) {
//            binding.adult.text = "16+"
//        } else {
//            binding.adult.visibility = View.GONE
//        }
//    }
//
//    private fun setMovieRuntime(movie: MovieDetailsRepository.MovieDetails) {
//
//        if (movie.runtime != 0) {
//            binding.runtime.text = movie.runtime.let { runtime ->
//                //runtime is in minutes, convert it to "X hours Y minutes" format
//                StringBuilder()
//                    .append(runtime.div(60), "h ") // get full hours
//                    .append(runtime.mod(60), "m") //get minutes that are left
//                    .toString()
//            }
//        }
//    }
//
//    private fun configureRatingButton() {
//
//        profileVM.getProfile().observe(viewLifecycleOwner) { user ->
//            if (user is ProfileState.UserState) {
//
//                binding.giveRating.visibility = View.VISIBLE
//                binding.giveRating.setOnClickListener {
//                    val values = resources.getStringArray(R.array.rating_options)
//                    val sessionId = user.sessionId
//
//                    buildRatingDialog(values, sessionId).show()
//                }
//            } else {
//                binding.giveRating.visibility = View.GONE
//            }
//        }
//    }
//
//    private fun buildRatingDialog(values: Array<String>, sessionId: String): AlertDialog {
//
//        return AlertDialog.Builder(requireActivity())
//            .setTitle("Select movie rating")
//            .setItems(
//                values,
//                getRatingDialogOnItemClickAction(values,sessionId)
//            )
//            .setNeutralButton(
//                "Remove current rating",
//                getRatingDialogNeutralAction(sessionId)
//            )
//            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
//            .create()
//    }
//
//    private fun getRatingDialogOnItemClickAction(
//        values: Array<String>,
//        sessionId: String
//    ): DialogInterface.OnClickListener {
//
//        return DialogInterface.OnClickListener { _, itemIndex: Int ->
//            val message = try {
//                val action = MovieDetailsState.Action.PostRating(
//                    sessionId,
//                    args.movieId,
//                    values[itemIndex].toFloat()
//                )
////                movieDetailsVM.handleAction(action)
//
//                "New rating posted"
//            } catch (e: IOException) {
//                "Failed to post rating"
//            }
//            Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun getRatingDialogNeutralAction(
//        sessionId: String
//    ): DialogInterface.OnClickListener {
//
//        return DialogInterface.OnClickListener { _, _ ->
//            val message = try {
//                val action = MovieDetailsState.Action.DeleteRating(
//                    sessionId,
//                    args.movieId
//                )
////                movieDetailsVM.handleAction(action)
//
//                "Successfully removed rating"
//            } catch (e: IOException) {
//                "Failed to remove rating"
//            }
//            Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
//        }
//    }

    //TODO add rating button and its actions

    @Composable
    fun MovieDetailsScreen() {

        val viewModel: MovieDetailsViewModel = viewModel()

        val uiState by viewModel.getState().observeAsState()

        when (uiState) {
            is MovieDetailsState.Initial -> {
                InitialState()
            }
            is MovieDetailsState.Error -> {
                ErrorState(uiState as MovieDetailsState.Error)
            }
            is MovieDetailsState.Display -> {
                DisplayState(uiState as MovieDetailsState.Display)
            }
        }
    }

    @Composable
    fun DisplayState(state: MovieDetailsState.Display) {
        val movie = state.content

        Column {
            Text(text = movie.title)
            Row {
                GlideImage(imageModel = TMDB_POSTER_ORIGINAL + movie.posterPath)
                Column {
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
                }
            }
            Text(text = movie.overview)
        }
    }

    @Composable
    fun ErrorState(state: MovieDetailsState.Error) {

        val viewModel: MovieDetailsViewModel = viewModel()
        val coroutineScope = rememberCoroutineScope()

        var isLoading by remember { mutableStateOf(false) }

        val onClick: () -> Unit = {
            coroutineScope.launch {
                isLoading = true
                viewModel.handleAction(MovieDetailsState.Action.Load(movieId))
                isLoading = false
            }
        }

        if (isLoading) {
            CircularProgressIndicator()

        } else {
            Column {
                Text(text = state.exception.message ?: "Something went wrong")
            }
            Button(onClick = onClick ) {
                Text(text = "Retry")
            }
        }
    }

    @Composable
    fun InitialState() {
        val viewModel: MovieDetailsViewModel = viewModel()

        var isLoading by remember { mutableStateOf(true) }

        LaunchedEffect(key1 = Unit) {
            viewModel.handleAction(MovieDetailsState.Action.Load(movieId))
            isLoading = false
        }

        if (isLoading) {
            CircularProgressIndicator()
        }
    }
}