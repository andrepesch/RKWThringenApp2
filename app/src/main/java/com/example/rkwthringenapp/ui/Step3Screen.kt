package com.example.rkwthringenapp.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.rkwthringenapp.data.FinancialYear
import com.example.rkwthringenapp.ui.util.CurrencyVisualTransformation
import com.example.rkwthringenapp.ui.util.IbanVisualTransformation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step3Screen(navController: NavController, viewModel: RkwFormViewModel) {
    val formData by viewModel.uiState.collectAsState()
    val isIbanError by viewModel.isIbanError.collectAsState()
    val stepLabels = listOf("Unternehmensdaten", "Ansprechpartner", "Finanzdaten", "Beratung", "Berater", "Abschluss")

    var penultimateYearVisible by remember { mutableStateOf(false) }
    var lastYearVisible by remember { mutableStateOf(false) }

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

            Text("Bankverbindung, Steuernr. und KMU-Bewertung", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(value = formData.bankDetails.institute, onValueChange = { viewModel.updateBankInstitute(it) }, label = { Text("Kreditinstitut") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                value = formData.bankDetails.iban,
                onValueChange = { viewModel.updateIban(it) },
                label = { Text("IBAN") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = IbanVisualTransformation(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, autoCorrect = false, keyboardType = KeyboardType.Ascii),
                isError = isIbanError,
                supportingText = { if (isIbanError) Text("IBAN ungültig") }
            )
            OutlinedTextField(value = formData.bankDetails.taxId, onValueChange = { viewModel.updateTaxId(it) }, label = { Text("USt-ID oder Steuer-Nr.") }, modifier = Modifier.fillMaxWidth())

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            InfoBox(text = "Die Einstufung als kleines oder mittleres Unternehmen (KMU) ist Voraussetzung für den Zugang zu vielen Förderprogrammen. Die Angaben der letzten beiden abgeschlossenen Geschäftsjahre sind dafür entscheidend.")

            Button(onClick = { lastYearVisible = !lastYearVisible }, modifier = Modifier.fillMaxWidth()) {
                Text(if (lastYearVisible) "Letzten Jahresabschluss ausblenden" else "Letzten Jahresabschluss hinzufügen")
            }

            AnimatedVisibility(visible = lastYearVisible) {
                FinancialYearCard(
                    title = "Letzter Jahresabschluss",
                    financialYear = formData.smeClassification.lastYear,
                    availableYears = viewModel.availableYears,
                    onUpdate = { updatedYear -> viewModel.updateFinancialYear(true, updatedYear) }
                )
            }

            Button(onClick = { penultimateYearVisible = !penultimateYearVisible }, modifier = Modifier.fillMaxWidth()) {
                Text(if (penultimateYearVisible) "Vorletzten Jahresabschluss ausblenden" else "Vorletzten Jahresabschluss hinzufügen")
            }

            AnimatedVisibility(visible = penultimateYearVisible) {
                FinancialYearCard(
                    title = "Vorletzter Jahresabschluss",
                    financialYear = formData.smeClassification.penultimateYear,
                    availableYears = viewModel.availableYears,
                    onUpdate = { updatedYear -> viewModel.updateFinancialYear(false, updatedYear) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = { navController.popBackStack() }) { Text("Zurück") }
                Button(onClick = { navController.navigate("step4") }) { Text("Weiter") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialYearCard(
    title: String,
    financialYear: FinancialYear,
    availableYears: List<String>,
    onUpdate: (FinancialYear) -> Unit
) {
    var yearDropdownExpanded by remember { mutableStateOf(false) }
    var showEmployeeInfoDialog by remember { mutableStateOf(false) }

    if (showEmployeeInfoDialog) {
        AlertDialog(
            onDismissRequest = { showEmployeeInfoDialog = false },
            title = { Text("Berechnung der Mitarbeiterzahl (JAE)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Die Mitarbeiterzahl wird in Jahresarbeitseinheiten (JAE) ausgedrückt.", fontWeight = FontWeight.Bold)
                    Text("Einbezogen werden:\n- Lohn- und Gehaltsempfänger\n- für das Unternehmen tätige Personen, die in einem Unterordnungsverhältnis zu diesem stehen und nach nationalem Recht Arbeitnehmern gleichgestellt sind\n- mitarbeitende Eigentümer\n- Teilhaber, die eine regelmäßige Tätigkeit in dem Unternehmen ausüben und finanzielle Vorteile aus dem Unternehmen ziehen.")
                    Text("Nicht einbezogen werden:\n- Auszubildende oder Studenten in der beruflichen Ausbildung\n- Arbeitnehmer im Mutterschafts- oder Elternurlaub")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text("Beispiel:", fontWeight = FontWeight.Bold)
                    Text("1 Vollzeitkraft = 1,0 JAE\n3 Teilzeitkräfte (50%) = 1,5 JAE\n1 Saisonkraft (3 Mon.) = 0,25 JAE\n= Insgesamt 2,75 JAE")
                }
            },
            confirmButton = {
                Button(onClick = { showEmployeeInfoDialog = false }) {
                    Text("Schließen")
                }
            }
        )
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)

            ExposedDropdownMenuBox(
                expanded = yearDropdownExpanded,
                onExpandedChange = { yearDropdownExpanded = !yearDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = financialYear.year,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Abschlussjahr") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = yearDropdownExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = yearDropdownExpanded,
                    onDismissRequest = { yearDropdownExpanded = false }
                ) {
                    availableYears.forEach { year ->
                        DropdownMenuItem(
                            text = { Text(year) },
                            onClick = {
                                onUpdate(financialYear.copy(year = year))
                                yearDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = if(financialYear.employees == 0) "" else financialYear.employees.toString(),
                onValueChange = { onUpdate(financialYear.copy(employees = it.toIntOrNull() ?: 0)) },
                label = { Text("Anzahl Mitarbeiter (JAE)") },
                trailingIcon = {
                    IconButton(onClick = { showEmployeeInfoDialog = true }) {
                        Icon(Icons.Outlined.Info, contentDescription = "Information zur Berechnung")
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // **KORRIGIERTES FELD FÜR JAHRESUMSATZ**
            OutlinedTextField(
                value = financialYear.turnover,
                onValueChange = { newValue ->
                    // Diese Zeile ist der Schlüssel: Sie entfernt alle Nicht-Ziffern,
                    // bevor die Daten im ViewModel gespeichert werden.
                    val digitsOnly = newValue.filter { it.isDigit() }
                    onUpdate(financialYear.copy(turnover = digitsOnly))
                },
                label = { Text("Jahresumsatz in €") },
                visualTransformation = CurrencyVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // **KORRIGIERTES FELD FÜR JAHRESBILANZSUMME**
            OutlinedTextField(
                value = financialYear.balanceSheetTotal,
                onValueChange = { newValue ->
                    // Auch hier werden nur die reinen Ziffern gespeichert.
                    val digitsOnly = newValue.filter { it.isDigit() }
                    onUpdate(financialYear.copy(balanceSheetTotal = digitsOnly))
                },
                label = { Text("Jahresbilanzsumme in €") },
                visualTransformation = CurrencyVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
    }
}