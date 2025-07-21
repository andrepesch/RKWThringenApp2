package com.example.rkwthringenapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun DashboardScreen(navController: NavController, authViewModel: AuthViewModel) {
    Scaffold(
        topBar = {
            // Verwende die neue RkwAppBar und übergebe den Logout-Button als Aktion
            RkwAppBar(
                title = "Meine Erfassungsbögen",
                actions = {
                    TextButton(onClick = {
                        authViewModel.logout()
                        navController.navigate("login") {
                            popUpTo(0)
                        }
                    }) {
                        Text("Logout")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate("step1")
            }) {
                Icon(Icons.Default.Add, contentDescription = "Neuen Bogen anlegen")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text("Zuletzt bearbeitet", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(5) { index ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Mustermann GmbH - Kunde ${index + 1}", style = MaterialTheme.typography.bodyLarge)
                        Text("99084 Erfurt - Angelegt am 20.07.2025", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
