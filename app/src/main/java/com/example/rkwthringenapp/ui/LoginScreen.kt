package com.example.rkwthringenapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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

    // Fehler-Dialog (unverändert)
    uiState.error?.let { error ->
        AlertDialog(
            onDismissRequest = { authViewModel.dismissError() },
            title = { Text("Fehler") },
            text = { Text(error) },
            confirmButton = {
                Button(onClick = { authViewModel.dismissError() }) { Text("OK") }
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background // Nutzt die neue Hintergrundfarbe
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Das Logo, das auf dunklem Hintergrund gut funktioniert
                Image(
                    painter = painterResource(id = R.drawable.rkw_thueringen_logo_grau),
                    contentDescription = "RKW Thüringen Logo",
                    modifier = Modifier.fillMaxWidth(0.7f)
                )

                Spacer(modifier = Modifier.height(48.dp))

                Text(
                    text = "Willkommen im Berater-Portal",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Eingabefelder im neuen Stil
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-Mail") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Passwort") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(32.dp))

                // Haupt-Button im neuen Stil
                Button(
                    onClick = { authViewModel.login(email, password) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !uiState.isLoading && email.isNotBlank() && password.isNotBlank(),
                    shape = RoundedCornerShape(12.dp)
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
