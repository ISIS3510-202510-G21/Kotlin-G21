package com.isis3510.growhub.view

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.isis3510.growhub.FakeApplication
import com.isis3510.growhub.viewmodel.AuthViewModel
import com.isis3510.growhub.view.theme.GrowhubTheme

@Composable
fun HomeScreen(
    viewModel: AuthViewModel = viewModel(),
    onLogout: () -> Unit
) {
    val uiState = viewModel.uiState.collectAsState().value

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Welcome!")
                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "You are logged in with: ${uiState.email}")

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        // Cierra sesión en Firebase y en local
                        viewModel.logoutUser()
                        onLogout()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sign Out")
                }
            }
        }
    }
}

// Vista previa de HomeScreen
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    // Usamos FakeApplication para crear el AuthViewModel en modo preview
    val fakeApp = FakeApplication()
    val previewViewModel = AuthViewModel(fakeApp)

    // Ajustamos manualmente el uiState para que se vea algo en la preview
    previewViewModel.onEmailChange("previewuser@example.com")

    GrowhubTheme {
        HomeScreen(
            viewModel = previewViewModel,
            onLogout = {}
        )
    }
}
