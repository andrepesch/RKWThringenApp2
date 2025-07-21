package com.example.rkwthringenapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun LoginScreen(navController: NavController, authViewModel: AuthViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val uiState by authViewModel.uiState.collectAsState()

    // Dialog für Fehler ODER Info-Nachrichten
    val message = uiState.error ?: uiState.info
    if (message != null) {
        AlertDialog(
            onDismissRequest = { authViewModel.dismissError() },
            title = { Text(if (uiState.error != null) "Fehler" else "Info") },
            text = { Text(message) },
            confirmButton = {
                Button(onClick = { authViewModel.dismissError() }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = { RkwAppBar(title = "Berater-Login") }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // NEUER TEST-BUTTON
                OutlinedButton(onClick = { authViewModel.testConnection() }) {
                    Text("1. Verbindungstest durchführen")
                }
                Spacer(modifier = Modifier.height(32.dp))

                Text("Willkommen zurück", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-Mail") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Passwort") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                )
                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { authViewModel.login(email, password) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading && email.isNotBlank() && password.isNotBlank()
                ) {
                    Text("Anmelden")
                }
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(
                    onClick = { navController.navigate("register") },
                    enabled = !uiState.isLoading
                ) {
                    Text("Noch kein Konto? Jetzt registrieren")
                }
            }

            if (uiState.isLoading) {
                CircularProgressIndicator()
            }
        }
    }
}