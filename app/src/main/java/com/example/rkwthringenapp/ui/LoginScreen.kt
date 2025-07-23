package com.example.rkwthringenapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.rkwthringenapp.R

@Composable
fun LoginScreen(navController: NavController, authViewModel: AuthViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val uiState by authViewModel.uiState.collectAsState()

    // Zeige einen Fehler-Dialog an, wenn ein Fehler auftritt
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

    // Wir verwenden eine Surface anstelle des Scaffolds, um die TopAppBar zu entfernen
    Surface(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp), // Mehr seitlicher Abstand
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Hinzugefügtes RKW Logo
                Image(
                    painter = painterResource(id = R.drawable.rkw_thueringen_logo_grau),
                    contentDescription = "RKW Thüringen Logo",
                    modifier = Modifier.fillMaxWidth(0.7f) // Logo füllt 70% der Breite
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Angepasster Begrüßungstext
                Text(
                    text = "Willkommen im Berater-Portal",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Bitte melden Sie sich an, um fortzufahren.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

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

            // Zeige einen Lade-Indikator an
            if (uiState.isLoading) {
                CircularProgressIndicator()
            }
        }
    }
}