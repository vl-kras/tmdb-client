package com.example.tmdbclient

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.tmdbclient.MainActivity.Companion.SESSION_ID_TAG

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private val viewModel : ProfileViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val accountIdView = view.findViewById<TextView>(R.id.account_id)
        val accountNameView = view.findViewById<TextView>(R.id.account_name)
        val accountUsernameView = view.findViewById<TextView>(R.id.account_username)

        //display user account details
        viewModel.account.observe(viewLifecycleOwner) {
            accountIdView.text = it.id.toString()
            accountNameView.text = it.name
            accountUsernameView.text = it.username
        }

        //button that logs user in
        view.findViewById<Button>(R.id.login_button).setOnClickListener {

            val username = view.findViewById<EditText>(R.id.username_input).text.toString()
            val password = view.findViewById<EditText>(R.id.password_input).text.toString()
            viewModel.login(username, password)
        }

        //button that logs user out
        view.findViewById<Button>(R.id.logout_button).setOnClickListener {

            AlertDialog.Builder(requireActivity())
                .setMessage("Confirm logging out")
                .setPositiveButton("Confirm") { _, _ -> viewModel.logout() } //log out
                .setNegativeButton("Cancel") { _, _ -> } //do nothing (remove dialog)
                .create()
                .show()
        }

        viewModel.profile.observe(viewLifecycleOwner) { session ->

            if (session.sessionId.isBlank()) {
                accountIdView.text = ""
                accountNameView.text = ""
                accountUsernameView.text = ""

            }
            view.findViewById<TextView>(R.id.session_id)?.text = session.sessionId

            //write sessionId changes to persistent storage
            val prefs = requireActivity().getPreferences(Context.MODE_PRIVATE)
            if (session.sessionId.isNotBlank()) {
                prefs.edit { putString(SESSION_ID_TAG, session.sessionId) } //new session
            } else {
                prefs.edit { remove(SESSION_ID_TAG) } //user logged out
            }
        }
    }
}