package com.example.rkwthringenapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
// NEUE IMPORTS FÜR DIE ICONS
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.rkwthringenapp.data.RkwFormData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SentFormDetailScreen(navController: NavController, formId: Int) {
    val viewModel: RkwFormViewModel = viewModel()
    val formData by viewModel.uiState.collectAsState()
    val isLoading by viewModel.isLoadingDetails.collectAsState()
    val error by viewModel.loadDetailsError.collectAsState()

    LaunchedEffect(formId) {
        if (formId != 0) {
            viewModel.loadDraft(formId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detailansicht") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                val currentError = error
                if (currentError != null) {
                    Text(
                        text = currentError,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        DetailSectionCard("Unternehmensdaten", Icons.Outlined.Business, formData)
                        DetailSectionCard("Ansprechpartner", Icons.Outlined.Person, formData)
                        DetailSectionCard("Bank & Steuern", Icons.Outlined.AccountBalance, formData)
                        DetailSectionCard("Beratungsdetails", Icons.Outlined.EditNote, formData)
                        DetailSectionCard("Berater", Icons.Outlined.SupportAgent, formData)
                    }
                }
            }
        }
    }
}

@Composable
fun DetailSectionCard(title: String, icon: ImageVector, formData: RkwFormData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleLarge)
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            when (title) {
                "Unternehmensdaten" -> {
                    DetailItem("Firma:", formData.companyName)
                    DetailItem("Rechtsform:", formData.legalForm)
                    DetailItem("Gründung:", formData.foundationDate)
                    DetailItem("Adresse:", "${formData.streetAndNumber}, ${formData.postalCode} ${formData.city}")
                    DetailItem("Branche:", formData.industrySector)
                    DetailItem("Website:", if (formData.hasWebsite) formData.websiteUrl else "Nicht vorhanden")
                }
                "Ansprechpartner" -> {
                    DetailItem("Name:", formData.mainContact.name)
                    DetailItem("E-Mail:", formData.mainContact.email)
                    DetailItem("Telefon:", formData.mainContact.phone)
                }
                "Bank & Steuern" -> {
                    DetailItem("Kreditinstitut:", formData.bankDetails.institute)
                    DetailItem("IBAN:", formData.bankDetails.iban)
                    DetailItem("Steuer-Nr./USt-ID:", formData.bankDetails.taxId)
                }
                "Beratungsdetails" -> {
                    val dailyRateInt = formData.consultationDetails.dailyRate.toIntOrNull() ?: 0
                    val scopeInDays = formData.consultationDetails.scopeInDays
                    val honorar = dailyRateInt * scopeInDays

                    DetailItem("Schwerpunkt:", formData.consultationDetails.focus)
                    DetailItem("Umfang:", "$scopeInDays Tage")
                    DetailItem("Tagessatz:", "$dailyRateInt €")
                    DetailItem("Honorar:", "$honorar €")
                    DetailItem("Zeitraum bis:", formData.consultationDetails.endDate)
                    DetailItemMultiline("Ausgangssituation:", formData.consultationDetails.initialSituation)
                    DetailItemMultiline("Beratungsinhalt:", formData.consultationDetails.consultationContent)
                }
                "Berater" -> {
                    DetailItem("Beratungsfirma:", if (formData.hasChosenConsultant) formData.consultingFirm else "Empfehlung durch RKW gewünscht")
                    if (formData.hasChosenConsultant) {
                        formData.consultants.forEachIndexed { index, consultant ->
                            Text("Berater ${index + 1}:", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                            DetailItem("  Name:", "${consultant.firstName} ${consultant.lastName}")
                            DetailItem("  E-Mail:", consultant.email)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(140.dp))
        Text(value)
    }
}

@Composable
fun DetailItemMultiline(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, fontWeight = FontWeight.SemiBold)
        Text(value, modifier = Modifier.padding(top = 4.dp))
    }
}