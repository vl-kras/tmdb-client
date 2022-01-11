package com.example.tmdbclient.profile.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import com.example.tmdbclient.shared.SESSION_ID_KEY
import com.example.tmdbclient.shared.datastore
import com.example.tmdbclient.shared.theme.Typography
import com.example.tmdbclient.tvshow.list.ui.LoadingIndicator
import kotlinx.coroutines.launch

// tries to be Humble (mostly responsible for drawing the UI)

@Composable
fun ProfileScreen(profileVM: ProfileViewModel, context: Context) {

    val state by profileVM.getState().collectAsState()

    var message by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }

    //TODO implement Scaffold + Snackbars + actionOnResultCallback for other screens

    Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) } ) {

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            when (state) {
                is ProfileState.InitialState -> {
                    InitialState()
                }
                is ProfileState.EmptyState -> {
                    NoProfile(
                        profileVM = profileVM,
                        onActionResult = { message = it }
                    )
                }
                is ProfileState.UserState -> {
                    ActiveUser(
                        state = state as ProfileState.UserState,
                        profileVM =  profileVM,
                        context = context,
                        onActionResult = { message = it }
                    )
                }
            }
        }
    }

    val coroutineScope = rememberCoroutineScope()

    if (message.isNotBlank()) {
        SideEffect {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(message)
            }
        }
    }
}

@Composable
fun InitialState() {
    LoadingIndicator()

}

@Composable
fun ActiveUser(
    state: ProfileState.UserState,
    profileVM: ProfileViewModel,
    context: Context,
    onActionResult: (String)-> Unit
) {

    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = state.sessionId) {
        context.datastore.edit { cookies ->
            cookies[SESSION_ID_KEY] = state.sessionId
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Signed in as ${state.name}",
            style = Typography.subtitle1
        )
        Text(
            text = "(${state.username})",
            style = Typography.subtitle2
        )
        Text(
            text = "Session ID: ${state.sessionId}",
            style = Typography.overline
        )


        Button(
            onClick = {
                showDialog = true
            }
        ) {
            Text(text = "Sign out")
        }
    }
    SignOutDialog(context, profileVM, showDialog, onChanged = { showDialog = it }, onActionResult)
}

@Composable
fun SignOutDialog(
    context: Context,
    profileVM: ProfileViewModel,
    showDialog: Boolean,
    onChanged: (Boolean) -> Unit,
    onActionResult: (String) -> Unit
) {

    val coroutineScope = rememberCoroutineScope()

    val callback: (Result<String>) -> Unit ={
        it.onSuccess { message ->
            onActionResult(message)
        }.onFailure { error ->
            onActionResult(error.message ?: "Something went wrong")
        }
    }

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
                                ProfileState.Action.SignOut(callback)
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
fun NoProfile(profileVM: ProfileViewModel, onActionResult: (String)-> Unit) {

    var isDialogShowing by remember { mutableStateOf(false) }
    SignInDialog(profileVM, isDialogShowing, onChanged = { isDialogShowing = it }, onActionResult)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "Sign in to view your profile info")
        Button(
            onClick = {
                isDialogShowing = true
            }
        ) {
            Text(text = "Sign in")
        }
    }
}

@Composable
fun SignInDialog(
    profileVM: ProfileViewModel,
    openDialog: Boolean,
    onChanged: (Boolean) -> Unit,
    onActionResult: (String)-> Unit
) {

    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    if (openDialog) {
        AlertDialog(
            onDismissRequest = { onChanged(false) },
            title = { Text(text = "Enter your TMDB credentials") },
            text = {
                Column {
                    LoginInput(login, onChanged = { login = it })
                    PasswordInput(password, onChanged = { password = it })
                }
            },
            confirmButton = {
                ConfirmButton(login, password, profileVM, onActionResult)
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
fun ConfirmButton(
    login: String,
    password: String,
    profileVM: ProfileViewModel,
    onActionResult: (String)-> Unit
) {

    val coroutineScope = rememberCoroutineScope()

    val callback: (Result<String>) -> Unit = { result ->
        result.onSuccess { message ->
            onActionResult(message)
        }.onFailure { error ->
            onActionResult(error.message ?: "Something went wrong")
        }
    }

    var isProcessing by remember { mutableStateOf(false) }

    val onClick: () -> Unit = {
        coroutineScope.launch {
            isProcessing = true
            profileVM.handleAction(
                ProfileState.Action.SignIn(
                    username = login,
                    password = password,
                    onResult = callback
                )
            )
            isProcessing = false
        }
    }
    if (isProcessing) {
        LoadingIndicator()
    } else {
        TextButton(
            onClick = onClick
        ) {
            Text(text = "Sign in")
        }
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

