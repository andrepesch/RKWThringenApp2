package com.example.rkwthringenapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.rkwthringenapp.data.FormSummary
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun DashboardScreen(navController: NavController, authViewModel: AuthViewModel) {
    val dashboardViewModel: DashboardViewModel = viewModel()
    val rkwFormViewModel: RkwFormViewModel = viewModel()
    val authState by authViewModel.uiState.collectAsState()
    val dashboardState by dashboardViewModel.uiState.collectAsState()

    LaunchedEffect(authState.beraterId) {
        authState.beraterId?.let {
            dashboardViewModel.loadForms(it)
        }
    }

    Scaffold(
        topBar = {
            RkwAppBar(
                title = "Meine Erfassungsbögen",
                actions = { TextButton(onClick = { authViewModel.logout() }) { Text("Logout") } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                rkwFormViewModel.startNewForm() // Wichtig: Formular zurücksetzen
                navController.navigate("step1")
            }) {
                Icon(Icons.Default.Add, contentDescription = "Neuen Bogen anlegen")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (dashboardState.isLoading) {
                CircularProgressIndicator()
            } else if (dashboardState.error != null) {
                Text("Fehler: ${dashboardState.error}", color = MaterialTheme.colorScheme.error)
            } else if (dashboardState.forms.isEmpty()) {
                Text("Keine Bögen gefunden.", textAlign = TextAlign.Center)
            } else {
                val groupedForms = dashboardState.forms.groupBy { it.status }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    groupedForms["entwurf"]?.let { drafts ->
                        item {
                            Text("ENTWÜRFE", style = MaterialTheme.typography.titleMedium)
                        }
                        items(drafts) { form ->
                            FormCard(form = form, onClick = {
                                rkwFormViewModel.loadDraft(form.id)
                                navController.navigate("step1")
                            })
                        }
                    }
                    groupedForms["gesendet"]?.let { sentForms ->
                        item {
                            Text("GESENDET", style = MaterialTheme.typography.titleMedium)
                        }
                        items(sentForms) { form ->
                            FormCard(form = form, onClick = {
                                navController.navigate("sentFormDetail/${form.id}")
                            })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FormCard(form: FormSummary, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(form.companyName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Box(
                    modifier = Modifier
                        .background(
                            color = if (form.status == "entwurf") Color.Blue.copy(alpha = 0.1f) else Color.Green.copy(alpha = 0.1f),
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = form.status.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (form.status == "entwurf") Color.Blue else Color.Green
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Tagwerke: ${form.scopeInDays}", style = MaterialTheme.typography.bodyMedium)
            Text("Honorar: ${form.honorar} €", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (form.status == "entwurf") "Zuletzt bearbeitet: ${formatDate(form.updated_at)}" else "Gesendet am: ${formatDate(form.updated_at)}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

fun formatDate(dateString: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMANY)
        val formatter = SimpleDateFormat("dd.MM.yyyy, HH:mm 'Uhr'", Locale.GERMANY)
        formatter.format(parser.parse(dateString)!!)
    } catch (e: Exception) {
        dateString // Fallback
    }
}