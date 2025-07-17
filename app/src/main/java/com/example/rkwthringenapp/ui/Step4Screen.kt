package com.example.rkwthringenapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step4Screen(navController: NavController, viewModel: RkwFormViewModel) {
    val formData by viewModel.uiState.collectAsState()
    val details = formData.consultationDetails
    val stepLabels = listOf("Unternehmensdaten", "Ansprechpartner", "Finanzdaten", "Beratung", "Berater", "Abschluss")

    Scaffold(
        topBar = { RkwAppBar() }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            ProgressStepper(currentStep = 4, stepLabels = stepLabels)
            Spacer(modifier = Modifier.height(8.dp))

            Text("Details zur Beratung", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = details.focus,
                onValueChange = { viewModel.updateConsultationFocus(it) },
                label = { Text("Schwerpunkt") },
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = if (details.scopeInDays == 0) "" else details.scopeInDays.toString(),
                    onValueChange = { viewModel.updateConsultationScope(it) },
                    label = { Text("Umfang (Tage)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = if (details.dailyRate == 0.0) "" else details.dailyRate.toString(),
                    onValueChange = { viewModel.updateConsultationRate(it) },
                    label = { Text("Tagessatz in €") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
            }
            OutlinedTextField(
                value = details.endDate,
                onValueChange = { viewModel.updateConsultationEndDate(it) },
                label = { Text("Zeitraum (bis)") },
                modifier = Modifier.fillMaxWidth()
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            OutlinedTextField(
                value = details.initialSituation,
                onValueChange = { viewModel.updateConsultationInitialSituation(it) },
                label = { Text("Ausgangssituation und Problembeschreibung") },
                modifier = Modifier.fillMaxWidth().height(120.dp)
            )
            OutlinedTextField(
                value = details.consultationContent,
                onValueChange = { viewModel.updateConsultationContent(it) },
                label = { Text("Beratungsinhalt: Vorgehen, Ziele, Mehrwert") },
                modifier = Modifier.fillMaxWidth().height(150.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = { navController.popBackStack() }) { Text("Zurück") }
                // ÄNDERUNG: Navigiert jetzt zu "step5"
                Button(onClick = { navController.navigate("step5") }) { Text("Weiter") }
            }
        }
    }
}