package com.example.tmdbclient.tvshow.details

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
import com.example.tmdbclient.MainActivity
import com.example.tmdbclient.R
import com.example.tmdbclient.TmdbBasePaths
import com.example.tmdbclient.TvShowDetails
import com.example.tmdbclient.databinding.FragmentTvshowDetailsBinding
import com.example.tmdbclient.profile.ProfileState
import com.example.tmdbclient.profile.ProfileViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class TvShowDetailsFragment : Fragment() {

    private val profileVM: ProfileViewModel by activityViewModels()
    private val tvShowDetailsVM: TvShowDetailsViewModel by viewModels()
    private val args: TvShowDetailsFragmentArgs by navArgs()

    private var _binding : FragmentTvshowDetailsBinding? = null
    private val binding get() = _binding!!

    private val tvShowId: Int by lazy {
        args.showId
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentTvshowDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val actionBar = (activity as MainActivity).supportActionBar

        lifecycleScope.launch {

            val tvShowDetails = tvShowDetailsVM.getTvShowById(tvShowId)

            actionBar?.title = tvShowDetails.name

            setMoviePoster(tvShowDetails)
            configureRatingButton()
            binding.name.text = tvShowDetails.name
            binding.status.text = tvShowDetails.status
            binding.genres.text = tvShowDetails.genres.joinToString { it.name }
            binding.tagline.text = tvShowDetails.tagline
            binding.overview.text = tvShowDetails.overview
            binding.userScore.text = "${tvShowDetails.voteAverage.times(10)}%"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setMoviePoster(show: TvShowDetails) {
        Glide.with(binding.poster)
            .load(TmdbBasePaths.TMDB_POSTER_ORIGINAL + show.posterPath)
            .into(binding.poster)
    }

    private fun configureRatingButton() {
        profileVM.getProfile().observe(viewLifecycleOwner) { user ->
            if (user is ProfileState.UserState) {

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
            .setTitle("Select show rating")
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
    ): (DialogInterface, Int) -> Unit {
        return { _, itemIndex: Int ->
            lifecycleScope.launch {
                val ratedSuccessfully = tvShowDetailsVM.rateTvShow(
                    tvShowId,
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
    ): (DialogInterface, Int) -> Unit {
        return { _, _ ->
            lifecycleScope.launch {
                val ratedSuccessfully = tvShowDetailsVM.removeTvShowRating(
                    tvShowId,
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