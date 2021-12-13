package com.example.tmdbclient

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.tmdbclient.MainActivity.Companion.SESSION_ID_TAG
import com.example.tmdbclient.databinding.FragmentProfileBinding
import com.google.android.material.snackbar.Snackbar

class ProfileFragment : Fragment() {

    //TODO make destination fragment from here

    private val viewModel: ProfileViewModel by activityViewModels()

    //binding is not-null when fragment's View exists
    private var binding: FragmentProfileBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentProfileBinding.inflate(inflater)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //display user account details
        viewModel.profile.observe(viewLifecycleOwner) { user ->
            when (user) {
                is ProfileViewModel.AppSession.UserSession -> {
                    displaySignedInState(user)
                    //write sessionId changes to persistent storage
                    storeSessionId(user.sessionId)
                }
                is ProfileViewModel.AppSession.NoSession -> {
                    displaySignedOutState()
                }
            }
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    private fun createSignInDialog(): AlertDialog {

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
        return AlertDialog.Builder(requireContext())
            .setTitle("Your TMDB credentials")
            .setView(dialogView)
            .setPositiveButton("Sign in") { _, _ ->
                val success = viewModel.signIn(
                    username = usernameInput.text.toString(),
                    password = passwordInput.text.toString()
                )
                val message = if (success) {
                    "Signed in successfully"
                } else {
                    "Failed to sign in"
                }
                Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            .create()

    }

    private fun createSignOutDialog(): AlertDialog {
        return AlertDialog.Builder(requireContext())
            .setTitle("Confirm signing out")
            .setPositiveButton("Confirm", getSignOutDialogPositiveAction())
            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            .create()
    }

    private fun getSignOutDialogPositiveAction(): DialogInterface.OnClickListener {
        return DialogInterface.OnClickListener { _, _ ->

            val success = viewModel.signOut()
            val message = if (success) {
                "Signed out successfully"
            } else {
                "Failed to sign out correctly"
            }
            Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()

            //remove session cookies from persistent storage
            val prefs = requireActivity().getPreferences(Context.MODE_PRIVATE)
            prefs.edit { remove(SESSION_ID_TAG) }
        }
    }

    private fun storeSessionId(sessionId: String) {
        val prefs = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val storedSession = prefs.getString(SESSION_ID_TAG, null)
        if (sessionId != storedSession) {
            prefs.edit { putString(SESSION_ID_TAG, sessionId) }
        }
    }

    private fun displaySignedInState(user: ProfileViewModel.AppSession.UserSession) {
        binding!!.sessionId.text = user.sessionId
        binding!!.accountId.text = user.details.id.toString()
        binding!!.accountName.text = user.details.name
        binding!!.accountUsername.text = user.details.username
        binding!!.signInButton.visibility = View.GONE

        //sign out button
        binding!!.signOutButton.visibility = View.VISIBLE
        binding!!.signOutButton.setOnClickListener {
            createSignOutDialog().show()
        }
    }

    private fun displaySignedOutState() {
        binding!!.sessionId.text = ""
        binding!!.accountId.text = ""
        binding!!.accountName.text = ""
        binding!!.accountUsername.text = ""
        binding!!.signOutButton.visibility = View.GONE

        //button that logs user in
        binding!!.signInButton.visibility = View.VISIBLE
        binding!!.signInButton.setOnClickListener{
            createSignInDialog().show()
        }
    }


}