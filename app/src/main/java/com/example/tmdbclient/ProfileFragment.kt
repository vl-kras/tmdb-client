package com.example.tmdbclient

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.tmdbclient.MainActivity.Companion.SESSION_ID_TAG
import kotlin.Exception

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    //TODO make destination fragment from here

    private val viewModel : ProfileViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sessionIdView = view.findViewById<TextView>(R.id.session_id)
        val accountIdView = view.findViewById<TextView>(R.id.account_id)
        val accountNameView = view.findViewById<TextView>(R.id.account_name)
        val accountUsernameView = view.findViewById<TextView>(R.id.account_username)

        val signInBtn = view.findViewById<Button>(R.id.login_button)
        val signOutBtn = view.findViewById<Button>(R.id.logout_button)

        //display user account details
        viewModel.profile.observe(viewLifecycleOwner) { user ->
            when (user) {
                is ProfileViewModel.AppSession.UserSession -> {

                    val prefs = requireActivity().getPreferences(Context.MODE_PRIVATE)
                    val currentSession = user.sessionId
                    val storedSession = prefs.getString(SESSION_ID_TAG, null)
                    if (currentSession != storedSession) {
                        prefs.edit { putString(SESSION_ID_TAG, user.sessionId) }
                    }

                    sessionIdView.text = user.sessionId
                    accountIdView.text = user.details.id.toString()
                    accountNameView.text = user.details.name
                    accountUsernameView.text = user.details.username

                    //write sessionId changes to persistent storage

                    //sign out button
                    signOutBtn.visibility = View.VISIBLE
                    signInBtn.visibility = View.GONE
                    signOutBtn.setOnClickListener {

                        AlertDialog.Builder(requireContext())
                            .setTitle("Confirm signing out")
                            .setPositiveButton("Confirm") { _, _ ->
                                viewModel.signOut()
                                //remove session cookies from persistent storage
                                prefs.edit { remove(SESSION_ID_TAG) }
                            }
                            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
                            .create()
                            .show()
                    }
                }
                is ProfileViewModel.AppSession.NoSession -> {
                    sessionIdView.text = ""
                    accountIdView.text = ""
                    accountNameView.text = ""
                    accountUsernameView.text = ""

                    //button that logs user in
                    signOutBtn.visibility = View.GONE
                    signInBtn.visibility = View.VISIBLE
                    signInBtn.setOnClickListener {

                        val usernameInput = EditText(context).apply {
                            hint = "Username"
                        }
                        val passwordInput = EditText(context).apply {
                            hint = "Password"
                        }
                        val dialogView = LinearLayout(context).apply {
                            orientation = LinearLayout.VERTICAL
                            addView(usernameInput)
                            addView(passwordInput)
                        }

                        AlertDialog.Builder(requireContext())
                            .setTitle("Your TMDB credentials")
                            .setView(dialogView)
                            .setPositiveButton("Sign in") { _, _ ->
                                viewModel.signIn(
                                    username = usernameInput.text.toString(),
                                    password = passwordInput.text.toString()
                                )
                            }
                            .setNegativeButton("Cancel") { dialog,_ -> dialog.cancel() }
                            .create()
                            .show()
                    }
                }
            }
        }
    }
}