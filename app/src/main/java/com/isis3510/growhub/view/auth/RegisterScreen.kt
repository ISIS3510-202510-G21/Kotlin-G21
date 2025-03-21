package com.isis3510.growhub.view.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.isis3510.growhub.FakeApplication
import com.isis3510.growhub.R
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

    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var roleError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    // Para menú básico (sin ExposedDropdownMenu)
    var roleMenuExpanded by remember { mutableStateOf(false) }
    val selectedRole = uiState.userRole ?: "Select a role"
    val roles = listOf("Host", "Attendee")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        IconButton(
            onClick = { onNavigateBack() },
            modifier = Modifier
                .size(60.dp)
                .padding(top = 16.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Go back",
                tint = Color.Black
            )
        }

        Text(
            text = "Sign up",
            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 30.sp),
            modifier = Modifier.padding(start = 8.dp, top = 80.dp, bottom = 12.dp)
        )

        // Full Name
        OutlinedTextField(
            value = uiState.name,
            onValueChange = {
                viewModel.onNameChange(it)
                nameError = null
            },
            label = { Text("Full name") },
            isError = (nameError != null),
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.image_30113480),
                    contentDescription = "User Icon"
                )
            }
        )
        nameError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )
        }

        // Email
        OutlinedTextField(
            value = uiState.email,
            onValueChange = {
                viewModel.onEmailChange(it)
                emailError = null
            },
            label = { Text("Email") },
            isError = (emailError != null),
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.image_30113480),
                    contentDescription = "Email Icon"
                )
            }
        )
        emailError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )
        }

        // User Role (DropdownMenu SIN ExposedDropdownMenu)
        Box {
            OutlinedTextField(
                value = selectedRole,
                onValueChange = {},
                readOnly = true,
                label = { Text("User Role") },
                isError = (roleError != null),
                modifier = Modifier
                    .fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.image_30113480),
                        contentDescription = "Role Icon"
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { roleMenuExpanded = !roleMenuExpanded }) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_drop_down),
                            contentDescription = "Expand or Collapse"
                        )
                    }
                }
            )
            DropdownMenu(
                expanded = roleMenuExpanded,
                onDismissRequest = { roleMenuExpanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                roles.forEach { role ->
                    DropdownMenuItem(
                        text = { Text(role) },
                        onClick = {
                            viewModel.onUserRoleChange(role)
                            roleError = null
                            roleMenuExpanded = false
                        }
                    )
                }
            }
        }
        roleError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )
        }

        // Password
        OutlinedTextField(
            value = uiState.password,
            onValueChange = {
                viewModel.onPasswordChange(it)
                passwordError = null
            },
            label = { Text("Your password") },
            visualTransformation =
            if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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
            isError = (passwordError != null),
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.image_3147576),
                    contentDescription = "Lock Icon"
                )
            }
        )
        passwordError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )
        }

        // Confirm Password
        OutlinedTextField(
            value = uiState.confirmPassword,
            onValueChange = {
                viewModel.onConfirmPasswordChange(it)
                confirmPasswordError = null
            },
            label = { Text("Confirm password") },
            visualTransformation =
            if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(
                        painter = painterResource(
                            id = if (confirmPasswordVisible) R.drawable.ic_eye_closed
                            else R.drawable.ic_eye_open
                        ),
                        contentDescription =
                        if (confirmPasswordVisible) "Hide password" else "Show password"
                    )
                }
            },
            isError = (confirmPasswordError != null),
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.image_3147576),
                    contentDescription = "Lock Icon"
                )
            }
        )
        confirmPasswordError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (uiState.isLoading) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
        }

        uiState.errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Button(
            onClick = {
                var hasError = false
                if (uiState.name.isBlank()) {
                    nameError = "Please enter your full name."
                    hasError = true
                }
                if (uiState.email.isBlank()) {
                    emailError = "Please enter your email."
                    hasError = true
                } else if (!uiState.email.contains("@") || !uiState.email.endsWith(".com")) {
                    emailError = "Please enter a valid email address"
                    hasError = true
                }
                if (uiState.userRole.isNullOrEmpty()) {
                    roleError = "Please select a user role."
                    hasError = true
                }
                if (uiState.password.isBlank()) {
                    passwordError = "Please enter a password."
                    hasError = true
                }
                if (uiState.confirmPassword.isBlank()) {
                    confirmPasswordError = "Please confirm your password."
                    hasError = true
                }
                if (!hasError) {
                    if (uiState.password != uiState.confirmPassword) {
                        confirmPasswordError = "Passwords do not match."
                    } else {
                        viewModel.registerUser { onRegisterSuccess() }
                    }
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