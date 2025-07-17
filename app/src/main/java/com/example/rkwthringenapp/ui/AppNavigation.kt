package com.example.rkwthringenapp.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation(
    viewModel: RkwFormViewModel,
    onSendClick: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "welcome") {
        composable("welcome") { WelcomeScreen(navController = navController, viewModel = viewModel) }
        composable("step1") { Step1Screen(navController = navController, viewModel = viewModel) }
        composable("step2") { Step2Screen(navController = navController, viewModel = viewModel) }
        composable("step3") { Step3Screen(navController = navController, viewModel = viewModel) }
        composable("step4") { Step4Screen(navController = navController, viewModel = viewModel) }
        // NEU: Route für Schritt 5
        composable("step5") { Step5Screen(navController = navController, viewModel = viewModel) }
        composable("step6") { Step6Screen(navController = navController, viewModel = viewModel, onSendClick = onSendClick) }
    }
}