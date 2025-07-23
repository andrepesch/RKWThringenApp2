package com.example.rkwthringenapp.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
    val shareResult by dashboardViewModel.shareResult.collectAsState()
    val context = LocalContext.current

    // Reagiert auf das Ergebnis der "Teilen"-Aktion
    when (val result = shareResult) {
        is ShareResult.Loading -> {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("Einladung wird gesendet...") },
                text = { Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) { CircularProgressIndicator() } },
                confirmButton = {}
            )
        }
        is ShareResult.Success -> {
            LaunchedEffect(result) {
                Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                dashboardViewModel.clearShareResult()
            }
        }
        is ShareResult.Error -> {
            AlertDialog(
                onDismissRequest = { dashboardViewModel.clearShareResult() },
                title = { Text("Fehler beim Teilen") },
                text = { Text(result.message) },
                confirmButton = { Button(onClick = { dashboardViewModel.clearShareResult() }) { Text("OK") } }
            )
        }
        is ShareResult.Idle -> {}
    }

    LaunchedEffect(authState.beraterId) {
        authState.beraterId?.let { dashboardViewModel.loadForms(it) }
    }

    Scaffold(
        topBar = {
            RkwAppBar(title = "Meine Erfassungsbögen", actions = { TextButton(onClick = { authViewModel.logout() }) { Text("Logout") } })
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
                        item { Text("ENTWÜRFE", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp)) }
                        items(drafts) { form ->
                            FormCard(form = form, onClick = {
                                rkwFormViewModel.loadDraft(form.id)
                                navController.navigate("step1")
                            }, onShareClick = {
                                dashboardViewModel.shareForm(form.id)
                            })
                        }
                    }
                    groupedForms["gesendet"]?.let { sentForms ->
                        item { Text("GESENDET", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp, top = 16.dp)) }
                        items(sentForms) { form ->
                            FormCard(form = form, onClick = {
                                navController.navigate("sentFormDetail/${form.id}")
                            }, onShareClick = {})
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FormCard(form: FormSummary, onClick: () -> Unit, onShareClick: () -> Unit) {
    val headerColor = if (form.status == "entwurf") Color(0xFF004A5A) else Color(0xFF5A6A62)
    val headerTextColor = Color.White
    val bodyColor = MaterialTheme.colorScheme.surface

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = bodyColor)
    ) {
        Column {
            Box(
                modifier = Modifier.fillMaxWidth().background(headerColor).padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = form.companyName,
                        style = MaterialTheme.typography.titleLarge,
                        color = headerTextColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    // "Teilen"-Button nur für Entwürfe anzeigen
                    if (form.status == "entwurf") {
                        IconButton(onClick = onShareClick) {
                            Icon(Icons.Outlined.Share, contentDescription = "Mit Kunde teilen", tint = headerTextColor)
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(form.address, style = MaterialTheme.typography.bodyLarge)
                Text(form.mainContact, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(4.dp))
                Text("${form.scopeInDays} Tagwerk zu ${form.dailyRate} Euro", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
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

fun formatDate(dateString: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMANY)
        val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY)
        formatter.format(parser.parse(dateString)!!)
    } catch (e: Exception) {
        dateString
    }
}