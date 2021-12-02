package com.example.tmdbclient

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private val viewModel : ProfileViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel

        viewModel.profile.observe(viewLifecycleOwner) {
            view.findViewById<TextView>(R.id.profile)?.text = it.toString()
        }

    }
}