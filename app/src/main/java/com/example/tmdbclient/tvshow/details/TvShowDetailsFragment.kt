package com.example.tmdbclient.tvshow.details

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.tmdbclient.R
import com.example.tmdbclient.shared.TmdbBasePaths
import com.example.tmdbclient.databinding.FragmentTvshowDetailsBinding
import com.example.tmdbclient.profile.ProfileState
import com.example.tmdbclient.profile.ProfileViewModel

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

        tvShowDetailsVM.getState().observe(viewLifecycleOwner) { state ->
            observeStateChanges(state)
        }
    }

    override fun onDestroyView() {

        super.onDestroyView()
        _binding = null
    }

    private fun observeStateChanges(state: TvShowDetailsState) {
        configureViews(state)
        displayState(state)
    }

    private fun configureViews(state: TvShowDetailsState) {

        when (state) {
            is TvShowDetailsState.Initial ->{
                val action = TvShowDetailsState.Action.Load(args.showId)
                tvShowDetailsVM.handleAction(action)
            }
            is TvShowDetailsState.Display -> {

                setMoviePoster(state.content)
                configureRatingButton()

                with (binding) {
                    name.text = state.content.title
                    tvshowStatus.text = state.content.status
                    genres.text = state.content.genres.joinToString { it }
                    tagline.text = state.content.tagline
                    overview.text = state.content.overview
                    userScore.text = "${state.content.userScore.times(10)}%"
                }
            }
            is TvShowDetailsState.Error -> {
                binding.statusMessage.text = state.exception.message
            }
        }
    }

    private fun displayState(state: TvShowDetailsState) {

        with(binding) {
            name.isVisible = state is TvShowDetailsState.Display
            tvshowStatus.isVisible = state is TvShowDetailsState.Display
            genres.isVisible = state is TvShowDetailsState.Display
            tagline.isVisible = state is TvShowDetailsState.Display
            overview.isVisible = state is TvShowDetailsState.Display
            userScore.isVisible = state is TvShowDetailsState.Display

            giveRating.isVisible = (state is TvShowDetailsState.Display) and
                    (profileVM.getProfile().value is ProfileState.UserState)

            loadingIndicator.isVisible = state is TvShowDetailsState.Loading

            statusMessage.isVisible = state is TvShowDetailsState.Error
            retryButton.isVisible = state is TvShowDetailsState.Error
        }
    }

    private fun setMoviePoster(show: TvShowDetailsRepository.TvShowDetails) {
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
            val action = TvShowDetailsState.Action.PostRating(
                sessionId,
                tvShowId,
                values[itemIndex].toFloat()
            )
            tvShowDetailsVM.handleAction(action)
        }
    }

    private fun getRatingDialogNeutralAction(
        sessionId: String
    ): (DialogInterface, Int) -> Unit {

        return { _, _ ->
            val action = TvShowDetailsState.Action.DeleteRating(sessionId, args.showId)
            tvShowDetailsVM.handleAction(action)
        }
    }
}