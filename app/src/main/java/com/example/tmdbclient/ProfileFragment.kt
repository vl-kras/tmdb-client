package com.example.tmdbclient

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.tmdbclient.MainActivity.Companion.SESSION_ID_TAG
import kotlin.concurrent.thread

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private val viewModel : ProfileViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.btn_guest_login).setOnClickListener {

            val username = view.findViewById<EditText>(R.id.username_input).text.toString()
            val password = view.findViewById<EditText>(R.id.password_input).text.toString()

            val sessionId = viewModel.createSession(username, password)
            requireActivity().getPreferences(Context.MODE_PRIVATE).edit()
                .putString(SESSION_ID_TAG, sessionId)
                .apply()
        }

        view.findViewById<Button>(R.id.btn_logout).setOnClickListener {
            viewModel.logout()
            requireActivity().getPreferences(Context.MODE_PRIVATE).edit()
                .remove(SESSION_ID_TAG)
                .apply()
        }

        viewModel.profile.observe(viewLifecycleOwner) {
            view.findViewById<TextView>(R.id.profile_info)?.text = it.sessionId

        }
    }
}