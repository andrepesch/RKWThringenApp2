package com.example.rkwthringenapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.rkwthringenapp.R
import com.example.rkwthringenapp.ui.util.DateVisualTransformation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RkwAppBar() {
    TopAppBar(
        title = { Text("Erfassungsbogen", fontWeight = FontWeight.Bold) },
        actions = {
            Image(
                painter = painterResource(id = R.drawable.rkw_thueringen_wuerfel_grau),
                contentDescription = "RKW Logo",
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(32.dp)
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step1Screen(navController: NavController, viewModel: RkwFormViewModel) {
    val formData by viewModel.uiState.collectAsState()
    val wzSearchText by viewModel.wzSearchText.collectAsState()
    val filteredWzList by viewModel.filteredWzList.collectAsState()
    val isPlzError by viewModel.isPlzError.collectAsState()

    val legalForms = listOf(
        "Einzelunternehmen", "GbR", "e.K.", "GmbH", "GmbH & Co. KG",
        "UG (haftungsbeschränkt)", "Freie Berufe", "Kommanditgesellschaft (KG)",
        "Offene Handelsgesellschaft (OHG)", "Aktiengesellschaft (AG)",
        "Limited (Ltd.)", "Ltd. & Co. KG", "e. V.", "Eingetragene Genossenschaft (eG)",
        "KG auf Aktien (KGaA)", "Partnerschaftsgesellschaft", "Societas Europaea (SE)", "Stiftung"
    )

    val stepLabels = listOf("Unternehmensdaten", "Ansprechpartner", "Finanzdaten", "Beratung", "Berater", "Abschluss")

    Scaffold(topBar = { RkwAppBar() }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            ProgressStepper(currentStep = 1, stepLabels = stepLabels)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(value = formData.companyName, onValueChange = { viewModel.updateCompanyName(it) }, label = { Text("Unternehmensname") }, modifier = Modifier.fillMaxWidth())

            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = formData.legalForm.ifEmpty { "Bitte auswählen" },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Rechtsform") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    legalForms.forEach { form ->
                        DropdownMenuItem(
                            text = { Text(form) },
                            onClick = {
                                viewModel.updateLegalForm(form)
                                expanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(value = formData.foundationDate, onValueChange = { viewModel.updateFoundationDate(it) }, label = { Text("Gründungsdatum (TTMMJJJJ)") }, visualTransformation = DateVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = formData.streetAndNumber, onValueChange = { viewModel.updateStreetAndNumber(it) }, label = { Text("Straße und Hausnummer") }, modifier = Modifier.fillMaxWidth())

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = formData.postalCode,
                    onValueChange = { viewModel.updatePostalCode(it) },
                    label = { Text("PLZ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    isError = isPlzError,
                    supportingText = { if (isPlzError) Text("PLZ ungültig", color = MaterialTheme.colorScheme.error) }
                )
                OutlinedTextField(
                    value = formData.city,
                    onValueChange = { viewModel.updateCity(it) },
                    label = { Text("Ort") },
                    modifier = Modifier.weight(2f)
                )
            }

            var wzExpanded by remember { mutableStateOf(false) }
            var textFieldValue by remember(wzSearchText) {
                mutableStateOf(TextFieldValue(wzSearchText, TextRange(wzSearchText.length)))
            }
            var hasFocus by remember { mutableStateOf(false) }

            ExposedDropdownMenuBox(
                expanded = wzExpanded,
                onExpandedChange = { wzExpanded = !wzExpanded }
            ) {
                OutlinedTextField(
                    value = textFieldValue,
                    onValueChange = {
                        textFieldValue = it
                        viewModel.onWzSearchChanged(it.text)
                    },
                    label = { Text("Wirtschaftszweig (suchen)") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = wzExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused && !hasFocus) {
                                textFieldValue = textFieldValue.copy(selection = TextRange(0, textFieldValue.text.length))
                                wzExpanded = true
                            }
                            hasFocus = focusState.isFocused
                        }
                )
                if (filteredWzList.isNotEmpty()) {
                    ExposedDropdownMenu(expanded = wzExpanded, onDismissRequest = { wzExpanded = false }) {
                        filteredWzList.forEach { wz ->
                            DropdownMenuItem(
                                text = { Text(wz) },
                                onClick = {
                                    viewModel.onWzSelected(wz)
                                    wzExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Text("Vorsteuerabzugsberechtigt?", style = MaterialTheme.typography.bodyLarge)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    Modifier
                        .selectable(selected = formData.isVatDeductible, onClick = { viewModel.updateIsVatDeductible(true) })
                        .padding(end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = formData.isVatDeductible, onClick = { viewModel.updateIsVatDeductible(true) })
                    Text("Ja", modifier = Modifier.padding(start = 4.dp))
                }
                Row(
                    Modifier.selectable(selected = !formData.isVatDeductible, onClick = { viewModel.updateIsVatDeductible(false) }),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = !formData.isVatDeductible, onClick = { viewModel.updateIsVatDeductible(false) })
                    Text("Nein", modifier = Modifier.padding(start = 4.dp))
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = formData.hasWebsite, onCheckedChange = { viewModel.updateHasWebsite(it) })
                Text("Website vorhanden")
            }

            if (formData.hasWebsite) {
                OutlinedTextField(value = formData.websiteUrl, onValueChange = { viewModel.updateWebsiteUrl(it) }, label = { Text("Website/URL") }, modifier = Modifier.fillMaxWidth())
            }

            Button(onClick = { navController.navigate("step2") }, modifier = Modifier.align(Alignment.End).padding(bottom = 16.dp)) {
                Text("Weiter")
            }
        }
    }
}