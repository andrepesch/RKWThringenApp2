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
fun RegisterScreen(navController: NavController, authViewModel: AuthViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val uiState by authViewModel.uiState.collectAsState()

    uiState.error?.let { error ->
        AlertDialog(
            onDismissRequest = { authViewModel.dismissError() },
            title = { Text("Fehler") },
            text = { Text(error) },
            confirmButton = {
                Button(onClick = { authViewModel.dismissError() }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = { RkwAppBar(title = "Berater-Registrierung") }
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
                Text("Neues Konto erstellen", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(32.dp))

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
                    label = { Text("Passwort (mind. 8 Zeichen)") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Passwort bestätigen") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading,
                    isError = password != confirmPassword && confirmPassword.isNotEmpty()
                )
                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        // Die Validierung findet jetzt primär im ViewModel statt
                        authViewModel.register(email, password)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    // NEU: Verbesserte Logik für den Button-Status
                    enabled = !uiState.isLoading && email.isNotBlank() && password.length >= 8 && password == confirmPassword
                ) {
                    Text("Registrieren")
                }
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(
                    onClick = { navController.navigateUp() },
                    enabled = !uiState.isLoading
                ) {
                    Text("Bereits registriert? Zum Login")
                }
            }
            if (uiState.isLoading) {
                CircularProgressIndicator()
            }
        }
    }
}