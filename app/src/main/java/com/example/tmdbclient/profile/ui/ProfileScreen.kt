package com.example.tmdbclient.profile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.tmdbclient.shared.theme.Typography
import com.example.tmdbclient.tvshow.list.ui.LoadingIndicator
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch

// tries to be Humble (mostly responsible for drawing the UI)

@Composable
fun ProfileScreen(profileVM: ProfileViewModel, writeSessionId: (String) -> Unit) {

    val state by profileVM.getState().collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    val coroutineScope = rememberCoroutineScope()
    val channel = Channel<String>()

    SideEffect {
        coroutineScope.launch {
            channel.consumeEach { message ->
                snackbarHostState.showSnackbar(message)
            }
        }
    }

    val sendMessage: (String) -> Unit = {
        coroutineScope.launch {
            channel.send(it)
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) } ) {

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            when (state) {
                is ProfileState.Initial -> {
                    InitialState()
                }
                is ProfileState.NoSession -> {
                    writeSessionId("")
                    NoSessionState(
                        profileVM = profileVM,
                        onActionResult = { sendMessage(it) }
                    )
                }
                is ProfileState.ActiveSession -> {
                    writeSessionId((state as ProfileState.ActiveSession).sessionId)
                    ActiveUser(
                        state = state as ProfileState.ActiveSession,
                        profileVM =  profileVM,
                        onActionResult = { sendMessage(it) }
                    )
                }
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
    state: ProfileState.ActiveSession,
    profileVM: ProfileViewModel,
    onActionResult: (String)-> Unit
) {

    var isSignOutDialogShowing by remember { mutableStateOf(false) }
    val setIsSignOutDialogShowing: (Boolean) -> Unit = {
        isSignOutDialogShowing = it
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ProfileName(state.name)
        ProfileUsernameAndId(state.username, state.userId)
        ProfileSessionId(state.sessionId)
        SignOutDialogButton(setIsSignOutDialogShowing)
    }
    SignOutDialog(profileVM,
        isSignOutDialogShowing, setIsSignOutDialogShowing,
        onActionResult
    )
}

@Composable
fun SignOutDialogButton(setIsDialogShowing: (Boolean) -> Unit) {
    Button(onClick = { setIsDialogShowing(true) }) {
        Text(text = "Sign out")
    }
}

@Composable
fun ProfileSessionId(sessionId: String) {
    Text(
        text = "Session ID: $sessionId",
        style = Typography.overline
    )
}

@Composable
fun ProfileName(name: String) {
    Text(
        text = "Signed in as $name",
        style = Typography.subtitle1
    )
}

@Composable
fun ProfileUsernameAndId(username: String, userId: Int) {
    Text(
        text = "(${username}#ID-${userId})",
        style = Typography.subtitle2
    )
}

@Composable
fun SignOutDialog(
    profileVM: ProfileViewModel,
    showDialog: Boolean,
    onChanged: (Boolean) -> Unit,
    onActionResult: (String) -> Unit
) {

    val coroutineScope = rememberCoroutineScope()

    var isProcessing by remember { mutableStateOf(false) }

    val onConfirm: () -> Unit = {
        coroutineScope.launch {
            isProcessing = true
            profileVM.handleAction(
                action = ProfileState.Action.SignOut(onActionResult)
            )
            isProcessing = false
        }
    }

    if (showDialog) {
        if (!isProcessing) {
            AlertDialog(
                onDismissRequest = { onChanged(false) },
                text = { ConfirmSigningOutText() },
                confirmButton = {
                    ConfirmSigningOutButton(onConfirm)
                },
                dismissButton = { DialogCancelButton( cancelDialog = { onChanged(false) } ) }
            )
        } else {
            ProcessingAction()
        }
    }
}

@Composable
fun ConfirmSigningOutText() {
    Text(text = "Are you sure you want to sign out?")
}

@Composable
fun ConfirmSigningOutButton(onConfirm: () -> Unit) {
    TextButton(
        onClick = onConfirm
    ) {
        Text(text = "Confirm")
    }
}

@Composable
fun NoSessionState(profileVM: ProfileViewModel, onActionResult: (String)-> Unit) {

    var isDialogShowing by remember { mutableStateOf(false) }
    SignInDialog(profileVM, isDialogShowing, setIsDialogOpen = { isDialogShowing = it }, onActionResult)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "Sign in to view your profile info")
        ShowSignInDialogButton(showDialog = { isDialogShowing = true })
    }
}

@Composable
fun ShowSignInDialogButton(showDialog: () -> Unit) {
    Button(onClick = showDialog) {
        Text(text = "Sign in")
    }
}

@Composable
fun SignInDialog(
    profileVM: ProfileViewModel,
    isDialogOpen: Boolean,
    setIsDialogOpen: (Boolean) -> Unit,
    onActionResult: (String)-> Unit
) {

    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    var isProcessing by remember { mutableStateOf(false) }

    val onClick: () -> Unit = {
        coroutineScope.launch {
            isProcessing = true
            profileVM.handleAction(
                ProfileState.Action.SignIn(
                    username = login,
                    password = password,
                    onResult = onActionResult
                )
            )
        }.invokeOnCompletion {
            isProcessing = false
        }
    }

    if (isDialogOpen) {
        if (!isProcessing) {
            AlertDialog(
                onDismissRequest = { setIsDialogOpen(false) },
                title = { Text(text = "Enter your TMDB credentials") },
                text = {
                    Column {
                        LoginInput(login, setValue = { login = it })
                        PasswordInput(password, setValue = { password = it })
                    }
                },
                confirmButton = {
                    ConfirmSignInButton(onClick)
                },
                dismissButton = {
                    DialogCancelButton(cancelDialog = { setIsDialogOpen(false) })
                }
            )
        } else {
            ProcessingAction()
        }
    }
}

@Composable
fun ProcessingAction() {
    Dialog(onDismissRequest = { }) {
        LoadingIndicator()
    }
}

@Composable
fun LoginInput(login: String, setValue: (String) -> Unit) {

    TextField(
        placeholder = { Text(text = "Login") },
        value = login,
        onValueChange = { setValue(it) }
    )
}

@Composable
fun PasswordInput(password: String, setValue: (String) -> Unit) {
    TextField(
        placeholder = { Text(text = "Password") },
        value = password,
        onValueChange = { setValue(it) },
        visualTransformation = PasswordVisualTransformation('*')
    )
}

@Composable
fun ConfirmSignInButton(confirmSignIn: () -> Unit) {
    TextButton(onClick = confirmSignIn) {
        Text(text = "Sign in")
    }
}

@Composable
fun DialogCancelButton(cancelDialog: () -> Unit) {
    TextButton(onClick = cancelDialog) {
        Text(text = "Cancel")
    }
}