package com.isis3510.growhub.view.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.isis3510.growhub.FakeApplication
import com.isis3510.growhub.viewmodel.AuthViewModel
import com.isis3510.growhub.view.theme.GrowhubTheme
import com.isis3510.growhub.R
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

import androidx.compose.ui.text.input.KeyboardType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AuthViewModel = viewModel(),
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val primaryBlue = Color(0xFF5669FF)
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.growhub_icon),
                contentDescription = "GrowHub Icon",
                modifier = Modifier
                    .size(150.dp)
                    .padding(bottom = 8.dp)
            )
            Text(
                text = "GrowHub",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Sign in",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                textAlign = TextAlign.Left
            )
            OutlinedTextField(
                value = uiState.email,
                onValueChange = {
                    if (it.length <= 50) {
                        viewModel.onEmailChange(it)
                        emailError = null
                    }
                },
                label = { Text("Email") },
                placeholder = { Text("abc@email.com") },
                isError = (emailError != null),
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_email),
                        contentDescription = "Email Icon",
                        modifier = Modifier.size(24.dp)
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
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = uiState.password,
                onValueChange = {
                    if (it.length <= 30) {
                        viewModel.onPasswordChange(it)
                        passwordError = null
                    }
                },
                label = { Text("Password") },
                placeholder = { Text("Your password") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
                visualTransformation =
                if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            painter = painterResource(
                                id = if (passwordVisible) R.drawable.ic_eye_closed
                                else R.drawable.ic_eye_open
                            ),
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                isError = (passwordError != null),
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_lock),
                        contentDescription = "Lock Icon",
                        modifier = Modifier.size(24.dp)
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
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(
                    checked = uiState.rememberMe,
                    onCheckedChange = { viewModel.onRememberMeChange(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = primaryBlue,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color.Gray
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Remember Me")
            }
            if (uiState.isLoading) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
            }
            uiState.errorMessage?.let { error ->
                Text(
                    text = "Error: $error",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            Button(
                onClick = {
                    var hasError = false
                    if (uiState.email.isBlank()) {
                        emailError = "Please enter your email."
                        hasError = true
                    } else if (!uiState.email.contains("@") || !uiState.email.endsWith(".com")) {
                        emailError = "Please enter a valid email address."
                        hasError = true
                    }
                    if (uiState.password.isBlank()) {
                        passwordError = "Please enter your password."
                        hasError = true
                    }
                    if (!hasError) {
                        viewModel.loginUser {
                            onLoginSuccess()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = primaryBlue)
            ) {
                Text("SIGN IN", color = Color.White)
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Don’t have an account?")
                TextButton(onClick = { onNavigateToRegister() }) {
                    Text("Sign Up", color = primaryBlue)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    val fakeApp = FakeApplication()
    val previewViewModel = AuthViewModel(fakeApp).apply {
        onEmailChange("login@preview.com")
        onPasswordChange("123456")
    }
    GrowhubTheme {
        LoginScreen(
            viewModel = previewViewModel,
            onLoginSuccess = {},
            onNavigateToRegister = {}
        )
    }
}
