package com.example.rkwthringenapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step2Screen(navController: NavController, viewModel: RkwFormViewModel) {
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
            ProgressStepper(currentStep = 2, stepLabels = stepLabels)
            Spacer(modifier = Modifier.height(8.dp))

            Text("Ansprechpartner für RKW", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                value = formData.mainContact.name,
                onValueChange = { viewModel.updateMainContactName(it) },
                label = { Text("Vor- und Nachname") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = formData.mainContact.email,
                onValueChange = { viewModel.updateMainContactEmail(it) },
                label = { Text("E-Mail") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = formData.mainContact.phone,
                onValueChange = { viewModel.updateMainContactPhone(it) },
                label = { Text("Telefon") },
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text("Wirtschaftlich berechtigte Personen", style = MaterialTheme.typography.titleLarge)

            formData.beneficialOwners.forEachIndexed { index, owner ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Person ${index + 1}", style = MaterialTheme.typography.titleMedium)
                            IconButton(onClick = { viewModel.removeBeneficialOwner(index) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Person entfernen")
                            }
                        }
                        OutlinedTextField(
                            value = owner.firstName,
                            onValueChange = { viewModel.updateBeneficialOwner(index, owner.copy(firstName = it)) },
                            label = { Text("Vorname") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = owner.lastName,
                            onValueChange = { viewModel.updateBeneficialOwner(index, owner.copy(lastName = it)) },
                            label = { Text("Nachname") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Button(
                onClick = { viewModel.addBeneficialOwner() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("+ Weitere Person hinzufügen")
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = { navController.popBackStack() }) { Text("Zurück") }
                // ÄNDERUNG: Navigiert jetzt zu "step3"
                Button(onClick = { navController.navigate("step3") }) { Text("Weiter") }
            }
        }
    }
}