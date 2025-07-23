package com.example.rkwthringenapp.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun AppNavigation() {
    val authViewModel: AuthViewModel = viewModel()
    val rkwViewModel: RkwFormViewModel = viewModel()
    val navController = rememberNavController()
    val authState by authViewModel.uiState.collectAsState()

    val startDestination = if (authState.isLoggedIn) "dashboard" else "login"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") { LoginScreen(navController, authViewModel) }
        composable("register") { RegisterScreen(navController, authViewModel) }
        composable("dashboard") { DashboardScreen(navController, authViewModel) }

        // Formular-Wizard
        composable("step1") { Step1Screen(navController, rkwViewModel) }
        composable("step2") { Step2Screen(navController, rkwViewModel) }
        composable("step3") { Step3Screen(navController, rkwViewModel) }
        composable("step4") { Step4Screen(navController, rkwViewModel) }
        composable("step5") { Step5Screen(navController, rkwViewModel) }
        composable("step6") { Step6Screen(navController, rkwViewModel, authViewModel) }

        // NEUE ROUTE für die Detailansicht eines gesendeten Formulars
        composable(
            route = "sentFormDetail/{formId}",
            arguments = listOf(navArgument("formId") { type = NavType.IntType })
        ) { backStackEntry ->
            val formId = backStackEntry.arguments?.getInt("formId") ?: 0
            SentFormDetailScreen(navController, formId)
        }
    }

    LaunchedEffect(authState.isLoggedIn) {
        if (authState.isLoggedIn && navController.currentDestination?.route != "dashboard") {
            navController.navigate("dashboard") {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                launchSingleTop = true
            }
        }
    }
}