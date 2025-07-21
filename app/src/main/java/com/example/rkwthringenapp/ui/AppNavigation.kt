package com.example.rkwthringenapp.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect // <-- DIESER IMPORT HAT GEFEHLT
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation(
    onSendClick: () -> Unit
) {
    val authViewModel: AuthViewModel = viewModel()
    val rkwViewModel: RkwFormViewModel = viewModel()
    val navController = rememberNavController()

    // Lese den UI-Zustand aus dem AuthViewModel.
    val authState by authViewModel.uiState.collectAsState()

    // Setze das Start-Ziel dynamisch basierend auf dem Login-Status.
    val startDestination = if (authState.isLoggedIn) "dashboard" else "login"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(navController = navController, authViewModel = authViewModel)
        }
        composable("register") {
            RegisterScreen(navController = navController, authViewModel = authViewModel)
        }
        composable("dashboard") {
            DashboardScreen(navController = navController, authViewModel = authViewModel)
        }
        composable("step1") { Step1Screen(navController = navController, viewModel = rkwViewModel) }
        composable("step2") { Step2Screen(navController = navController, viewModel = rkwViewModel) }
        composable("step3") { Step3Screen(navController = navController, viewModel = rkwViewModel) }
        composable("step4") { Step4Screen(navController = navController, viewModel = rkwViewModel) }
        composable("step5") { Step5Screen(navController = navController, viewModel = rkwViewModel) }
        composable("step6") {
            Step6Screen(navController = navController, viewModel = rkwViewModel, onSendClick = onSendClick)
        }
    }

    // Navigiere zum Dashboard, wenn sich der Login-Status ändert
    LaunchedEffect(authState.isLoggedIn) {
        if (authState.isLoggedIn && navController.currentDestination?.route != "dashboard") {
            navController.navigate("dashboard") {
                // Lösche den gesamten Backstack bis zum Start, damit der Nutzer nicht
                // per "Zurück"-Taste auf den Login-Screen kommt.
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
        }
    }
}