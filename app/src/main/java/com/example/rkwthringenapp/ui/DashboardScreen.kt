package com.example.rkwthringenapp.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.rkwthringenapp.data.FormSummary
import com.example.rkwthringenapp.ui.theme.App_Status_Entwurf_Bar
import com.example.rkwthringenapp.ui.theme.App_Status_Gesendet_Bar
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

    // Reaktion auf die "Teilen"-Aktion (unverändert)
    when (val result = shareResult) {
        is ShareResult.Loading -> { AlertDialog(onDismissRequest = {}, title = { Text("Einladung wird gesendet...") }, text = { Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) { CircularProgressIndicator() } }, confirmButton = {}) }
        is ShareResult.Success -> { LaunchedEffect(result) { Toast.makeText(context, result.message, Toast.LENGTH_LONG).show(); dashboardViewModel.clearShareResult() } }
        is ShareResult.Error -> { AlertDialog(onDismissRequest = { dashboardViewModel.clearShareResult() }, title = { Text("Fehler beim Teilen") }, text = { Text(result.message) }, confirmButton = { Button(onClick = { dashboardViewModel.clearShareResult() }) { Text("OK") } }) }
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
            FloatingActionButton(
                onClick = {
                    rkwFormViewModel.startNewForm()
                    navController.navigate("step1")
                },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Neuen Bogen anlegen", tint = Color.White)
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
                Text("Fehler: ${dashboardState.error}", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
            } else if (dashboardState.forms.isEmpty()) {
                Text("Sie haben noch keine Erfassungsbögen angelegt.", textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(dashboardState.forms) { form ->
                        // HIER IST DER AUFRUF, DER DEN FEHLER VERURSACHT HAT
                        FormCardVerticalLabel(
                            form = form,
                            onClick = {
                                if (form.status == "entwurf") {
                                    rkwFormViewModel.loadDraft(form.id)
                                    navController.navigate("step1")
                                } else {
                                    navController.navigate("sentFormDetail/${form.id}")
                                }
                            },
                            // Wir übergeben die onShareClick-Funktion nur für Entwürfe
                            onShareClick = if (form.status == "entwurf") { { dashboardViewModel.shareForm(form.id) } } else null
                        )
                    }
                }
            }
        }
    }
}

// HIER IST DIE KORRIGIERTE DEFINITION DER FUNKTION
@Composable
fun FormCardVerticalLabel(form: FormSummary, onClick: () -> Unit, onShareClick: (() -> Unit)?) {
    val isDraft = form.status == "entwurf"
    val statusBarColor = if (isDraft) App_Status_Entwurf_Bar else App_Status_Gesendet_Bar
    val contentColor = if (isDraft) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary
    val statusText = if (isDraft) "ENTWURF" else "GESENDET"
    val dateText = if (isDraft) "Zuletzt bearbeitet: ${formatDate(form.updated_at)}" else "Gesendet am: ${formatDate(form.updated_at)}"
    val honorarColor = if (isDraft) MaterialTheme.colorScheme.primary else Color.White

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        // Linke, vertikale Status-Leiste
        Box(
            modifier = Modifier
                .width(50.dp)
                .fillMaxHeight()
                .background(statusBarColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = if (isDraft) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .graphicsLayer { rotationZ = -90f }
                    .wrapContentWidth(unbounded = true)
            )
        }

        // Rechter Hauptinhaltsbereich
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(contentColor)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = form.companyName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = form.mainContact,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${form.scopeInDays} Tagwerk zu ${form.dailyRate} Euro",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = honorarColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = dateText,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f)
            )
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