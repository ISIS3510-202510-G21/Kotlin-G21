package com.isis3510.growhub.view.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.isis3510.growhub.view.auth.LoginScreen
import com.isis3510.growhub.view.home.MainView
import com.isis3510.growhub.view.dummy.PlaceholderScreen
import com.isis3510.growhub.view.events.MyEventsView
import com.isis3510.growhub.view.map.MapView
import com.isis3510.growhub.view.profile.ProfileView

//import com.isis3510.growhub.view.auth.RegisterScreen

object Destinations {
    const val LOGIN = "login"
    const val HOME = "home"
    const val REGISTER = "register"
    const val MAP = "map"
    const val MY_EVENTS = "my_events"
    const val PROFILE = "profile"
    const val EDIT_PROFILE = "edit_profile"
    const val CREATE = "create"
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Destinations.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    // Successful login will take us Home
                    navController.navigate(Destinations.HOME) {
                        popUpTo(Destinations.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Destinations.REGISTER)
                }
            )
        }

        //composable(Destinations.REGISTER)
        composable(Destinations.HOME) {
            MainView(
                navController = navController,
                onLogout = {
                    // Returns us to login
                    navController.navigate(Destinations.LOGIN) {
                        popUpTo(Destinations.HOME) { inclusive = true }
                    }
                }
            )
        }

        composable(Destinations.MAP) {
            MapView(
                navController = navController,
                onNavigateBack = {
                    navController.navigate(Destinations.MAP) {
                        popUpTo(Destinations.MAP) { inclusive = true }
                    }
                }
            )
        }

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

        composable(Destinations.EDIT_PROFILE) {
            PlaceholderScreen("EDIT_PROFILE")
        }

        composable(Destinations.CREATE) {
            PlaceholderScreen("CREATE")
        }
    }
}
