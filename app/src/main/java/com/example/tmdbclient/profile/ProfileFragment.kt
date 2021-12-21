package com.example.tmdbclient.profile

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
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.tmdbclient.MainActivity.Companion.SESSION_ID_TAG
import com.example.tmdbclient.databinding.FragmentProfileBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch


// tries to be Humble (mostly responsible for drawing the UI)
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

        with(binding!!) {
            signInButton.setOnClickListener{
                createSignInDialog().show()
            }
            signOutButton.setOnClickListener {
                createSignOutDialog().show()
            }
        }

        //display user state
        viewModel.getProfile().observe(viewLifecycleOwner) { user ->
            if (user is ProfileState.UserState) {
                displayProfileDetails(user)
                storeSessionId(user.sessionId)
            }
            displayState(user)
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
                val action = ProfileState.Action.SignIn(
                    username = usernameInput.text.toString(),
                    password = passwordInput.text.toString()
                )
                lifecycleScope.launch {
                    viewModel.handleAction(action)
                }
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

            val message: String = try {
                lifecycleScope.launch {
                    viewModel.handleAction(ProfileState.Action.SignOut)
                }
                "Signed out successfully"
            } catch (e: IllegalStateException) {
                "Failed to sign out correctly"
            }
            Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()

            //remove session cookies from persistent storage
            val prefs = requireActivity().getPreferences(Context.MODE_PRIVATE)
            prefs.edit {
                remove(SESSION_ID_TAG)
            }
        }
    }

    private fun storeSessionId(sessionId: String) {
        val prefs = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val storedSession = prefs.getString(SESSION_ID_TAG, null)
        if (sessionId != storedSession) {
            prefs.edit {
                putString(SESSION_ID_TAG, sessionId)
            }
        }
    }

    private fun displayProfileDetails(userState: ProfileState.UserState) {
        with(binding!!) {
            sessionId.text = userState.sessionId
            accountId.text = userState.userId.toString()
            accountName.text = userState.name
            accountUsername.text = userState.username
        }
    }

    private fun displayState(state: ProfileState) {
        with(binding!!) {
            loadingIndicator.isVisible = state is ProfileState.Loading

            sessionId.isVisible = state is ProfileState.UserState
            accountId.isVisible = state is ProfileState.UserState
            accountName.isVisible = state is ProfileState.UserState
            accountUsername.isVisible = state is ProfileState.UserState

            signOutButton.isVisible = state is ProfileState.UserState
            signInButton.isVisible = (state is ProfileState.EmptyState) or (state is ProfileState.Error)

            if (state is ProfileState.Error) {
                Snackbar.make(view!!, state.exception.message.toString(), Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}