package com.example.rkwthringenapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.rkwthringenapp.R

@Composable
fun WelcomeScreen(navController: NavController, viewModel: RkwFormViewModel) {
    // HINWEIS: Die Logik für "hasSavedData" wurde entfernt, da dieser Screen
    // im neuen Login-Flow nicht mehr aktiv genutzt wird. Er wird nur beibehalten,
    // um den Build-Prozess nicht zu stören.

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.rkw_thueringen_logo_grau),
                contentDescription = "RKW Thüringen Logo",
                modifier = Modifier.fillMaxWidth(0.7f)
            )
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Willkommen beim digitalen Erfassungsbogen des RKW Thüringen.",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Starten Sie eine neue Erfassung.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = {
                    viewModel.startNewForm()
                    navController.navigate("step1")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Neuen Erfassungsbogen anlegen")
            }
        }
    }
}