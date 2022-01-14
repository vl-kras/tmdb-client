package com.example.tmdbclient.profile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.tmdbclient.R
import com.example.tmdbclient.shared.LocalProfileVM
import com.example.tmdbclient.shared.LocalSessionIDWriter
import com.example.tmdbclient.shared.theme.Typography
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch

// tries to be Humble (mostly responsible for drawing the UI)

@Composable
fun ProfileScreen() {

    val state by LocalProfileVM.current.getState().collectAsState()

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
                    LocalSessionIDWriter.current("")
                    NoSessionState(
                        onActionResult = { sendMessage(it) }
                    )
                }
                is ProfileState.ActiveSession -> {
                    LocalSessionIDWriter.current((state as ProfileState.ActiveSession).sessionId)
                    ActiveUser(
                        state = state as ProfileState.ActiveSession,
                        onActionResult = { sendMessage(it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun InitialState() {
    LoadingIndicator()
}

@Composable
private fun ActiveUser(
    state: ProfileState.ActiveSession,
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
    SignOutDialog(
        isSignOutDialogShowing, setIsSignOutDialogShowing,
        onActionResult
    )
}

@Composable
private fun SignOutDialogButton(setIsDialogShowing: (Boolean) -> Unit) {
    Button(onClick = { setIsDialogShowing(true) }) {
        Text(text = stringResource(R.string.sign_out_button_text))
    }
}

@Composable
private fun ProfileSessionId(sessionId: String) {
    Text(
        text = "Session ID: $sessionId",
        style = Typography.overline
    )
}

@Composable
private fun ProfileName(name: String) {
    Text(
        text = "Signed in as $name",
        style = Typography.subtitle1
    )
}

@Composable
private fun ProfileUsernameAndId(username: String, userId: Int) {
    Text(
        text = "(${username}#ID-${userId})",
        style = Typography.subtitle2
    )
}

@Composable
private fun SignOutDialog(
    showDialog: Boolean,
    onChanged: (Boolean) -> Unit,
    onActionResult: (String) -> Unit
) {

    val coroutineScope = rememberCoroutineScope()

    var isProcessing by remember { mutableStateOf(false) }

    val profileVM = LocalProfileVM.current

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
private fun ConfirmSigningOutText() {
    Text(text = stringResource(R.string.confirm_sign_out_dialog_text))
}

@Composable
private fun ConfirmSigningOutButton(onConfirm: () -> Unit) {
    TextButton(
        onClick = onConfirm
    ) {
        Text(text = stringResource(R.string.confirm_sign_out_button_text))
    }
}

@Composable
private fun NoSessionState(onActionResult: (String)-> Unit) {

    var isDialogShowing by remember { mutableStateOf(false) }
    SignInDialog(isDialogShowing, setIsDialogOpen = { isDialogShowing = it }, onActionResult)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = stringResource(R.string.sign_in_message))
        ShowSignInDialogButton(showDialog = { isDialogShowing = true })
    }
}

@Composable
private fun ShowSignInDialogButton(showDialog: () -> Unit) {
    Button(onClick = showDialog) {
        Text(text = stringResource(R.string.sign_in_button_text))
    }
}

@Composable
private fun SignInDialog(
    isDialogOpen: Boolean,
    setIsDialogOpen: (Boolean) -> Unit,
    onActionResult: (String)-> Unit
) {

    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    var isProcessing by remember { mutableStateOf(false) }

    val profileVM = LocalProfileVM.current

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
                title = { Text(text = stringResource(R.string.sign_in_dialog_title)) },
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
private fun ProcessingAction() {
    Dialog(onDismissRequest = { }) {
        LoadingIndicator()
    }
}

@Composable
private fun LoginInput(login: String, setValue: (String) -> Unit) {

    TextField(
        placeholder = { Text(text = stringResource(R.string.username_input_hint)) },
        value = login,
        onValueChange = { setValue(it) }
    )
}

@Composable
private fun PasswordInput(password: String, setValue: (String) -> Unit) {
    TextField(
        placeholder = { Text(text = stringResource(R.string.password_input_hint)) },
        value = password,
        onValueChange = { setValue(it) },
        visualTransformation = PasswordVisualTransformation('*')
    )
}

@Composable
private fun ConfirmSignInButton(confirmSignIn: () -> Unit) {
    TextButton(onClick = confirmSignIn) {
        Text(text = stringResource(R.string.confirm_sign_in_button_text))
    }
}

@Composable
private fun DialogCancelButton(cancelDialog: () -> Unit) {
    TextButton(onClick = cancelDialog) {
        Text(text = stringResource(R.string.cancel_sign_in_button_text))
    }
}

@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}