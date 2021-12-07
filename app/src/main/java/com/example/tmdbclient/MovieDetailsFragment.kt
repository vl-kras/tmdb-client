package com.example.tmdbclient

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MovieDetailsFragment : Fragment(R.layout.fragment_movie_details) {

    private val profileVM: ProfileViewModel by activityViewModels()
    private val movieDetailsVM: MovieDetailsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val movieId = arguments?.getInt("movieId")
            ?: throw NullPointerException("Passed MovieID cannot be null'")

        lifecycleScope.launch {
            val movieDetails = movieDetailsVM.getMovieById(movieId)
            view.findViewById<TextView>(R.id.movie_details).text = movieDetails.toString()
        }
    }
}