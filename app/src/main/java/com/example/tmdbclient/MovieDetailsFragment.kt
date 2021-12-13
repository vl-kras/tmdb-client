package com.example.tmdbclient

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.tmdbclient.TmdbBasePaths.TMDB_POSTER_ORIGINAL
import com.example.tmdbclient.databinding.FragmentMovieDetailsBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.lang.StringBuilder

class MovieDetailsFragment : Fragment() {

    private val profileVM: ProfileViewModel by activityViewModels()
    private val movieDetailsVM: MovieDetailsViewModel by viewModels()
    private val args: MovieDetailsFragmentArgs by navArgs()

    private var _binding : FragmentMovieDetailsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentMovieDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val actionBar = (activity as MainActivity).supportActionBar

        lifecycleScope.launch {

            val movieDetails = movieDetailsVM.getMovieById(args.movieId)

            actionBar?.title = movieDetails.title

            setMoviePoster(movieDetails)
            setMovieAdultRating(movieDetails)
            setMovieRuntime(movieDetails)
            configureRatingButton()
            binding.title.text = movieDetails.title
            binding.genres.text = movieDetails.genres.joinToString { it.name }
            binding.tagline.text = movieDetails.tagline
            binding.overview.text = movieDetails.overview
            binding.userScore.text = "${movieDetails.voteAverage.times(10)}%"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setMoviePoster(movie: MovieDetails) {
        Glide.with(binding.poster)
            .load(TMDB_POSTER_ORIGINAL + movie.posterPath)
            .into(binding.poster)
    }

    private fun setMovieAdultRating(movie: MovieDetails) {
        if (movie.isAdult) {
            binding.adult.text = "16+"
        } else {
            binding.adult.visibility = View.GONE
        }
    }

    private fun setMovieRuntime(movie: MovieDetails) {
        if (movie.runtime != null) {
            binding.runtime.text = movie.runtime.let { runtime ->
                //runtime is in minutes, convert it to "X hours Y minutes" format
                StringBuilder()
                    .append(runtime.div(60), "h ") // get full hours
                    .append(runtime.mod(60), "m") //get minutes that are left
                    .toString()
            }
        }
    }

    private fun configureRatingButton() {
        profileVM.profile.observe(viewLifecycleOwner) { user ->
            if (user is ProfileViewModel.AppSession.UserSession) {

                binding.giveRating.visibility = View.VISIBLE
                binding.giveRating.setOnClickListener {
                    val values = resources.getStringArray(R.array.rating_options)
                    val sessionId = user.sessionId

                    buildRatingDialog(values, sessionId).show()
                }
            } else {
                binding.giveRating.visibility = View.GONE
            }
        }
    }

    private fun buildRatingDialog(values: Array<String>, sessionId: String): AlertDialog {
        return AlertDialog.Builder(requireActivity())
            .setTitle("Select movie rating")
            .setItems(
                values,
                getRatingDialogOnItemClickAction(values,sessionId)
            )
            .setNeutralButton(
                "Remove current rating",
                getRatingDialogNeutralAction(sessionId)
            )
            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            .create()
    }

    private fun getRatingDialogOnItemClickAction(
        values: Array<String>,
        sessionId: String
    ): DialogInterface.OnClickListener {
        return DialogInterface.OnClickListener { _, itemIndex: Int ->
            lifecycleScope.launch {
                val ratedSuccessfully = movieDetailsVM.rateMovie(
                    args.movieId,
                    values[itemIndex].toFloat(),
                    sessionId
                )
                val message = if (ratedSuccessfully) {
                    "New rating posted"
                } else {
                    "Failed to post rating"
                }
                Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun getRatingDialogNeutralAction(
        sessionId: String
    ): DialogInterface.OnClickListener {
        return DialogInterface.OnClickListener { _, _ ->
            lifecycleScope.launch {
                val ratedSuccessfully = movieDetailsVM.removeMovieRating(
                    args.movieId,
                    sessionId
                )
                val message = if (ratedSuccessfully) {
                    "Successfully removed rating"
                } else {
                    "Failed to remove rating"
                }
                Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}