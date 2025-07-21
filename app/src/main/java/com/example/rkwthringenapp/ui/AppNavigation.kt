package com.example.rkwthringenapp.ui

import androidx.compose.runtime.Composable
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
    // Hier erstellen wir Instanzen unserer ViewModels.
    // Compose sorgt dafür, dass die richtige Instanz für den Lebenszyklus verwendet wird.
    val authViewModel: AuthViewModel = viewModel()
    val rkwViewModel: RkwFormViewModel = viewModel()
    val navController = rememberNavController()

    // Lese den Login-Zustand aus dem AuthViewModel.
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    // Setze das Start-Ziel dynamisch basierend auf dem Login-Status.
    // Wenn der Nutzer eingeloggt ist, startet die App im Dashboard, sonst im Login.
    val startDestination = if (isLoggedIn) "dashboard" else "login"

    NavHost(navController = navController, startDestination = startDestination) {

        // Routen für Login, Registrierung und Dashboard
        composable("login") {
            LoginScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }
        composable("register") { // NEUE ROUTE
            RegisterScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }
        composable("dashboard") {
            DashboardScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        // Die bestehenden Routen für den Formular-Wizard.
        // Sie verwenden das rkwViewModel.
        composable("step1") {
            Step1Screen(navController = navController, viewModel = rkwViewModel)
        }
        composable("step2") {
            Step2Screen(navController = navController, viewModel = rkwViewModel)
        }
        composable("step3") {
            Step3Screen(navController = navController, viewModel = rkwViewModel)
        }
        composable("step4") {
            Step4Screen(navController = navController, viewModel = rkwViewModel)
        }
        composable("step5") {
            Step5Screen(navController = navController, viewModel = rkwViewModel)
        }
        composable("step6") {
            Step6Screen(
                navController = navController,
                viewModel = rkwViewModel,
                onSendClick = onSendClick
            )
        }
    }
}
