package com.example.tmdbclient

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.tmdbclient.TmdbBasePaths.TMDB_POSTER_ORIGINAL
import com.example.tmdbclient.databinding.FragmentMovieDetailsBinding
import kotlinx.coroutines.launch
import java.lang.StringBuilder

class MovieDetailsFragment : Fragment() {

    private val profileVM: ProfileViewModel by activityViewModels()
    private val movieDetailsVM: MovieDetailsViewModel by viewModels()

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

        val movieId = arguments?.getInt("movieId")
            ?: throw NullPointerException("Passed MovieID cannot be null'")

        val actionBar = (activity as MainActivity).supportActionBar

        lifecycleScope.launch {

            val movieDetails = movieDetailsVM.getMovieById(movieId)


            actionBar?.title = movieDetails.title

            with(binding) {

                Log.d("BLABLA", movieDetails.toString())

                poster.apply {
                    Glide.with(this)
                        .load(TMDB_POSTER_ORIGINAL + movieDetails.posterPath)
                        .into(this)
                }

                title.text = movieDetails.title

                if (movieDetails.isAdult) {
                    adult.text = "16+"
                } else {
                    adult.visibility = View.GONE
                }
                genres.text = movieDetails.genres.joinToString { it.name }

                if (movieDetails.runtime != null) {
                    runtime.text = movieDetails.runtime.let { runtime ->
                        //runtime is in minutes, convert it to "X hours Y minutes" format
                            val hour = 60
                            StringBuilder()
                                .append(runtime.div(hour), "h ") // get full hours
                                .append(runtime.mod(hour), "m") //get minutes that are left
                                .toString()
                    }
                }

                tagline.text = movieDetails.tagline

                overview.text = movieDetails.overview

                userScore.text = "${movieDetails.voteAverage.times(10)}%"

                profileVM.profile.observe(viewLifecycleOwner) { user ->
                    if (user is ProfileViewModel.AppSession.UserSession) {
                        giveRating.visibility = View.VISIBLE
                        giveRating.setOnClickListener {
                            val values = resources.getStringArray(R.array.rating_options)
                            val sessionId = user.sessionId

                            AlertDialog.Builder(requireActivity())
                                .setTitle("Select movie rating")
                                .setItems(
                                    values
                                ) { _, itemIndex ->
                                    lifecycleScope.launch {
                                        val ratedSuccessfully = movieDetailsVM.rateMovie(
                                            movieId,
                                            values[itemIndex].toFloat(),
                                            sessionId
                                        )
                                        val message: String = if (ratedSuccessfully) {
                                            "New rating posted"
                                        } else {
                                            "Failed to post rating"
                                        }
                                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .setNeutralButton("Remove current rating") { _, _ ->
                                    lifecycleScope.launch {
                                        val ratedSuccessfully = movieDetailsVM.removeMovieRating(
                                            movieId,
                                            sessionId
                                        )
                                        val message: String = if (ratedSuccessfully) {
                                            "Successfully removed rating"
                                        } else {
                                            "Failed to remove rating"
                                        }
                                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
                                .create()
                                .show()
                        }

                    } else {
                        giveRating.visibility = View.GONE
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}