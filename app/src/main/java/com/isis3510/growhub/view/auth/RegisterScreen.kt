package com.isis3510.growhub.view.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import com.isis3510.growhub.R
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.isis3510.growhub.FakeApplication
import com.isis3510.growhub.view.theme.GrowhubTheme
import com.isis3510.growhub.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel = viewModel(),
    onRegisterSuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val primaryBlue = Color(0xFF5669FF)

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Flecha de regreso más grande
        IconButton(
            onClick = { onNavigateBack() },
            modifier = Modifier
                .size(60.dp)
                .padding(top = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Go back",
                tint = Color.Black
            )
        }

        // Título "Sign up" alineado a la izquierda
        Text(
            text = "Sign up",
            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 30.sp),
            modifier = Modifier.padding(start = 8.dp, top = 80.dp, bottom = 12.dp)
        )

        // Full Name
        OutlinedTextField(
            value = uiState.name,
            onValueChange = { viewModel.onNameChange(it) },
            label = { Text("Full name") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        // Email
        OutlinedTextField(
            value = uiState.email,
            onValueChange = { viewModel.onEmailChange(it) },
            label = { Text("Email") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        // Password
        OutlinedTextField(
            value = uiState.password,
            onValueChange = { viewModel.onPasswordChange(it) },
            label = { Text("Your password") },
            visualTransformation = if (passwordVisible)
                VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        painter = painterResource(
                            id = if (passwordVisible) R.drawable.ic_eye_closed
                            else R.drawable.ic_eye_open
                        ),
                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                    )
                }
            },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        // Confirm Password
        OutlinedTextField(
            value = uiState.confirmPassword,
            onValueChange = { viewModel.onConfirmPasswordChange(it) },
            label = { Text("Confirm password") },
            visualTransformation = if (confirmPasswordVisible)
                VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(
                        painter = painterResource(
                            id = if (passwordVisible) R.drawable.ic_eye_closed
                            else R.drawable.ic_eye_open
                        ),
                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                    )
                }
            },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        )

        // User Type

        var expanded by remember { mutableStateOf(false) }
        val selectedRole = uiState.userRole ?: "Select a role"


        val roles = listOf("Host", "Attendee") // Opciones del menú

        // Estado para manejar el error de selección del rol
        var roleError by remember { mutableStateOf(false) }

        // Menú desplegable para seleccionar el rol del usuario
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedRole,
                onValueChange = {},
                readOnly = true,
                label = { Text("User Role") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                isError = roleError,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                roles.forEach { role ->
                    DropdownMenuItem(
                        text = { Text(role) },
                        onClick = {
                            viewModel.onUserRoleChange(role)
                            roleError = false
                            expanded = false
                        }
                    )
                }
            }
        }



        // Loading indicator
        if (uiState.isLoading) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Error message
        uiState.errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Botón "Sign Up"
        Button(
            onClick = {
                if (uiState.userRole.isNullOrEmpty()) {
                    roleError = true // ✅ Activa el error si el usuario no selecciona un rol
                } else {
                    viewModel.registerUser { onRegisterSuccess() }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
            enabled = !uiState.isLoading
        ) {
            Text(text = "SIGN UP", color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Texto "Already have an account?" alineado correctamente
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Already have an account? ")
            TextButton(onClick = { onNavigateBack() }) {
                Text(text = "Sign In", color = primaryBlue)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    val fakeApp = FakeApplication()
    val previewViewModel = AuthViewModel(fakeApp).apply {
        onNameChange("John Doe")
        onEmailChange("new@user.com")
        onPasswordChange("password")
        onConfirmPasswordChange("password")
        onUserRoleChange("Host")
    }

    GrowhubTheme {
        RegisterScreen(
            viewModel = previewViewModel,
            onRegisterSuccess = {},
            onNavigateBack = {}
        )
    }
}
