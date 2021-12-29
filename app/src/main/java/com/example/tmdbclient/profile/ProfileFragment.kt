package com.example.tmdbclient.profile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.tmdbclient.shared.SESSION_ID_KEY
import com.example.tmdbclient.shared.datastore
import com.example.tmdbclient.shared.theme.MyApplicationTheme
import kotlinx.coroutines.launch



// tries to be Humble (mostly responsible for drawing the UI)
class ProfileFragment : Fragment() {

//    val viewModel: ProfileViewModel by activityViewModels()

    //TODO make destination fragment from here

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply {
            setContent {
                MyApplicationTheme {
//                    ProfileScreen()
                }
            }
        }
    }


}

@Composable
fun ProfileScreen(profileVM: ProfileViewModel, context: Context) {

    val uiState by profileVM.getProfile().observeAsState()

    when (uiState) {
        is ProfileState.EmptyState -> {
            NoProfile(context, profileVM)
        }
        is ProfileState.UserState -> {
            ActiveUser(state = uiState as ProfileState.UserState, profileVM =  profileVM, context = context)
        }
    }
}

@Composable
fun ActiveUser(state: ProfileState.UserState, profileVM: ProfileViewModel, context: Context) {

    var showDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    SideEffect {
        coroutineScope.launch {
            context.datastore.edit { cookies ->
                cookies[SESSION_ID_KEY] = state.sessionId
            }
        }
    }

    Column {
        Text(text = state.sessionId)
        Text(text = state.username)
        Text(text = state.name)
        Button(
            onClick = {
                showDialog = true
            }
        ) {
            Text(text = "Sign out")
        }
    }
    SignOutDialog(context, profileVM, showDialog, onChanged = { showDialog = it })
}

@Composable
fun SignOutDialog(
    context: Context,
    profileVM: ProfileViewModel,
    showDialog: Boolean,
    onChanged: (Boolean) -> Unit
) {

    val coroutineScope = rememberCoroutineScope()

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { onChanged(false) },
            text = {
                Text(text = "Are you sure you want to sign out?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            profileVM.handleAction(
                                ProfileState.Action.SignOut
                            )
                            context.datastore.edit { cookies ->
                                // delete stored session ID
                                cookies.remove(SESSION_ID_KEY)
                            }
                        }
                    }
                ) {
                    Text(text = "Confirm")
                }
            },
            dismissButton = { DialogCancelButton(onChanged) }
        )
    }
}

@Composable
fun NoProfile(context: Context, profileVM: ProfileViewModel) {

    var openDialog by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = Unit) {
        context.datastore.edit { cookies ->
            // if there is stored session id, then restore session with it
            cookies[SESSION_ID_KEY]?.let { sessionId ->
                profileVM.handleAction(ProfileState.Action.Restore(sessionId))
            }
        }
    }

    Column {
        Text(text = "You are not signed in")
        Button(
            onClick = {
                openDialog = true
            }
        ) {
            Text(text = "Sign in")
        }
    }
    SignInDialog(profileVM, openDialog, onChanged = { openDialog = it })
}

@Composable
fun SignInDialog(
    profileVM: ProfileViewModel,
    openDialog: Boolean,
    onChanged: (Boolean) -> Unit
) {

    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    if (openDialog) {
        AlertDialog(
            onDismissRequest = { onChanged(false) },
            title = { Text(text = "Input your TMDB credentials") },
            text = {
                Column {
                    LoginInput(login, onChanged = { login = it })
                    PasswordInput(password, onChanged = { password = it })
                }
            },
            confirmButton = {
                ConfirmButton(login, password, profileVM)
            },
            dismissButton = {
                DialogCancelButton(onChanged)
            }
        )
    }
}

@Composable
fun LoginInput(login: String, onChanged: (String) -> Unit) {
    TextField(
        label = { Text(text = "Login") },
        value = login,
        onValueChange = { onChanged(it) }
    )
}

@Composable
fun PasswordInput(password: String, onChanged: (String) -> Unit) {
    TextField(
        label = { Text(text = "Password") },
        value = password,
        onValueChange = { onChanged(it) },
        visualTransformation = PasswordVisualTransformation('*')
    )
}

@Composable
fun ConfirmButton(login: String, password: String, profileVM: ProfileViewModel) {

    val coroutineScope = rememberCoroutineScope()

    val onClick: () -> Unit = {
        coroutineScope.launch {
            profileVM.handleAction(
                ProfileState.Action.SignIn(
                    username = login,
                    password = password
                )
            )
        }
    }
    TextButton(
        onClick = onClick
    ) {
        Text(text = "Sign in")
    }
}

@Composable
fun DialogCancelButton(onChanged: (Boolean) -> Unit) {
    TextButton(
        onClick = {
            onChanged(false)
        }
    ) {
        Text(text = "Cancel")
    }
}

