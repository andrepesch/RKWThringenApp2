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
import androidx.compose.ui.unit.sp
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
                rkwFormViewModel.startNewForm()
                navController.navigate("step1")
            }) {
                Icon(Icons.Default.Add, contentDescription = "Neuen Bogen anlegen")
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
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
                            Text("ENTWÜRFE", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
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
                            Text("GESENDET", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp, top = 16.dp))
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
    // Farben basierend auf dem Status definieren
    val headerColor = if (form.status == "entwurf") Color(0xFF004A5A) else Color(0xFF5A6A62) // Dunkles Türkis für Entwurf, Grau-Grün für Gesendet
    val headerTextColor = Color.White
    val bodyColor = MaterialTheme.colorScheme.surface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = bodyColor)
    ) {
        Column {
            // Kopfzeile mit farblichem Hintergrund
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerColor)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = form.companyName,
                    style = MaterialTheme.typography.titleLarge,
                    color = headerTextColor,
                    fontWeight = FontWeight.Bold
                )
            }

            // Inhaltsbereich
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = form.address,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = form.mainContact,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${form.scopeInDays} Tagwerk zu ${form.dailyRate} Euro",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (form.status == "entwurf") "letzte Bearbeitung ${formatDate(form.updated_at)}" else "gesendet am ${formatDate(form.updated_at)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    fontSize = 10.sp
                )
            }
        }
    }
}

// Die formatDate-Funktion bleibt unverändert
fun formatDate(dateString: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMANY)
        val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY) // Nur Datum, ohne Uhrzeit
        formatter.format(parser.parse(dateString)!!)
    } catch (e: Exception) {
        dateString // Fallback
    }
}