package com.isis3510.growhub.view.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.isis3510.growhub.view.auth.InterestsScreen
import com.isis3510.growhub.view.auth.LoginScreen
import com.isis3510.growhub.view.auth.RegisterScreen
import com.isis3510.growhub.view.create.CreateEventView
import com.isis3510.growhub.view.dummy.PlaceholderScreen
import com.isis3510.growhub.view.events.MyEventsView
import com.isis3510.growhub.view.home.MainView
import com.isis3510.growhub.view.map.MapView
import com.isis3510.growhub.view.profile.ProfileView
import com.isis3510.growhub.view.detail.EventDetailView
import com.isis3510.growhub.viewmodel.AuthViewModel

object Destinations {
    const val LOGIN = "login"
    const val HOME = "home"
    const val REGISTER = "register"
    const val MAP = "map"
    const val MY_EVENTS = "my_events"
    const val PROFILE = "profile"
    const val EDIT_PROFILE = "edit_profile"
    const val CREATE = "create"
    const val INTERESTS = "interestsScreen"
    const val EVENT_DETAIL = "event_detail"
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavGraph(
    manifestApiKey: String?,
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    // Creamos UNA SOLA instancia de AuthViewModel aquí:
    val authViewModel: AuthViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // LOGIN
        composable(Destinations.LOGIN) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    val currentUid = FirebaseAuth.getInstance().currentUser?.uid
                    if (currentUid == null) {
                        // Si por algún motivo no existe UID, forzamos a Login
                        navController.navigate(Destinations.LOGIN) {
                            popUpTo(Destinations.LOGIN) { inclusive = true }
                        }
                    } else {
                        // Revisamos si el usuario en Firestore tiene la info de 'skills'
                        FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(currentUid)
                            .get()
                            .addOnSuccessListener { doc ->
                                val hasSkills = doc.exists() && doc.contains("skills")
                                if (!hasSkills) {
                                    // Navegamos a Interests
                                    navController.navigate(Destinations.INTERESTS) {
                                        popUpTo(Destinations.LOGIN) { inclusive = true }
                                    }
                                } else {
                                    // Navegamos a Home
                                    navController.navigate(Destinations.HOME) {
                                        popUpTo(Destinations.LOGIN) { inclusive = true }
                                    }
                                }
                            }
                            .addOnFailureListener {
                                // Si falla la consulta, lo enviamos a Home
                                navController.navigate(Destinations.HOME) {
                                    popUpTo(Destinations.LOGIN) { inclusive = true }
                                }
                            }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Destinations.REGISTER)
                }
            )
        }

        // REGISTER
        composable(Destinations.REGISTER) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToInterests = {
                    // Al terminar, pasamos a InterestsScreen
                    navController.navigate(Destinations.INTERESTS) {
                        popUpTo(Destinations.REGISTER) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // INTERESTS (sin argumento)
        composable(Destinations.INTERESTS) {
            InterestsScreen(
                viewModel = authViewModel,
                onContinueSuccess = {
                    // Al finalizar el registro, pasamos a Home
                    navController.navigate(Destinations.HOME) {
                        popUpTo(Destinations.INTERESTS) { inclusive = true }
                    }
                },
                onGoBack = {
                    navController.popBackStack()
                }
            )
        }

        // HOME
        composable(Destinations.HOME) {
            MainView(
                navController = navController,
                onLogout = {
                    navController.navigate(Destinations.LOGIN) {
                        popUpTo(Destinations.HOME) { inclusive = true }
                    }
                }
            )
        }

        // MAP
        composable(Destinations.MAP) {
            MapView(
                manifestApiKey = manifestApiKey,
                navController = navController,
                onNavigateBack = {
                    navController.navigate(Destinations.HOME) {
                        popUpTo(Destinations.MAP) { inclusive = true }
                    }
                }
            )
        }

        // MY_EVENTS
        composable(Destinations.MY_EVENTS) {
            MyEventsView(
                navController = navController,
                onNavigateBack = {
                    navController.navigate(Destinations.HOME) {
                        popUpTo(Destinations.MY_EVENTS) { inclusive = true }
                    }
                }
            )
        }

        // PROFILE
        composable(Destinations.PROFILE) {
            ProfileView(
                navController = navController,
                onNavigateBack = {
                    navController.navigate(Destinations.HOME) {
                        popUpTo(Destinations.PROFILE) { inclusive = true }
                    }
                },
                onNavigateToEditProfile = {
                    navController.navigate(Destinations.EDIT_PROFILE) {
                        popUpTo(Destinations.EDIT_PROFILE) { inclusive = true }
                    }
                }
            )
        }

        // EDIT_PROFILE
        composable(Destinations.EDIT_PROFILE) {
            PlaceholderScreen("EDIT_PROFILE")
        }

        // CREATE EVENT
        composable(Destinations.CREATE) {
            CreateEventView(
                onNavigateBack = {
                    navController.navigate(Destinations.HOME) {
                        popUpTo(Destinations.CREATE) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = "${Destinations.EVENT_DETAIL}/{eventName}",
            arguments = listOf(
                navArgument("eventName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val eventName = backStackEntry.arguments?.getString("eventName") ?: ""
            EventDetailView(
                eventName = eventName,
                navController = navController
            )
        }
    }
}
