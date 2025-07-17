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
fun Step3Screen(navController: NavController, viewModel: RkwFormViewModel) {
    val formData by viewModel.uiState.collectAsState()
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
            ProgressStepper(currentStep = 3, stepLabels = stepLabels)
            Spacer(modifier = Modifier.height(8.dp))

            Text("Bankverbindung & Steuern", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                value = formData.bankDetails.institute,
                onValueChange = { viewModel.updateBankInstitute(it) },
                label = { Text("Kreditinstitut") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = formData.bankDetails.iban,
                onValueChange = { viewModel.updateIban(it) },
                label = { Text("IBAN") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = formData.bankDetails.taxId,
                onValueChange = { viewModel.updateTaxId(it) },
                label = { Text("USt-ID oder Steuer-Nr.") },
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text("KMU-Einstufung (letzte 2 Jahresabschlüsse)", style = MaterialTheme.typography.titleLarge)

            Text("Vorletzter Jahresabschluss", style = MaterialTheme.typography.titleMedium)
            val penultimate = formData.smeClassification.penultimateYear
            OutlinedTextField(
                value = if(penultimate.employees == 0) "" else penultimate.employees.toString(),
                onValueChange = { viewModel.updateFinancialYear(false, penultimate.copy(employees = it.toIntOrNull() ?: 0)) },
                label = { Text("Anzahl Vollzeitbeschäftigte") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = if(penultimate.turnover == 0.0) "" else penultimate.turnover.toString(),
                onValueChange = { viewModel.updateFinancialYear(false, penultimate.copy(turnover = it.toDoubleOrNull() ?: 0.0)) },
                label = { Text("Jahresumsatz in €") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = if(penultimate.balanceSheetTotal == 0.0) "" else penultimate.balanceSheetTotal.toString(),
                onValueChange = { viewModel.updateFinancialYear(false, penultimate.copy(balanceSheetTotal = it.toDoubleOrNull() ?: 0.0)) },
                label = { Text("Jahresbilanzsumme in €") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Letzter Jahresabschluss", style = MaterialTheme.typography.titleMedium)
            val last = formData.smeClassification.lastYear
            OutlinedTextField(
                value = if(last.employees == 0) "" else last.employees.toString(),
                onValueChange = { viewModel.updateFinancialYear(true, last.copy(employees = it.toIntOrNull() ?: 0)) },
                label = { Text("Anzahl Vollzeitbeschäftigte") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = if(last.turnover == 0.0) "" else last.turnover.toString(),
                onValueChange = { viewModel.updateFinancialYear(true, last.copy(turnover = it.toDoubleOrNull() ?: 0.0)) },
                label = { Text("Jahresumsatz in €") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = if(last.balanceSheetTotal == 0.0) "" else last.balanceSheetTotal.toString(),
                onValueChange = { viewModel.updateFinancialYear(true, last.copy(balanceSheetTotal = it.toDoubleOrNull() ?: 0.0)) },
                label = { Text("Jahresbilanzsumme in €") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = { navController.popBackStack() }) { Text("Zurück") }
                // ÄNDERUNG: Navigiert jetzt zu "step4"
                Button(onClick = { navController.navigate("step4") }) { Text("Weiter") }
            }
        }
    }
}