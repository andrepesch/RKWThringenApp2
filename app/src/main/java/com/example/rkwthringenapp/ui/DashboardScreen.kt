package com.example.rkwthringenapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@Composable
fun DashboardScreen(navController: NavController, authViewModel: AuthViewModel) {
    val dashboardViewModel: DashboardViewModel = viewModel()
    val authState by authViewModel.uiState.collectAsState()
    val dashboardState by dashboardViewModel.uiState.collectAsState()

    // Lade die Formulare, sobald die Berater-ID verfügbar ist.
    LaunchedEffect(authState.beraterId) {
        authState.beraterId?.let {
            dashboardViewModel.loadForms(it)
        }
    }

    Scaffold(
        topBar = {
            RkwAppBar(
                title = "Meine Erfassungsbögen",
                actions = {
                    TextButton(onClick = { authViewModel.logout() }) {
                        Text("Logout")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("step1") }) {
                Icon(Icons.Default.Add, contentDescription = "Neuen Bogen anlegen")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (dashboardState.isLoading) {
                CircularProgressIndicator()
            } else if (dashboardState.error != null) {
                Text(
                    text = "Fehler: ${dashboardState.error}",
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            } else if (dashboardState.forms.isEmpty()) {
                Text(
                    text = "Sie haben noch keine Erfassungsbögen angelegt.\nKlicken Sie auf das '+' um zu beginnen.",
                    textAlign = TextAlign.Center
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(dashboardState.forms) { form ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Text(form.companyName, style = MaterialTheme.typography.titleMedium)
                                Text(form.legalForm, style = MaterialTheme.typography.bodyMedium)
                                Text("Erstellt am: ${form.created_at}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}