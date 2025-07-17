package com.example.rkwthringenapp.ui

import androidx.compose.animation.AnimatedVisibility
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
fun Step5Screen(navController: NavController, viewModel: RkwFormViewModel) {
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
            ProgressStepper(currentStep = 5, stepLabels = stepLabels)
            Spacer(modifier = Modifier.height(8.dp))

            Text("Beraterauswahl", style = MaterialTheme.typography.titleLarge)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = formData.hasChosenConsultant,
                    onCheckedChange = { viewModel.updateHasChosenConsultant(it) }
                )
                Text("Ich habe mich bereits für einen Berater entschieden.")
            }

            AnimatedVisibility(visible = formData.hasChosenConsultant) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = formData.consultingFirm,
                        onValueChange = { viewModel.updateConsultingFirm(it) },
                        label = { Text("Beratungsfirma") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    formData.consultants.forEachIndexed { index, consultant ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("Berater ${index + 1}", style = MaterialTheme.typography.titleMedium)
                                    IconButton(onClick = { viewModel.removeConsultant(index) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Berater entfernen")
                                    }
                                }
                                OutlinedTextField(value = consultant.firstName, onValueChange = { viewModel.updateConsultant(index, consultant.copy(firstName = it)) }, label = { Text("Vorname") }, modifier = Modifier.fillMaxWidth())
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(value = consultant.lastName, onValueChange = { viewModel.updateConsultant(index, consultant.copy(lastName = it)) }, label = { Text("Nachname") }, modifier = Modifier.fillMaxWidth())
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(value = consultant.accreditationId, onValueChange = { viewModel.updateConsultant(index, consultant.copy(accreditationId = it)) }, label = { Text("Akkreditierungs-Nr.") }, modifier = Modifier.fillMaxWidth())
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(value = consultant.email, onValueChange = { viewModel.updateConsultant(index, consultant.copy(email = it)) }, label = { Text("E-Mail") }, modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }

                    Button(
                        onClick = { viewModel.addConsultant() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("+ Weiteren Berater hinzufügen")
                    }
                }
            }

            // Navigations-Buttons
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp, top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = { navController.popBackStack() }) { Text("Zurück") }
                Button(onClick = { navController.navigate("step6") }) { Text("Weiter") }
            }
        }
    }
}