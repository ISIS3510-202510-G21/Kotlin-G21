package com.isis3510.growhub.view.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.isis3510.growhub.FakeApplication
import com.isis3510.growhub.viewmodel.AuthViewModel
import com.isis3510.growhub.view.theme.GrowhubTheme
import com.isis3510.growhub.R
import androidx.compose.ui.graphics.Color


@Composable
fun LoginScreen(
    viewModel: AuthViewModel = viewModel(),
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val primaryBlue = Color(0xFF5669FF) // Color usado en el Switch y ahora en el botón y texto

    // Contenedor principal.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Columna que organizará todo el contenido.
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Ícono o imagen circular en la parte superior
            Image(
                painter = painterResource(id = R.drawable.growhub_icon),
                contentDescription = "GrowHub Icon",
                modifier = Modifier
                    .size(150.dp)
                    .padding(bottom = 8.dp)
            )

            // Título principal (GrowHub)
            Text(
                text = "GrowHub",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier
                    .padding(bottom = 16.dp),
                textAlign = TextAlign.Center
            )

            // Subtítulo (Sign in)
            Text(
                text = "Sign in",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                textAlign = TextAlign.Left
            )

            // Campo de Email
            OutlinedTextField(
                value = uiState.email,
                onValueChange = { viewModel.onEmailChange(it) },
                label = { Text("Email") },
                placeholder = { Text("abc@email.com") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de Password
            OutlinedTextField(
                value = uiState.password,
                onValueChange = { viewModel.onPasswordChange(it) },
                label = { Text("Password") },
                placeholder = { Text("Your password") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Switch "Remember Me" con colores personalizados
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CustomSwitch(
                    checked = uiState.rememberMe,
                    onCheckedChange = { viewModel.onRememberMeChange(it) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Remember Me")
            }

            // Indicador de carga si está logueando
            if (uiState.isLoading) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Error (si lo hay)
            uiState.errorMessage?.let { error ->
                Text(
                    text = "Error: $error",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Botón de "Sign In" con color azul
            Button(
                onClick = {
                    viewModel.loginUser {
                        // Navega a Home si el login fue exitoso
                        onLoginSuccess()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = primaryBlue) // Aplicamos el color azul
            ) {
                Text("SIGN IN", color = Color.White)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Texto para navegar a "Sign Up" alineado correctamente
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Don’t have an account?")

                TextButton(onClick = { onNavigateToRegister() }) {
                    Text("Sign Up", color = primaryBlue) // Texto azul
                }
            }
        }
    }
}

// Switch personalizado con colores
@Composable
fun CustomSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White, // Color del "botón" cuando está activado
            checkedTrackColor = Color(0xFF5669FF), // Color del fondo cuando está activado
            uncheckedThumbColor = Color.White, // Color del "botón" cuando está desactivado
            uncheckedTrackColor = Color.Gray // Color del fondo cuando está desactivado
        )
    )
}

// Vista previa
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
