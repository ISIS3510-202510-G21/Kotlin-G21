package com.isis3510.growhub

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp
import com.isis3510.growhub.view.navigation.AppNavGraph
import com.isis3510.growhub.view.navigation.Destinations
import com.isis3510.growhub.viewmodel.AuthViewModel
import com.isis3510.growhub.view.theme.GrowhubTheme

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()

        setContent {
            GrowhubTheme {
                // Decidimos cuál es la pantalla inicial:
                val startDestination = if (authViewModel.isUserLoggedIn()) {
                    Destinations.HOME
                } else {
                    Destinations.LOGIN
                }

                AppScaffold(startDestination, authViewModel)
            }
        }
    }
}

@Composable
fun AppScaffold(startDestination: String, authViewModel: AuthViewModel) {
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        AppNavGraph(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        )
    }
}

// Vista previa de la pantalla principal (AppScaffold)
@Preview(showBackground = true)
@Composable
fun AppScaffoldPreview() {
    // FakeApplication para no depender de un Application real en la preview
    val fakeApp = FakeApplication()
    val fakeAuthViewModel = AuthViewModel(fakeApp)
    val fakeStartDestination = Destinations.LOGIN

    GrowhubTheme {
        AppScaffold(
            startDestination = fakeStartDestination,
            authViewModel = fakeAuthViewModel
        )
    }
}

/**
 * FakeApplication que se utilizará solo en funciones de preview
 * para evitar problemas al instanciar AuthViewModel en un preview.
 */
class FakeApplication : Application()
