package com.isis3510.growhub.view.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.isis3510.growhub.R
import com.isis3510.growhub.offline.NetworkUtils
import com.isis3510.growhub.viewmodel.AuthViewModel

/* ---- límites ---- */
private const val MAX_NAME = 40
private const val MAX_EMAIL = 30
private const val MAX_PASS = 30

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel = viewModel(),
    onNavigateToInterests: () -> Unit,
    onNavigateBack: () -> Unit
) {
    /* 🔑 Reset del estado al montar el composable */
    /*LaunchedEffect(Unit) { viewModel.startFreshRegistration() } */

    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val primaryBlue = Color(0xFF5669FF)

    /* visibilidad y errores */
    var passVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }

    var errName by remember { mutableStateOf<String?>(null) }
    var errEmail by remember { mutableStateOf<String?>(null) }
    var errRole by remember { mutableStateOf<String?>(null) }
    var errPass by remember { mutableStateOf<String?>(null) }
    var errCPass by remember { mutableStateOf<String?>(null) }

    var roleMenuExpanded by remember { mutableStateOf(false) }
    val roles = listOf("Host", "Attendee")
    val selectedRole = if (uiState.userRole.isNotBlank()) uiState.userRole else "Select a role"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        /* back */
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .size(60.dp)
                .padding(top = 16.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
        }

        Text(
            text = "Sign up",
            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 30.sp),
            modifier = Modifier.padding(start = 8.dp, top = 80.dp, bottom = 12.dp)
        )

        /* -------- Nombre -------- */
        OutlinedTextField(
            value = uiState.name,
            onValueChange = {
                if (it.length <= MAX_NAME) {
                    viewModel.onNameChange(it); errName = null
                }
            },
            label = { Text("Full name") },
            isError = errName != null,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_email),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
        )
        errName?.let { Error(it) }

        /* -------- Email -------- */
        OutlinedTextField(
            value = uiState.email,
            onValueChange = {
                if (it.length <= MAX_EMAIL) {
                    viewModel.onEmailChange(it); errEmail = null
                }
            },
            label = { Text("Email") },
            isError = errEmail != null,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_email),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
        )
        errEmail?.let { Error(it) }

        /* -------- Role -------- */
        Box {
            OutlinedTextField(
                value = selectedRole,
                onValueChange = {},
                readOnly = true,
                label = { Text("User Role") },
                isError = errRole != null,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_check),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { roleMenuExpanded = !roleMenuExpanded }) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_drop_down),
                            contentDescription = null
                        )
                    }
                }
            )
            DropdownMenu(expanded = roleMenuExpanded, onDismissRequest = { roleMenuExpanded = false }) {
                roles.forEach { role ->
                    DropdownMenuItem(
                        text = { Text(role) },
                        onClick = {
                            viewModel.onUserRoleChange(role)
                            errRole = null
                            roleMenuExpanded = false
                        }
                    )
                }
            }
        }
        errRole?.let { Error(it) }

        /* -------- Password -------- */
        OutlinedTextField(
            value = uiState.password,
            onValueChange = {
                if (it.length <= MAX_PASS) { viewModel.onPasswordChange(it); errPass = null }
            },
            label = { Text("Your password") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (passVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passVisible = !passVisible }) {
                    Icon(
                        painter = painterResource(
                            id = if (passVisible) R.drawable.ic_eye_closed else R.drawable.ic_eye_open
                        ),
                        contentDescription = null
                    )
                }
            },
            isError = errPass != null,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_lock),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
        )
        errPass?.let { Error(it) }

        /* -------- Confirm Password -------- */
        OutlinedTextField(
            value = uiState.confirmPassword,
            onValueChange = {
                if (it.length <= MAX_PASS) { viewModel.onConfirmPasswordChange(it); errCPass = null }
            },
            label = { Text("Confirm password") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { confirmVisible = !confirmVisible }) {
                    Icon(
                        painter = painterResource(
                            id = if (confirmVisible) R.drawable.ic_eye_closed else R.drawable.ic_eye_open
                        ),
                        contentDescription = null
                    )
                }
            },
            isError = errCPass != null,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_lock),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
        )
        errCPass?.let { Error(it) }

        Spacer(Modifier.height(20.dp))

        if (uiState.isLoading) {
            CircularProgressIndicator(); Spacer(Modifier.height(16.dp))
        }

        uiState.errorMessage?.let { Error(it) }

        Button(
            onClick = {
                if (!NetworkUtils.isNetworkAvailable(context)) {
                    Toast.makeText(context, "No internet connection", Toast.LENGTH_LONG).show(); return@Button
                }
                val ok = validate(
                    uiState,
                    { errName = it },
                    { errEmail = it },
                    { errRole = it },
                    { errPass = it },
                    { errCPass = it }
                )
                if (ok) {
                    viewModel.updateRegistrationData(
                        name = uiState.name,
                        email = uiState.email,
                        password = uiState.password,
                        confirmPassword = uiState.confirmPassword,
                        userRole = uiState.userRole
                    )
                    onNavigateToInterests()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !uiState.isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = primaryBlue)
        ) { Text("SIGN UP", color = Color.White) }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Already have an account? ")
            TextButton(onClick = onNavigateBack) { Text("Sign In", color = primaryBlue) }
        }
    }
}

/* Helpers */
@Composable private fun Error(msg: String) = Text(
    msg, color = MaterialTheme.colorScheme.error, fontSize = 12.sp,
    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
)

private fun validate(
    state: com.isis3510.growhub.model.objects.AuthUiState,
    setNameErr: (String?) -> Unit,
    setEmailErr: (String?) -> Unit,
    setRoleErr: (String?) -> Unit,
    setPassErr: (String?) -> Unit,
    setCPassErr: (String?) -> Unit
): Boolean {
    var ok = true

    /* ---- nombre, email, rol (sin cambios) ---- */
    if (state.name.isBlank())  { setNameErr("Please enter your full name."); ok = false }
    if (state.email.isBlank()) { setEmailErr("Please enter your email.");     ok = false }
    else if (!state.email.contains("@") || !state.email.endsWith(".com")) {
        setEmailErr("Please enter a valid email address.");                   ok = false
    }
    if (state.userRole.isBlank()) { setRoleErr("Please select a user role."); ok = false }

    /* ---- contraseñas ---- */
    if (state.password.isBlank())        { setPassErr("Please enter a password.");         ok = false }
    if (state.confirmPassword.isBlank()) { setCPassErr("Please confirm your password.");   ok = false }

    if (ok && state.password != state.confirmPassword) {
        setPassErr("Passwords do not match.")
        setCPassErr("Passwords do not match.")
        ok = false
    }

    return ok
}
